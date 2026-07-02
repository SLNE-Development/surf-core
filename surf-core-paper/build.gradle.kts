import dev.slne.surf.api.gradle.util.registerRequired

plugins {
    id("dev.slne.surf.api.gradle.paper-plugin")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.core.paper.PaperMain")
    bootstrapper("dev.slne.surf.core.paper.PaperBootstrap")
    generateLibraryLoader(false)
    foliaSupported(true)

    withSurfRedis()

    authors.add("red")

    bootstrapDependencies {
        registerRequired("surf-rabbitmq-paper")
    }

    serverDependencies {
        registerRequired("LuckPerms")
        registerRequired("surf-rabbitmq-paper")
    }
}

dependencies {
    api(projects.surfCoreCore.surfCoreCorePaper)
    compileOnly("net.luckperms:api:5.4")
    implementation(projects.surfCoreLauncher.surfCoreLauncherApi)
}