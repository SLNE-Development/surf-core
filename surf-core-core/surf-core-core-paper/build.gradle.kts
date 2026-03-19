plugins {
    id("dev.slne.surf.surfapi.gradle.paper-raw")
}

dependencies {
    api(projects.surfCoreCore.surfCoreCoreClient)
    api(projects.surfCoreApi.surfCoreApiPaper)
}