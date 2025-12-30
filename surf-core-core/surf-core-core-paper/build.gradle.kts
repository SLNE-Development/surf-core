plugins {
    id("dev.slne.surf.surfapi.gradle.paper-raw")
}

dependencies {
    api(project(":surf-core-core:surf-core-core-common"))
    api(project(":surf-core-api:surf-core-api-paper"))
}