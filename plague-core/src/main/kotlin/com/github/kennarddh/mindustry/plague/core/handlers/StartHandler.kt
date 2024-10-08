package com.github.kennarddh.mindustry.plague.core.handlers

import arc.Events
import arc.util.Reflect
import arc.util.Timer
import com.github.kennarddh.mindustry.genesis.core.Genesis
import com.github.kennarddh.mindustry.genesis.core.commons.runOnMindustryThread
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.plague.core.commons.Logger
import com.github.kennarddh.mindustry.plague.core.commons.PlagueRules
import com.github.kennarddh.mindustry.plague.core.commons.PlagueVars
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mindustry.Vars
import mindustry.game.EventType
import mindustry.game.Gamemode
import mindustry.net.Administration.Config
import mindustry.server.ServerControl
import kotlin.time.Duration.Companion.seconds

class StartHandler : Handler {
    override suspend fun onInit() {
        Genesis.commandRegistry.removeCommand("host")
        Genesis.commandRegistry.removeCommand("load")

        Config.messageRateLimit.set(1)
        Vars.netServer.admins.playerLimit = 30

        // @EventHandler Annotation doesn't always work when the EventType is ServerLoadEvent
        Events.on(EventType.ServerLoadEvent::class.java) { _ ->
            Logger.info("Server load... Will host in 1 second.")

            Config.port.set(PlagueVars.port)

            Logger.info("Port set to ${PlagueVars.port}")

            runBlocking {
                delay(1.seconds)
            }

            Logger.info("Hosting")

            host()
        }
    }

    private fun host() {
        runOnMindustryThread {
            val gameMode = Gamemode.survival

            // TODO: When v147 released replace this with ServerControl.instance.cancelPlayTask()
            Reflect.get<Timer.Task>(ServerControl.instance, "lastTask")?.cancel()

            val map = Vars.maps.shuffleMode.next(gameMode, Vars.state.map)

            Vars.logic.reset()

            ServerControl.instance.lastMode = gameMode

            Vars.world.loadMap(map)

            Vars.state.rules = PlagueRules.initRules(Vars.state.map.rules())

            Vars.logic.play()

            Vars.netServer.openServer()

            Logger.info("Hosted")
        }
    }
}