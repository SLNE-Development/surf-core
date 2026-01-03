plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

surfCoreApi {
    withSurfRedis()
}

dependencies {
    api(project(":surf-core-api:surf-core-api-common"))
}