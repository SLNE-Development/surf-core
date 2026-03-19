import dev.slne.surf.surfapi.gradle.util.registerRequired

plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.core.paper.PaperMain")
    bootstrapper("dev.slne.surf.core.paper.PaperBootstrap")
    generateLibraryLoader(false)
    foliaSupported(true)

    withSurfRedis()

    authors.add("red")

    serverDependencies {
        registerRequired("LuckPerms")
    }
}

dependencies {
    api(projects.surfCoreCore.surfCoreCorePaper)
    compileOnly("net.luckperms:api:5.4")
}