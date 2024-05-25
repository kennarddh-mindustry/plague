package com.github.kennarddh.mindustry.plague.core.commons

object PlagueVars {
    val port: Int
        get() = System.getenv("PORT")?.toInt() ?: 6567
}