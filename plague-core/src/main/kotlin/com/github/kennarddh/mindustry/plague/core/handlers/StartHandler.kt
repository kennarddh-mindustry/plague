package com.github.kennarddh.mindustry.plague.core.handlers

import arc.Core
import arc.struct.Seq
import arc.util.Reflect
import arc.util.Timer
import com.github.kennarddh.mindustry.genesis.core.commons.runOnMindustryThread
import com.github.kennarddh.mindustry.genesis.core.events.annotations.EventHandler
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.plague.core.commons.PlagueVars
import com.github.kennarddh.mindustry.plague.core.commons.extensions.Logger
import kotlinx.coroutines.delay
import mindustry.Vars
import mindustry.content.UnitTypes
import mindustry.game.EventType
import mindustry.game.Gamemode
import mindustry.game.Team
import mindustry.net.Administration.Config
import mindustry.server.ServerControl
import kotlin.time.Duration.Companion.seconds

class StartHandler : Handler {
    @EventHandler
    suspend fun onLoad(event: EventType.ServerLoadEvent) {
        Logger.info("Server load... Will host in 1 second.")

        Config.port.set(PlagueVars.port)

        Logger.info("Port set to ${PlagueVars.port}")

        delay(1.seconds)

        Logger.info("Hosting")

        host()
    }

    private fun host() {
        runOnMindustryThread {
            val gameMode = Gamemode.survival

            // TODO: When v147 released replace this with ServerControl.instance.cancelPlayTask()
            Reflect.get<Timer.Task>(ServerControl.instance, "lastTask")?.cancel()

            val map = Vars.maps.shuffleMode.next(gameMode, Vars.state.map)

            Vars.logic.reset()

            ServerControl.instance.lastMode = gameMode

            Core.settings.put("lastServerMode", ServerControl.instance.lastMode.name)
            Vars.world.loadMap(map, map.applyRules(ServerControl.instance.lastMode))

            Vars.state.rules.canGameOver = false
            Vars.state.rules.hideBannedBlocks = true

            // Remove units weapon so they have 0 damage.
            // This is a slight desync but not noticeable because on client they will see they can shoot but with 0 damage.
            UnitTypes.alpha.weapons = Seq()
            UnitTypes.beta.weapons = Seq()
            UnitTypes.gamma.weapons = Seq()
            UnitTypes.poly.weapons = Seq()
            UnitTypes.flare.weapons = Seq()

            Team.all.filter { it != Team.malis }.forEach {
                Vars.state.rules.teams[it].unitCrashDamageMultiplier = 0f
            }

            Vars.state.rules.modeName = "Plague"

            Vars.state.rules.hideBannedBlocks = true
            Vars.state.rules.unitWhitelist = false
            Vars.state.rules.blockWhitelist = false

            Vars.logic.play()

            Vars.netServer.openServer()

            Logger.info("Hosted")
        }
    }
}