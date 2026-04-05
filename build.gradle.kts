import dev.slne.surf.api.gradle.util.slneReleases

buildscript {
    repositories {
        gradlePluginPortal()
        maven("https://reposilite.slne.dev/releases")
        maven("https://repo.slne.dev/repository/maven-public/") { name = "maven-public" }
    }
    dependencies {
        classpath("dev.slne.surf.api:surf-api-gradle-plugin:+")
        classpath("dev.slne.surf.microservice:surf-microservice-gradle-plugin:1.21.11+")
    }
}

allprojects {
    group = "dev.slne.surf.core"
    version = findProperty("version") as String
}

subprojects {
    afterEvaluate {
        plugins.withType<PublishingPlugin> {
            configure<PublishingExtension> {
                repositories {
                    slneReleases()
                }
            }
        }
    }
}