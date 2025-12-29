plugins {
    id("dev.slne.surf.surfapi.gradle.velocity")
}

dependencies {
    api(project(":surf-core-core:surf-core-core-common"))
    api(project(":surf-core-core:surf-core-api-velocity"))
}