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
}

dependencies {
    api(project(":surf-core-core:surf-core-core-paper"))
    runtimeOnly(project(":surf-core-backend"))
}