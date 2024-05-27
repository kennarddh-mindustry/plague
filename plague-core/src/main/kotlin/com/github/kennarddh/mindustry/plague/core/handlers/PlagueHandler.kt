package com.github.kennarddh.mindustry.plague.core.handlers

import com.github.kennarddh.mindustry.genesis.core.commons.priority.Priority
import com.github.kennarddh.mindustry.genesis.core.commons.runOnMindustryThread
import com.github.kennarddh.mindustry.genesis.core.events.annotations.EventHandler
import com.github.kennarddh.mindustry.genesis.core.filters.FilterType
import com.github.kennarddh.mindustry.genesis.core.filters.annotations.Filter
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.genesis.core.timers.annotations.TimerTask
import com.github.kennarddh.mindustry.plague.core.commons.PlagueBanned
import com.github.kennarddh.mindustry.plague.core.commons.PlagueState
import com.github.kennarddh.mindustry.plague.core.commons.PlagueVars
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock
import mindustry.Vars
import mindustry.content.Blocks
import mindustry.core.NetServer
import mindustry.game.EventType
import mindustry.game.Team
import mindustry.gen.Groups
import mindustry.net.Administration

class PlagueHandler : Handler {
    @Filter(FilterType.Action, Priority.High)
    suspend fun actionFilter(action: Administration.PlayerAction): Boolean {
        PlagueVars.stateLock.withLock {
            if (action.block != null && action.block == Blocks.powerSource) return false

            if (action.unit == null) return true

            if (action.unit.team() == Team.blue) return false

            if (PlagueVars.state === PlagueState.Prepare) return false

            if (action.unit.team() == Team.malis) {
                if (action.block != null) {
                    if (PlagueBanned.getCurrentPlagueBannedBlocks(true).contains(action.block)) {
                        return false
                    }
                }
            } else {
                if (action.block != null) {
                    if (PlagueBanned.getCurrentSurvivorsBannedBlocks().contains(action.block)) {
                        return false
                    }
                }
            }

            return true
        }
    }

    fun getNewEmptyTeam(): Team? {
        return Team.all.find {
            // Non default team and not active.
            it.id > 5 && !it.active()
        }
    }

    @EventHandler
    suspend fun onTap(event: EventType.TapEvent) {
        PlagueVars.stateLock.withLock {
            if (PlagueVars.state != PlagueState.Prepare) return
        }

        runOnMindustryThread {
            if (event.player.team() == Team.blue) {
                val newTeam = getNewEmptyTeam()
                    ?: return@runOnMindustryThread event.player.sendMessage("[scarlet]No available team.")

                event.player.team(newTeam)

                event.tile.setNet(Blocks.coreShard, newTeam, 0)
            }
        }
    }

    override suspend fun onInit() {
        Vars.netServer.assigner = NetServer.TeamAssigner { _, _ ->
            runBlocking {
                PlagueVars.stateLock.withLock {
                    if (PlagueVars.state == PlagueState.Prepare) Team.blue
                }

                Team.malis
            }
        }
    }

    @TimerTask(120f)
    suspend fun onStart() {
        PlagueVars.stateLock.withLock {
            PlagueVars.state = PlagueState.PlayingFirstPhase
        }

        runOnMindustryThread {
            // Move every no team player to plague team
            Groups.player.filter { it.team() === Team.blue }.forEach {
                it.team(Team.malis)
            }

            // Remove all blue core
            Team.blue.cores().forEach { it.kill() }
        }
    }

    // 45 minutes after onPlay move to second phase.
    @TimerTask(120f + 45 * 60)
    suspend fun onSecondPhase() {
        PlagueVars.stateLock.withLock {
            PlagueVars.state = PlagueState.PlayingSecondPhase
        }
    }

    // 15 minutes after onSecondPhase move to ended.
    @TimerTask(120f + 45 * 60 + 15 * 60)
    suspend fun onEnded() {
        PlagueVars.stateLock.withLock {
            PlagueVars.state = PlagueState.Ended
        }
    }
}