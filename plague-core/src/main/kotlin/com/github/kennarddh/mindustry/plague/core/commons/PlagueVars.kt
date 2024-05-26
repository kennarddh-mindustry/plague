package com.github.kennarddh.mindustry.plague.core.commons

import kotlinx.coroutines.sync.Mutex

object PlagueVars {
    val port: Int
        get() = System.getenv("PORT")?.toInt() ?: 6567


    var state: PlagueState = PlagueState.Prepare

    val stateLock = Mutex()
}