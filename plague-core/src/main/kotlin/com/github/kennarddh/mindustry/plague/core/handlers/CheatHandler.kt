package com.github.kennarddh.mindustry.plague.core.handlers

import com.github.kennarddh.mindustry.genesis.core.Genesis
import com.github.kennarddh.mindustry.genesis.core.commands.annotations.Command
import com.github.kennarddh.mindustry.genesis.core.commands.senders.CommandSender
import com.github.kennarddh.mindustry.genesis.core.commands.senders.PlayerCommandSender
import com.github.kennarddh.mindustry.genesis.core.commons.runOnMindustryThread
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.genesis.standard.commands.parameters.validations.numbers.GTE
import com.github.kennarddh.mindustry.genesis.standard.commands.parameters.validations.numbers.LTE
import com.github.kennarddh.mindustry.plague.core.commands.validations.Admin
import com.github.kennarddh.mindustry.plague.core.commons.PlagueVars
import com.github.kennarddh.mindustry.plague.core.commons.extensions.toDisplayString
import mindustry.Vars
import mindustry.game.Team
import mindustry.type.UnitType
import kotlin.math.floor
import kotlin.time.Duration

class CheatHandler : Handler {
    @Command(["skiptime"])
    @Admin
    fun skipTime(sender: CommandSender, duration: Duration) {
        PlagueVars.totalMapSkipDuration += duration

        sender.sendSuccess("Skipped '${duration.toDisplayString()}'. Current map time is '${PlagueVars.mapTime.toDisplayString()}'.")
    }

    @Command(["spawnunit"])
    @Admin
    fun spawnUnit(
        sender: PlayerCommandSender,
        unitType: UnitType,
        @GTE(1) @LTE(20) count: Int = 1,
        team: Team = sender.player.team(),
    ) {
        runOnMindustryThread {
            var validSpawnedUnits = 0

            repeat(count) {
                val unit = unitType.spawn(sender.player, team)

                if (unit.isValid)
                    validSpawnedUnits += 1
            }

            sender.sendSuccess(
                "Spawned $validSpawnedUnits '${unitType.name}' at (${floor(sender.player.x / Vars.tilesize).toInt()}, ${
                    floor(sender.player.y / Vars.tilesize).toInt()
                })"
            )
        }
    }

    @Command(["gameover"])
    @Admin
    suspend fun gameover(sender: CommandSender, winner: Team = Team.derelict) {
        Genesis.getHandler<PlagueHandler>()?.restart(winner)
    }
}