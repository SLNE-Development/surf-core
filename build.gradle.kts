import dev.slne.surf.api.gradle.util.slneReleases

buildscript {
    repositories {
        gradlePluginPortal()
        maven("https://repo.slne.dev/repository/maven-public/") { name = "maven-public" }
    }
    dependencies {
        classpath("dev.slne.surf:surf-api-gradle-plugin:26+")
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