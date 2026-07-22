package dev.slne.surf.core.launcher.server.updater.github

import dev.slne.surf.core.launcher.server.updater.GitHubRepositoryCoordinates
import dev.slne.surf.core.launcher.server.updater.asset.ReleaseAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import org.kohsuke.github.*
import java.io.IOException
import java.io.InputStream
import java.io.Serial
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.Duration
import kotlin.coroutines.coroutineContext

class LatestGitHubRelease(
    val repository: GHRepository,
    val release: GHRelease,
    val tagName: String,
    val assets: List<ReleaseAsset>
)

sealed interface LatestReleaseResult {
    data class Found(val value: LatestGitHubRelease) : LatestReleaseResult
    data class RepositoryUnavailable(val repository: GitHubRepositoryCoordinates) :
        LatestReleaseResult

    data class NoPublishedRelease(val repository: GitHubRepositoryCoordinates) : LatestReleaseResult
    data class Failed(
        val repository: GitHubRepositoryCoordinates,
        val operation: String,
        val status: Int?,
        val message: String,
        val rateLimit: String?
    ) : LatestReleaseResult
}

class GitHubAssetDownloadException(
    val status: Int?,
    val assetName: String,
    val rateLimit: String?,
    message: String,
    cause: Throwable? = null
) : IOException(message, cause) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 8731427226239280476L
    }
}

class GitHubReleaseClient(token: String?) {
    private val normalizedToken = token?.trim()?.takeIf(String::isNotEmpty)
    private val github: GitHub = GitHubBuilder()
        .apply {
            if (normalizedToken != null) {
                withOAuthToken(normalizedToken)
            }
        }
        .build()

    private val downloadClient = HttpClient.newBuilder()
        .connectTimeout(CONNECT_TIMEOUT)
        .followRedirects(HttpClient.Redirect.NEVER)
        .build()

    val isAuthenticated: Boolean
        get() = normalizedToken != null

    suspend fun fetchLatestRelease(
        repository: GitHubRepositoryCoordinates
    ): LatestReleaseResult = runInterruptible(Dispatchers.IO) {
        val githubRepository = try {
            github.getRepository(repository.fullName)
        } catch (_: GHFileNotFoundException) {
            return@runInterruptible LatestReleaseResult.RepositoryUnavailable(repository)
        } catch (exception: IOException) {
            return@runInterruptible failure(repository, "resolve repository", exception)
        }

        val release = try {
            githubRepository.latestRelease
        } catch (_: GHFileNotFoundException) {
            return@runInterruptible LatestReleaseResult.NoPublishedRelease(repository)
        } catch (exception: IOException) {
            return@runInterruptible failure(repository, "fetch latest release", exception)
        } ?: return@runInterruptible LatestReleaseResult.NoPublishedRelease(repository)

        val assets = try {
            release.listAssets()
                .toList()
                .map { asset ->
                    ReleaseAsset(
                        id = asset.id,
                        name = asset.name,
                        size = asset.size.takeIf { it > 0 },
                        apiUri = asset.url.toURI()
                    )
                }
        } catch (exception: Exception) {
            return@runInterruptible failure(repository, "read release assets", exception)
        }

        LatestReleaseResult.Found(
            LatestGitHubRelease(
                repository = githubRepository,
                release = release,
                tagName = release.tagName,
                assets = assets
            )
        )
    }

