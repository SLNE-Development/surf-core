plugins {
    id("dev.slne.surf.api.gradle.velocity")
}

dependencies {
    api(projects.surfCoreCore.surfCoreCoreClient)
    api(projects.surfCoreApi.surfCoreApiVelocity)
}