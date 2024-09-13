package com.github.kennarddh.mindustry.plague.core.commons

import mindustry.ai.types.FlyingAI

class PlagueAttackerFlyingAI : FlyingAI() {
    override fun isLogicControllable(): Boolean {
        return false
    }
}