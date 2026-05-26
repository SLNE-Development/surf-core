plugins {
    id("dev.slne.surf.api.gradle.standalone")
}

dependencies {
    api(projects.surfCoreLauncher.surfCoreLauncherApi)
    implementation("dev.slne.surf.redis:surf-redis-api:1.6.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    implementation(projects.surfCoreApi.surfCoreApiCommon)
}

tasks.shadowJar {
    relocate("dev.slne.surf.redis", "dev.slne.surf.core.launcher.libs")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "dev.slne.surf.core.launcher.server.CoreLauncherKt"
    }
}