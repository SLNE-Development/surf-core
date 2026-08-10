import dev.slne.surf.api.gradle.util.slneReleases

plugins {
    id("dev.slne.surf.api.gradle.minestom")
}

surfMinestomApi {
    withSurfRedis()
}

dependencies {
    api(projects.surfCoreCore.surfCoreCoreMinestom)
}

publishing {
    repositories {
        slneReleases()
    }
}
