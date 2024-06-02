package com.github.kennarddh.mindustry.plague.core.commons

import com.github.kennarddh.mindustry.plague.core.commons.extensions.sumOf
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

enum class PlagueState(val displayName: String, val duration: Duration) {
    Prepare("Prepare", 2.minutes),
    PlayingFirstPhase("Playing First Phase", 45.minutes),
    PlayingSecondPhase("Playing Second Phase", 15.minutes),
    Ended("Ended", Duration.ZERO),
    GameOver("Game Over", Duration.ZERO);

    val startTime: Duration get() = entries.filter { it <= this }.sumOf { it.duration }
}