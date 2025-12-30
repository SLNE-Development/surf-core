plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

dependencies {
    api(project(":surf-core-api:surf-core-api-common"))
    api("dev.slne.surf:surf-redis:1.0.0-SNAPSHOT")
}