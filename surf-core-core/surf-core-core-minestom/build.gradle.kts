plugins {
    id("dev.slne.surf.api.gradle.minestom")
}

dependencies {
    api(projects.surfCoreCore.surfCoreCoreClient)
    api(projects.surfCoreApi.surfCoreApiMinestom)
}
