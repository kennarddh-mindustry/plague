package com.github.kennarddh.mindustry.plague.core.commons

import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import mindustry.content.Items
import mindustry.gen.Player
import mindustry.type.ItemStack
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object PlagueVars {
    val port: Int
        get() = System.getenv("PORT")?.toInt() ?: 6567

    val monoReward = listOf(ItemStack(Items.copper, 300), ItemStack(Items.lead, 300))
    val newCoreCost = listOf(ItemStack(Items.thorium, 1000))

    var state: PlagueState = PlagueState.Prepare

    val stateLock = Mutex()

    lateinit var mapStartTime: Instant
    var totalMapSkipDuration: Duration = 0.seconds

    val mapTime: Duration
        get() {
            if (::mapStartTime.isInitialized)
                return Clock.System.now() - mapStartTime + totalMapSkipDuration

            return totalMapSkipDuration
        }

    val playersWithDisabledHUD: MutableSet<Player> = Collections.synchronizedSet(mutableSetOf<Player>())

    /**
     * If this is true mono tree units made pre second phase will have their weapons restored when second phase.
     */
    val restorePreSecondPhaseMonoTreeUnitsWeapons: Boolean = false

    val survivorsMinBuildRangeFromPlagueCoreInTiles: Int = 100
    val newSurvivorCoreMinDistanceFromPlagueCoreInTiles: Int = 70
    val survivorCoreMaxJoinDistanceInTiles: Int = 120
}