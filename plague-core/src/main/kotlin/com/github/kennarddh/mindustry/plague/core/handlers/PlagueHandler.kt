package com.github.kennarddh.mindustry.plague.core.handlers

import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.genesis.core.timers.annotations.TimerTask
import com.github.kennarddh.mindustry.plague.core.commons.PlagueState
import com.github.kennarddh.mindustry.plague.core.commons.PlagueVars
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock
import mindustry.Vars
import mindustry.core.NetServer
import mindustry.game.Team

class PlagueHandler : Handler {
    override suspend fun onInit() {
        Vars.netServer.assigner = NetServer.TeamAssigner { player, players ->
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