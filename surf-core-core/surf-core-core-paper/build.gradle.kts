plugins {
    id("dev.slne.surf.api.gradle.paper-raw")
}

surfRawPaperApi {
    testing {
        mockBukkit = false
    }
}

dependencies {
    api(projects.surfCoreCore.surfCoreCoreClient)
    api(projects.surfCoreApi.surfCoreApiPaper)
}