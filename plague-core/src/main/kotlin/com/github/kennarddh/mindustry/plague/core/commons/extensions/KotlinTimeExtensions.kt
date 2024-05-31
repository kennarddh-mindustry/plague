package com.github.kennarddh.mindustry.plague.core.commons.extensions

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun Duration.toDisplayString(): String = inWholeSeconds.seconds.toString()
fun Duration.toMinutesDisplayString(): String = inWholeMinutes.minutes.toString()
