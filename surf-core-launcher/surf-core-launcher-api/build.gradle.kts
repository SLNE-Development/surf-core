plugins {
    id("dev.slne.surf.api.gradle.core")
}

surfCoreApi {
    withSurfRedis()
}

dependencies {
    api(projects.surfCoreApi.surfCoreApiCommon)
}