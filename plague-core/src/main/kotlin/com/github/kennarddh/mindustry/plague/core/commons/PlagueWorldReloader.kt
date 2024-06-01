package com.github.kennarddh.mindustry.plague.core.commons

import kotlinx.coroutines.runBlocking
import mindustry.Vars
import mindustry.gen.Call
import mindustry.gen.Groups
import mindustry.gen.Player

class PlagueWorldReloader {
    var players = mutableSetOf<Player>()
    var wasServer = false
    var began = false

    /**
     * Begins reloading the world. Sends world begin packets to each user and stores player state.
     * If the current client is not a server, this resets state and disconnects.
     */
    fun begin() {
        // Don't begin twice
        if (began) return

        if (Vars.net.server().also { wasServer = it }) {
            players.clear()

            Groups.player.forEach {
                if (it.isLocal) return@forEach

                players.add(it)

                it.clearUnit()
            }

            Vars.logic.reset()

            Call.worldDataBegin()
        } else {
            if (Vars.net.client()) {
                Vars.net.reset()
            }

            Vars.logic.reset()
        }

        began = true
    }

    /**
     * Ends reloading the world. Sends world data to each player.
     * If the current client was not a server, does nothing.
     */
    fun end() {
        if (!wasServer) return

        runBlocking {
            players.forEach {
                if (it.con == null) return@forEach

                val wasAdmin = it.admin

                it.reset()

                it.admin = wasAdmin

                it.team(Vars.netServer.assignTeam(it, players))

                Vars.netServer.sendWorldData(it)
            }
        }
    }
}