    suspend fun downloadAsset(asset: ReleaseAsset, target: Path) = withContext(Dispatchers.IO) {
        var uri = asset.apiUri
        val apiAuthority = URI(github.apiUrl).authority
        if (!uri.authority.equals(apiAuthority, ignoreCase = true)) {
            throw GitHubAssetDownloadException(
                status = null,
                assetName = asset.name,
                rateLimit = null,
                message = "asset API URL does not belong to the configured GitHub API"
            )
        }

        repeat(MAX_REDIRECTS + 1) { redirectCount ->
            coroutineContext.ensureActive()
            requireSecureUri(uri, asset.name)

            val request = HttpRequest.newBuilder(uri)
                .GET()
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/octet-stream")
                .header("User-Agent", USER_AGENT)
                .header("X-GitHub-Api-Version", GITHUB_API_VERSION)
                .apply {
                    if (normalizedToken != null && uri.authority.equals(
                            apiAuthority,
                            ignoreCase = true
                        )
                    ) {
                        header("Authorization", "Bearer $normalizedToken")
                    }
                }
                .build()

            val response = try {
                runInterruptible(Dispatchers.IO) {
                    downloadClient.send(request, HttpResponse.BodyHandlers.ofInputStream())
                }
            } catch (exception: IOException) {
                throw GitHubAssetDownloadException(
                    status = null,
                    assetName = asset.name,
                    rateLimit = null,
                    message = "request failed: ${conciseMessage(exception)}",
                    cause = exception
                )
            }

            when (response.statusCode()) {
                200 -> {
                    response.body().use { input -> streamToFile(input, target) }
                    return@withContext
                }

                in REDIRECT_STATUSES -> {
                    response.body().close()
                    if (redirectCount == MAX_REDIRECTS) {
                        throw GitHubAssetDownloadException(
                            status = response.statusCode(),
                            assetName = asset.name,
                            rateLimit = response.rateLimitDescription(),
                            message = "too many redirects"
                        )
                    }
                    val location = response.headers().firstValue("Location").orElse(null)
                        ?: throw GitHubAssetDownloadException(
                            status = response.statusCode(),
                            assetName = asset.name,
                            rateLimit = response.rateLimitDescription(),
                            message = "redirect response did not include a Location header"
                        )
                    uri = uri.resolve(location)
                }

                else -> {
                    val responseMessage = response.body().use(::readConciseError)
                    throw GitHubAssetDownloadException(
                        status = response.statusCode(),
                        assetName = asset.name,
                        rateLimit = response.rateLimitDescription(),
                        message = responseMessage
                    )
                }
            }
        }

        error("Unreachable redirect loop")
    }

    private suspend fun streamToFile(input: InputStream, target: Path) {
        withContext(Dispatchers.IO) {
            Files.newOutputStream(
                target,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
            ).use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    coroutineContext.ensureActive()
                    val count = input.read(buffer)
                    if (count < 0) {
                        break
                    }
                    output.write(buffer, 0, count)
                }
            }
        }
    }

    private fun failure(
        repository: GitHubRepositoryCoordinates,
        operation: String,
        exception: Exception
    ) = LatestReleaseResult.Failed(
        repository = repository,
        operation = operation,
        status = (exception as? HttpException)?.responseCode,
        message = conciseMessage(exception),
        rateLimit = lastKnownRateLimit()
    )

    @Suppress("DEPRECATION")
    private fun lastKnownRateLimit(): String? = github.lastRateLimit().core
        .takeIf { it.limit > 0 }
        ?.let { rateLimit ->
            "remaining=${rateLimit.remaining}/${rateLimit.limit}, reset=${rateLimit.resetEpochSeconds}"
        }

    private fun HttpResponse<*>.rateLimitDescription(): String? {
        val remaining = headers().firstValue("X-RateLimit-Remaining").orElse(null) ?: return null
        val reset = headers().firstValue("X-RateLimit-Reset").orElse("unknown")
        return "remaining=$remaining, reset=$reset"
    }

    private fun requireSecureUri(uri: URI, assetName: String) {
        if (!uri.scheme.equals("https", ignoreCase = true) || uri.host.isNullOrBlank()) {
            throw GitHubAssetDownloadException(
                status = null,
                assetName = assetName,
                rateLimit = null,
                message = "refused insecure or invalid redirect target"
            )
        }
    }

    companion object {
        private const val USER_AGENT = "surf-core-launcher"
        private const val GITHUB_API_VERSION = "2022-11-28"
        private const val MAX_REDIRECTS = 5
        private const val MAX_ERROR_BYTES = 4096
        private val CONNECT_TIMEOUT = Duration.ofSeconds(10)
        private val REQUEST_TIMEOUT = Duration.ofSeconds(20)
        private val REDIRECT_STATUSES = setOf(301, 302, 303, 307, 308)

        private fun readConciseError(input: InputStream): String {
            val content = input.readNBytes(MAX_ERROR_BYTES)
                .toString(Charsets.UTF_8)
                .replace(Regex("\\s+"), " ")
                .trim()
            return when {
                content.isBlank() -> "GitHub returned an empty error response"
                content.startsWith('<') -> "GitHub returned an HTML error response"
                else -> content.take(500)
            }
        }

        private fun conciseMessage(exception: Throwable): String = exception.message
            ?.replace(Regex("\\s+"), " ")
            ?.trim()
            ?.take(500)
            ?.takeIf(String::isNotEmpty)
            ?: exception::class.simpleName.orEmpty()
    }
}
