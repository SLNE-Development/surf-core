package dev.slne.surf.core.velocity.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.core.core.common.util.appendCorePrefix
import dev.slne.surf.core.velocity.permission.PermissionList
import dev.slne.surf.core.velocity.plugin
import kotlin.jvm.optionals.getOrNull

fun coreCommand() = commandAPICommand("core") {
    withPermission(PermissionList.CORE_COMMAND)

    anyExecutor { source, _ ->
        val coreVersion =
            plugin.pluginManager.getPlugin("surf-core-velocity")
                .getOrNull()?.description?.version?.getOrNull() ?: "Unbekannt"
        val platform = plugin.proxy.version.name
        val platformVersion = plugin.proxy.version.version
        val vendor = plugin.proxy.version.vendor

        source.sendText {
            appendCorePrefix()
            info("This proxy is running ")
            variableValue("surf-core-velocity")
            info(" version ")
            variableValue(coreVersion)
            info(" on ")
            variableValue(platform)
            appendSpace()
            variableValue(platformVersion)
            info(" by ")
            variableValue(vendor)
            info(".")
        }
    }

    corePlayerCommand()
    coreServiceCommand()
}