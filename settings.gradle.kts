pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://reposilite.slne.dev/releases")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.slne.surf.api.gradle.settings") version "+"
}

// Api
include("surf-core-api")
include("surf-core-api:surf-core-api-common")
include("surf-core-api:surf-core-api-paper")
include("surf-core-api:surf-core-api-velocity")

// Core
include("surf-core-core")
include("surf-core-core:surf-core-core-common")
include("surf-core-core:surf-core-core-client")
include("surf-core-core:surf-core-core-paper")
include("surf-core-core:surf-core-core-velocity")

// Client
include("surf-core-paper")
include("surf-core-velocity")

// Microservice
include("surf-core-microservice")

// Launcher
include("surf-core-launcher")
include("surf-core-launcher:surf-core-launcher-api")
include("surf-core-launcher:surf-core-launcher-server")