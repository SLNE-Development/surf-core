plugins {
    id("dev.slne.surf.surfapi.gradle.velocity")
}

surfVelocityApi {
    withSurfRedis()
}

velocityPluginFile {
    main = "dev.slne.surf.core.velocity.VelocityMain"
    authors = listOf("red")

    pluginDependencies {
        register("surf-redis-velocity") {
            optional = false
        }
    }
}

dependencies {
    api(project(":surf-core-core:surf-core-core-velocity"))
    runtimeOnly(project(":surf-core-backend"))
}