package com.github.kennarddh.mindustry.plague.core.handlers

import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.genesis.core.timers.annotations.TimerTask
import com.github.kennarddh.mindustry.plague.core.commons.PlagueState
import com.github.kennarddh.mindustry.plague.core.commons.PlagueVars
import kotlinx.coroutines.sync.withLock

class PlagueHandler : Handler {
    @TimerTask(120f)
    suspend fun onStart() {
        PlagueVars.stateLock.withLock {
            PlagueVars.state = PlagueState.PlayingFirstPhase
        }
    }
}