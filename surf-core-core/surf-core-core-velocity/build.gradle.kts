plugins {
    id("dev.slne.surf.surfapi.gradle.velocity")
}

dependencies {
    api(projects.surfCoreCore.surfCoreCoreClient)
    api(projects.surfCoreApi.surfCoreApiVelocity)
}