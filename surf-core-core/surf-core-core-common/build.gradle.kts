import dev.slne.surf.api.gradle.util.slneReleases
import dev.slne.surf.microservice.gradle.plugin.rabbit.RabbitModule

plugins {
    id("dev.slne.surf.api.gradle.core")
    id("dev.slne.surf.microservice")
}

surfCoreApi {
    withSurfRedis()
}

surfMicroservice {
    withRabbitModule(RabbitModule.CLIENT_API)
}

dependencies {
    api(projects.surfCoreApi.surfCoreApiCommon)
}

publishing {
    repositories {
        slneReleases()
    }
}
