package com.github.kennarddh.mindustry.plague.core.handlers

import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.plague.core.commons.Logger
import mindustry.net.Administration.Config

class ServerPresenceHandler : Handler {
    override suspend fun onInit() {
        Config.serverName.set("[red]A[yellow]L[teal]E[blue]X [white]| PLAGUE [yellow](BETA)")
        Config.desc.set("classic [forest]plague")

        Logger.info("Server presence done")
    }
}