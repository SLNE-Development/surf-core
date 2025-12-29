plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.core.paper.PaperMain")
    generateLibraryLoader(false)
    foliaSupported(true)

    authors.add("red")
}

dependencies {
    api(project(":surf-core-core:surf-core-core-paper"))
}