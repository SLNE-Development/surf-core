package dev.slne.surf.core.fallback

import com.google.auto.service.AutoService
import dev.slne.surf.core.core.common.database.DatabaseLoader
import dev.slne.surf.core.fallback.table.SurfPlayerIpAddressHistoryTable
import dev.slne.surf.core.fallback.table.SurfPlayerNameHistoryTable
import dev.slne.surf.core.fallback.table.SurfPlayerTable
import dev.slne.surf.core.fallback.table.SurfPlayerTexturesHistoryTable
import dev.slne.surf.database.DatabaseApi
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import net.kyori.adventure.util.Services
import java.nio.file.Path

@AutoService(DatabaseLoader::class)
class DatabaseLoaderImpl : DatabaseLoader, Services.Fallback {
    lateinit var databaseApi: DatabaseApi
    override suspend fun connect(dataPath: Path) {
        databaseApi = DatabaseApi.create(dataPath)

        suspendTransaction {
            SchemaUtils.create(
                SurfPlayerTable,
                SurfPlayerNameHistoryTable,
                SurfPlayerIpAddressHistoryTable,
                SurfPlayerTexturesHistoryTable
            )
        }
    }

    override fun disconnect() {
        databaseApi.shutdown()
    }
}