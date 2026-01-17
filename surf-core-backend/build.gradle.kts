plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

surfCoreApi {
    withSurfRedis()
    withSurfDatabaseR2dbc("1.0.1-SNAPSHOT", "dev.slne.surf.core.libs.db")
}

dependencies {
    api(project(":surf-core-core:surf-core-core-common"))
}