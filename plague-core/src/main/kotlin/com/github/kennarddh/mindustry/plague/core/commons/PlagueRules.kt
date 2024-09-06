package com.github.kennarddh.mindustry.plague.core.commons

import com.github.kennarddh.mindustry.plague.core.handlers.PlagueHandler
import mindustry.Vars
import mindustry.game.Rules
import mindustry.game.Team

object PlagueRules {
    fun initRules(baseRules: Rules? = null): Rules {
        val rules = if (baseRules == null) Rules() else baseRules.copy()

        rules.canGameOver = false
        rules.hideBannedBlocks = true
        rules.enemyCoreBuildRadius = 70f * Vars.tilesize
        rules.damageExplosions = true
        rules.reactorExplosions = false

        Team.all.filter { PlagueHandler.isValidSurvivorTeam(it) }.forEach {
            rules.teams[it].unitCrashDamageMultiplier = 0f
        }

        rules.modeName = "Plague"

        rules.hideBannedBlocks = true
        rules.unitWhitelist = false
        rules.blockWhitelist = false
        rules.unitCapVariable = false

        if (Vars.state.map == null) {
            rules.unitCap = 48
        } else {
            rules.unitCap = Vars.state.map.rules().unitCap.coerceAtMost(40)
        }

        return rules
    }
}