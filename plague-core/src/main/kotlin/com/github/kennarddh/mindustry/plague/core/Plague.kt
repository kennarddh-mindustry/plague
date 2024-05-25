package com.github.kennarddh.mindustry.plague.core

import com.github.kennarddh.mindustry.genesis.core.Genesis
import com.github.kennarddh.mindustry.genesis.core.commons.AbstractPlugin
import com.github.kennarddh.mindustry.plague.core.commons.extensions.Logger
import com.github.kennarddh.mindustry.plague.core.handlers.PlagueHandler
import com.github.kennarddh.mindustry.plague.core.handlers.ServerPresenceHandler
import com.github.kennarddh.mindustry.plague.core.handlers.StartHandler
import com.github.kennarddh.mindustry.plague.core.handlers.WelcomeHandler

@Suppress("unused")
class Plague : AbstractPlugin() {
    override suspend fun onInit() {
        Logger.info("Registering handlers")

        Genesis.registerHandler(PlagueHandler())

        Genesis.registerHandler(StartHandler())

        Genesis.registerHandler(ServerPresenceHandler())
        Genesis.registerHandler(WelcomeHandler())

        Logger.info("Loaded")
    }
}