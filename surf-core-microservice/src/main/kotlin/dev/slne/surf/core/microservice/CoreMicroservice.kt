package dev.slne.surf.core.microservice

import com.google.auto.service.AutoService
import dev.slne.surf.core.microservice.database.tables.*
import dev.slne.surf.core.microservice.rabbit.*
import dev.slne.surf.database.DatabaseApi
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.microservice.api.microservice.Microservice
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import kotlin.io.path.Path

@AutoService(Microservice::class)
class CoreMicroservice : Microservice() {
    override val dataPath = Path("config")
    private val databaseApi = DatabaseApi.create(dataPath)
    private val rabbitApi = ServerRabbitMQApi.create("surf-core", dataPath)

    override suspend fun onBootstrap(args: List<String>) {
        suspendTransaction {
            SchemaUtils.create(
                SurfPlayerIpAddressHistoriesTable,
                SurfPlayerNameHistoriesTable,
                SurfPlayersTable,
                SurfPlayerTexturesHistoriesTable,
                SurfPlayerErrorsTable
            )
        }

        rabbitApi.registerRequestHandler(IpAddressHistoryHandler)
        rabbitApi.registerRequestHandler(NameHistoryHandler)
        rabbitApi.registerRequestHandler(SkinHistoryHandler)
        rabbitApi.registerRequestHandler(SurfPlayerHandler)
        rabbitApi.registerRequestHandler(SurfPlayerErrorHandler)

        rabbitApi.freezeAndConnect()
    }

    override suspend fun onDisable() {
        rabbitApi.disconnect()
        databaseApi.shutdown()
    }
}