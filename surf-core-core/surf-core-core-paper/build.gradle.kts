plugins {
    id("dev.slne.surf.api.gradle.paper-raw")
}

dependencies {
    api(projects.surfCoreCore.surfCoreCoreClient)
    api(projects.surfCoreApi.surfCoreApiPaper)
}