import dev.slne.surf.api.gradle.util.slneReleases

plugins {
    id("dev.slne.surf.api.gradle.core")
}

surfCoreApi {
    withSurfRedis()
}

dependencies {
    api(projects.surfCoreApi.surfCoreApiCommon)
}

publishing {
    repositories {
        slneReleases()
    }
}