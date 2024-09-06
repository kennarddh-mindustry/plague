package com.github.kennarddh.mindustry.plague.core.handlers

import com.github.kennarddh.mindustry.genesis.core.events.annotations.EventHandler
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.genesis.standard.extensions.infoMessage
import mindustry.game.EventType
import mindustry.net.Administration.Config

class WelcomeHandler : Handler {
    override suspend fun onInit() {
        Config.motd.set("Welcome to Plague server.")
    }

    @EventHandler
    fun onPlayerJoin(event: EventType.PlayerJoin) {
        event.player.infoMessage(
            """
            This is a game of [forest]Plague[](default) vs [yellow]Survivor[]. Last one standing.
            Prepare state(2 minutes): Choose to be [yellow]Survivor[], or join a [yellow]Survivor[] team. Place any block away from plague core to become [yellow]Survivor[].
            First Phase(45 minutes): [forest]Plague[] make units, [yellow]Survivor[] make defense. Air units don't do damage.
            Second Phase(15 minutes): Air units do damage.
            Sudden Death: [yellow]Survivors[] won, but [forest]Plague[] multiplies in strength. [yellow]Survive[] as long as possible.
            
            Full Rules: Join discord by using [accent]/discord[] command.
            """.trimIndent()
        )
    }
}