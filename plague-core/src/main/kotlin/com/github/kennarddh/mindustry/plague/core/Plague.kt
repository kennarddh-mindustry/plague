package com.github.kennarddh.mindustry.plague.core

import com.github.kennarddh.mindustry.genesis.core.Genesis
import com.github.kennarddh.mindustry.genesis.core.commons.AbstractPlugin
import com.github.kennarddh.mindustry.plague.core.commands.validations.Admin
import com.github.kennarddh.mindustry.plague.core.commands.validations.validateAdmin
import com.github.kennarddh.mindustry.plague.core.commons.Logger
import com.github.kennarddh.mindustry.plague.core.handlers.PlagueHandler
import com.github.kennarddh.mindustry.plague.core.handlers.ServerPresenceHandler
import com.github.kennarddh.mindustry.plague.core.handlers.StartHandler
import com.github.kennarddh.mindustry.plague.core.handlers.WelcomeHandler

@Suppress("unused")
class Plague : AbstractPlugin() {
    override suspend fun onInit() {
        Logger.info("Registering handlers")

        Genesis.commandRegistry.registerCommandValidationAnnotation(Admin::class, ::validateAdmin)

        Genesis.registerHandler(PlagueHandler())

        Genesis.registerHandler(StartHandler())

        Genesis.registerHandler(ServerPresenceHandler())
        Genesis.registerHandler(WelcomeHandler())

        Logger.info("Loaded")
    }
}