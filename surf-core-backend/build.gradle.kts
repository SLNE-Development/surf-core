plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

surfCoreApi {
    withSurfRedis()
}

dependencies {
    api("dev.slne.surf:surf-database-r2dbc:1.0.0-SNAPSHOT")
    api(project(":surf-core-core:surf-core-core-common"))
}