package dev.slne.surf.core.microservice

import com.google.auto.service.AutoService
import dev.slne.surf.core.microservice.database.tables.SurfPlayerIpAddressHistoriesTable
import dev.slne.surf.core.microservice.database.tables.SurfPlayerNameHistoriesTable
import dev.slne.surf.core.microservice.database.tables.SurfPlayerTexturesHistoriesTable
import dev.slne.surf.core.microservice.database.tables.SurfPlayersTable
import dev.slne.surf.core.microservice.rabbit.IpAddressHistoryHandler
import dev.slne.surf.core.microservice.rabbit.NameHistoryHandler
import dev.slne.surf.core.microservice.rabbit.SkinHistoryHandler
import dev.slne.surf.core.microservice.rabbit.SurfPlayerHandler
import dev.slne.surf.database.DatabaseApi
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.microservice.api.microservice.Microservice
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import kotlin.io.path.Path

@AutoService(Microservice::class)
class CoreMicroservice : Microservice() {
    private val databaseApi = DatabaseApi.create(Path("data/database"))
    private val rabbitApi = ServerRabbitMQApi.create(1, "surf-core")

    override suspend fun onBootstrap(args: List<String>) {
        suspendTransaction {
            SchemaUtils.create(
                SurfPlayerIpAddressHistoriesTable,
                SurfPlayerNameHistoriesTable,
                SurfPlayersTable,
                SurfPlayerTexturesHistoriesTable
            )
        }

        rabbitApi.registerRequestHandler(IpAddressHistoryHandler)
        rabbitApi.registerRequestHandler(NameHistoryHandler)
        rabbitApi.registerRequestHandler(SkinHistoryHandler)
        rabbitApi.registerRequestHandler(SurfPlayerHandler)

        rabbitApi.freezeAndConnect()
    }

    override suspend fun onDisable() {
        rabbitApi.disconnect()
        databaseApi.shutdown()
    }
}