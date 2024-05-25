package com.github.kennarddh.mindustry.plague.core.handlers

import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.plague.core.commons.extensions.Logger
import mindustry.net.Administration.Config

class ServerPresenceHandler : Handler {
    override suspend fun onInit() {
        Config.serverName.set("Plague")
        Config.desc.set("Plague")

        Logger.info("Server presence done")
    }
}