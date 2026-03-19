plugins {
    id("dev.slne.surf.surfapi.gradle.velocity")
}

surfVelocityApi {
    withSurfRedis()
}

velocityPluginFile {
    main = "dev.slne.surf.core.velocity.VelocityMain"
    authors = listOf("red")
}

dependencies {
    api(projects.surfCoreCore.surfCoreCoreVelocity)
}