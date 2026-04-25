plugins {
    id("dev.slne.surf.api.gradle.velocity")
}

surfVelocityApi {
    withSurfRedis()
}

velocityPluginFile {
    main = "dev.slne.surf.core.velocity.VelocityMain"
    authors = listOf("red")
    version = findProperty("version") as String

    pluginDependencies {
        register("surf-rabbitmq-velocity")
    }
}

dependencies {
    api(projects.surfCoreCore.surfCoreCoreVelocity)
}