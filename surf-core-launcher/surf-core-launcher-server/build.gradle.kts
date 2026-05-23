plugins {
    id("dev.slne.surf.api.gradle.standalone")
}

dependencies {
    api(projects.surfCoreLauncher.surfCoreLauncherApi)
    implementation("dev.slne.surf.redis:surf-redis-api:1.6.0")
}

tasks.shadowJar {
    relocate("dev.slne.surf.redis", "dev.slne.surf.core.launcher.libs")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "dev.slne.surf.core.launcher.server.CoreLauncherKt"
    }
}