package dev.slne.surf.core.minestom.command

import dev.slne.minestom.lobby.api.command.CommandRegistrar
import revxrsal.commands.Lamp
import revxrsal.commands.minestom.actor.MinestomCommandActor

abstract class SurfCoreMinestomCommand : CommandRegistrar {
    final override fun register(lamp: Lamp<MinestomCommandActor>) {
        lamp.register(this)
    }
}
