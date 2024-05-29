package com.github.kennarddh.mindustry.plague.core.commons

import arc.struct.Seq
import mindustry.Vars
import mindustry.content.UnitTypes
import mindustry.game.Rules
import mindustry.game.Team

object PlagueRules {
    fun initRules(baseRules: Rules? = null): Rules {
        val rules = if (baseRules == null) Rules() else baseRules.copy()

        rules.canGameOver = false
        rules.hideBannedBlocks = true
        rules.enemyCoreBuildRadius = 0f


        Team.all.filter { it != Team.malis }.forEach {
            rules.teams[it].unitCrashDamageMultiplier = 0f
        }

        rules.modeName = "Plague"

        rules.hideBannedBlocks = true
        rules.unitWhitelist = false
        rules.blockWhitelist = false
        rules.reactorExplosions = false
        rules.unitCapVariable = false

        if (Vars.state.map == null) {
            rules.unitCap = 48
        } else {
            rules.unitCap = Vars.state.map.rules().unitCap.coerceAtMost(40)
        }

        return rules
    }

    fun removeUnitsWeapons() {
        // Remove units weapon so they have 0 damage.
        // This is a slight desync but not noticeable because on client they will see they can shoot but with 0 damage.
        UnitTypes.alpha.weapons = Seq()
        UnitTypes.beta.weapons = Seq()
        UnitTypes.gamma.weapons = Seq()
        UnitTypes.poly.weapons = Seq()
        UnitTypes.mega.weapons = Seq()
        UnitTypes.flare.weapons = Seq()
    }
}