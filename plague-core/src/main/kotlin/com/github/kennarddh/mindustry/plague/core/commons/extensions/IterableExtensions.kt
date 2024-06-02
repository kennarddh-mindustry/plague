package com.github.kennarddh.mindustry.plague.core.commons.extensions

import kotlin.time.Duration

inline fun <T> Iterable<T>.sumOf(selector: (T) -> Duration): Duration {
    var sum = Duration.ZERO

    for (element in this) {
        sum += selector(element)
    }

    return sum
}