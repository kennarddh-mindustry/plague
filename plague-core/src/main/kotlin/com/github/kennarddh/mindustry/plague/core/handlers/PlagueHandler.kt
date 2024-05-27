package com.github.kennarddh.mindustry.plague.core.handlers

import arc.util.Timer
import com.github.kennarddh.mindustry.genesis.core.commands.annotations.Command
import com.github.kennarddh.mindustry.genesis.core.commands.senders.CommandSender
import com.github.kennarddh.mindustry.genesis.core.commons.CoroutineScopes
import com.github.kennarddh.mindustry.genesis.core.commons.priority.Priority
import com.github.kennarddh.mindustry.genesis.core.commons.runOnMindustryThread
import com.github.kennarddh.mindustry.genesis.core.events.annotations.EventHandler
import com.github.kennarddh.mindustry.genesis.core.filters.FilterType
import com.github.kennarddh.mindustry.genesis.core.filters.annotations.Filter
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.plague.core.commons.PlagueBanned
import com.github.kennarddh.mindustry.plague.core.commons.PlagueState
import com.github.kennarddh.mindustry.plague.core.commons.PlagueVars
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock
import mindustry.Vars
import mindustry.content.Blocks
import mindustry.core.NetServer
import mindustry.game.EventType
import mindustry.game.Team
import mindustry.gen.Groups
import mindustry.net.Administration
import kotlin.time.Duration.Companion.minutes


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

    private val timers = mutableSetOf<Timer.Task>()

    @EventHandler
    suspend fun onPlay(event: EventType.PlayEvent) {
        PlagueVars.stateLock.withLock {
            PlagueVars.state = PlagueState.Prepare
        }

        Timer.schedule({
            CoroutineScopes.Main.launch {
                onFirstPhase()

                Timer.schedule({
                    CoroutineScopes.Main.launch {
                        onSecondPhase()
                    }

                    Timer.schedule({
                        CoroutineScopes.Main.launch {
                            onEnded()
                        }
                    }, 15.minutes.inWholeSeconds.toFloat()).run {
                        timers.add(this)
                    }
                }, 45.minutes.inWholeSeconds.toFloat()).run {
                    timers.add(this)
                }
            }
        }, 2.minutes.inWholeSeconds.toFloat()).run {
            timers.add(this)
        }
    }

    @EventHandler
    fun onGameOver(event: EventType.GameOverEvent) {
        timers.forEach {
            it.cancel()
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

    @Command(["state"])
    suspend fun getState(sender: CommandSender) {
        PlagueVars.stateLock.withLock {
            sender.sendSuccess(PlagueVars.state.name)
        }
    }

    override suspend fun onInit() {
        runOnMindustryThread {
            Vars.netServer.assigner = NetServer.TeamAssigner { _, _ ->
                val team = runBlocking {
                    PlagueVars.stateLock.withLock {
                        if (PlagueVars.state == PlagueState.Prepare) return@runBlocking Team.blue
                    }

                    Team.malis
                }

                team
            }
        }
    }

    suspend fun onFirstPhase() {
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
    suspend fun onSecondPhase() {
        PlagueVars.stateLock.withLock {
            PlagueVars.state = PlagueState.PlayingSecondPhase
        }
    }

    // 15 minutes after onSecondPhase move to ended.
    suspend fun onEnded() {
        PlagueVars.stateLock.withLock {
            PlagueVars.state = PlagueState.Ended
        }
    }
}