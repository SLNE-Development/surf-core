plugins {
    id("dev.slne.surf.api.gradle.standalone")
}

dependencies {
    api(projects.surfCoreLauncher.surfCoreLauncherApi)
    implementation(projects.surfCoreApi.surfCoreApiCommon)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    implementation("dev.slne.surf.redis:surf-redis-standalone:1.6.1")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "dev.slne.surf.core.launcher.server.CoreLauncherKt"
        attributes["Implementation-Version"] = project.version
    }
}

tasks.shadowJar {
    exclude("okio/**")
    exclude("io/netty/**")
}