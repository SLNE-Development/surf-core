import dev.slne.surf.api.gradle.util.slneReleases

plugins {
    id("dev.slne.surf.api.gradle.paper-raw")
}

surfRawPaperApi {
    testing {
        mockBukkit = false
    }
}

dependencies {
    api(projects.surfCoreApi.surfCoreApiCommon)
}

publishing {
    repositories {
        slneReleases()
    }
}