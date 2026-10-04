import dev.slne.surf.api.gradle.util.slneReleases

plugins {
    id("dev.slne.surf.api.gradle.minestom")
}

surfMinestomApi {
    withSurfRedis()
}

minestomPluginFile {
    main = "dev.slne.surf.core.minestom.SurfCoreMinestomPlugin"
    authors = listOf("red")

    pluginDependencies {
        register("surf-redis-minestom")
        register("surf-rabbitmq-minestom")
    }
}

dependencies {
    api(projects.surfCoreCore.surfCoreCoreMinestom)
}

publishing {
    repositories {
        slneReleases()
    }
}
