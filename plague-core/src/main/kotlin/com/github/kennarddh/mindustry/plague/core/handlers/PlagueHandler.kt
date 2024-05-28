package com.github.kennarddh.mindustry.plague.core.handlers

import arc.math.Mathf
import arc.util.Timer
import com.github.kennarddh.mindustry.genesis.core.commands.annotations.Command
import com.github.kennarddh.mindustry.genesis.core.commands.senders.CommandSender
import com.github.kennarddh.mindustry.genesis.core.commands.senders.PlayerCommandSender
import com.github.kennarddh.mindustry.genesis.core.commons.CoroutineScopes
import com.github.kennarddh.mindustry.genesis.core.commons.priority.Priority
import com.github.kennarddh.mindustry.genesis.core.commons.runOnMindustryThread
import com.github.kennarddh.mindustry.genesis.core.events.annotations.EventHandler
import com.github.kennarddh.mindustry.genesis.core.filters.FilterType
import com.github.kennarddh.mindustry.genesis.core.filters.annotations.Filter
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.genesis.standard.extensions.setRules
import com.github.kennarddh.mindustry.plague.core.commons.PlagueBanned
import com.github.kennarddh.mindustry.plague.core.commons.PlagueRules
import com.github.kennarddh.mindustry.plague.core.commons.PlagueState
import com.github.kennarddh.mindustry.plague.core.commons.PlagueVars
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock
import mindustry.Vars
import mindustry.content.Blocks
import mindustry.content.Items
import mindustry.content.UnitTypes
import mindustry.core.NetServer
import mindustry.game.EventType
import mindustry.game.Team
import mindustry.gen.Call
import mindustry.gen.Groups
import mindustry.gen.Iconc
import mindustry.gen.Player
import mindustry.net.Administration
import mindustry.type.ItemStack
import mindustry.world.Block
import mindustry.world.Tile
import mindustry.world.blocks.storage.CoreBlock.CoreBuild
import kotlin.time.Duration.Companion.minutes


class PlagueHandler : Handler {
    val monoReward = listOf(ItemStack(Items.copper, 300), ItemStack(Items.lead, 300))

    private val timers = mutableSetOf<Timer.Task>()

    @Filter(FilterType.Action, Priority.High)
    fun powerSourceActionFilter(action: Administration.PlayerAction): Boolean {
        if (!(action.type == Administration.ActionType.breakBlock || action.type == Administration.ActionType.pickupBlock)) return true

        if (action.block != Blocks.powerSource) return true

        return false
    }

    @Filter(FilterType.Action, Priority.High)
    suspend fun buildBlockActionFilter(action: Administration.PlayerAction): Boolean {
        if (action.player == null) return true

        if (action.player.team() == Team.blue) return true

        if (action.block == null) return true

        PlagueVars.stateLock.withLock {
            if (action.player.team() == Team.malis) {
                if (PlagueBanned.getCurrentPlagueBannedBlocks(true).contains(action.block)) {
                    return false
                }
            } else {
                if (PlagueBanned.getCurrentSurvivorsBannedBlocks().contains(action.block)) {
                    return false
                }
            }
        }

        return true
    }

    @Filter(FilterType.Action, Priority.High)
    fun respawnActionFilter(action: Administration.PlayerAction): Boolean {
        if (action.type != Administration.ActionType.respawn) return true

        if (action.player.team() == Team.blue) return false

        return true
    }

    /**
     * Player has their own rules
     * This desync the player's rules with server's rules but this is fine because only the banned blocks and units are changed
     * This is not safe by itself because with customized client or mod rules can be easily bypassed and because the server doesn't ban the block in the rules player can just build the block.
     * To prevent this action filter is used to check if player is allowed to build or create unit
     */
    suspend fun updatePlayerSpecificRules(player: Player) {
        if (player.team() == Team.blue) return

        val playerRules = Vars.state.rules.copy()

        if (player.team() == Team.malis) {
            PlagueBanned.getCurrentPlagueBannedBlocks().forEach {
                playerRules.bannedBlocks.add(it)
            }

            PlagueBanned.getCurrentPlagueBannedUnits().forEach {
                playerRules.bannedUnits.add(it)
            }
        } else {
            PlagueBanned.getCurrentSurvivorsBannedBlocks().forEach {
                playerRules.bannedBlocks.add(it)
            }

            PlagueBanned.getCurrentSurvivorsBannedUnits().forEach {
                playerRules.bannedUnits.add(it)
            }
        }

        player.setRules(playerRules)
    }

    suspend fun updateAllPlayerSpecificRules() {
        Groups.player.forEach {
            updatePlayerSpecificRules(it)
        }
    }

    suspend fun changePlayerTeam(player: Player, team: Team) {
        player.team(team)

        updatePlayerSpecificRules(player)
    }

    @Command(["plague"])
    fun plague(sender: PlayerCommandSender) {
        runOnMindustryThread {
            runBlocking {
                changePlayerTeam(sender.player, Team.malis)

                sender.sendSuccess("You are now in plague team.")
            }
        }
    }

    fun getNewEmptyTeam(): Team? {
        return Team.all.find {
            // Non default team and not active.
            it.id > 6 && !it.active()
        }
    }

    fun validPlace(block: Block, tile: Tile): Boolean {
        val offsetX: Int = -(block.size - 1) / 2
        val offsetY: Int = -(block.size - 1) / 2

        for (dx in 0..<block.size) {
            for (dy in 0..<block.size) {
                val wx = dx + offsetX + tile.x
                val wy = dy + offsetY + tile.y

                val checkTile = Vars.world.tile(wx, wy)

                if (
                // Void tile
                    checkTile == null ||
                    // Tile with block
                    checkTile.build != null ||
                    // Deep water
                    checkTile.floor().isDeep ||
                    // Exactly same block
                    (block == checkTile.block() && checkTile.build != null && block.rotate) ||
                    !checkTile.floor().placeableOn
                )
                    return false
            }
        }

        return true
    }

    fun getClosestEnemyCore(team: Team, x: Float, y: Float, maxDistance: Float = Float.MAX_VALUE): CoreBuild? {
        var closest: CoreBuild? = null
        var closestDistance = Float.MAX_VALUE

        for (activeTeam in Vars.state.teams.active) {
            if (activeTeam == team) continue

            for (core in activeTeam.cores) {
                val distance = Mathf.dst(x, y, core.x, core.y)

                if (distance > maxDistance) continue

                if (closestDistance > distance) {
                    closest = core

                    closestDistance = distance
                }
            }
        }

        return closest
    }

    @EventHandler
    suspend fun createSurvivorCoreEventHandler(event: EventType.BuildSelectEvent) {
        PlagueVars.stateLock.withLock {
            if (PlagueVars.state != PlagueState.Prepare) return
        }

        if (event.builder.player == null) return
        if (event.builder.team() != Team.blue) return
        if (event.breaking) return

        runOnMindustryThread {
            event.tile.removeNet()

            if (!validPlace(Blocks.coreShard, event.tile))
                return@runOnMindustryThread event.builder.player.sendMessage("[scarlet]Invalid core position.")

            for (core in Team.malis.cores()) {
                if (core.dst(event.tile) < 100 * Vars.tilesize)
                    return@runOnMindustryThread event.builder.player.sendMessage("[scarlet]Core must be at least 100 tiles away from nearest plague's core.")
            }

            val closestEnemyCoreInRange = getClosestEnemyCore(
                Team.derelict,
                event.tile.x.toFloat() * Vars.tilesize,
                event.tile.y.toFloat() * Vars.tilesize,
                50f * Vars.tilesize
            )

            if (closestEnemyCoreInRange != null) {
                // Join closest survivor core team
                runBlocking {
                    changePlayerTeam(event.builder.player, closestEnemyCoreInRange.team)
                }

                event.tile.setNet(Blocks.coreShard, closestEnemyCoreInRange.team, 0)

                Vars.state.teams.registerCore(event.tile.build as CoreBuild)
            } else {
                // Create new team
                val newTeam = getNewEmptyTeam()
                    ?: return@runOnMindustryThread event.builder.player.sendMessage("[scarlet]No available team.")

                runBlocking {
                    changePlayerTeam(event.builder.player, newTeam)
                }

                event.tile.setNet(Blocks.coreShard, newTeam, 0)

                Vars.state.teams.registerCore(event.tile.build as CoreBuild)

                Vars.state.rules.loadout.forEach {
                    newTeam.core().items().add(it.item, it.amount.coerceAtMost(newTeam.core().storageCapacity))
                }
            }
        }
    }

    @EventHandler
    suspend fun onPlay(event: EventType.PlayEvent) {
        PlagueVars.stateLock.withLock {
            PlagueVars.state = PlagueState.Prepare
        }

        runOnMindustryThread {
            // Reset rules
            Vars.state.rules = PlagueRules.initRules(Vars.state.rules)

            Call.setRules(Vars.state.rules)

            Team.malis.core().items().clear()
        }

        Timer.schedule({
            CoroutineScopes.Main.launch {
                onFirstPhase()

                Timer.schedule({
                    CoroutineScopes.Main.launch {
                        onSecondPhase()
                    }

                    Timer.schedule({
                        CoroutineScopes.Main.launch {
                            onEnded()
                        }
                    }, 15.minutes.inWholeSeconds.toFloat()).run {
                        timers.add(this)
                    }
                }, 45.minutes.inWholeSeconds.toFloat()).run {
                    timers.add(this)
                }
            }
        }, 2.minutes.inWholeSeconds.toFloat()).run {
            timers.add(this)
        }
    }

    @EventHandler
    fun onGameOver(event: EventType.GameOverEvent) {
        timers.forEach {
            it.cancel()
        }
    }

    fun spawnPlayerUnit(
        player: Player,
        team: Team,
        x: Float = Vars.state.map.width / 2f * Vars.tilesize,
        y: Float = Vars.state.map.height / 2f * Vars.tilesize
    ) {
        val unit = UnitTypes.gamma.create(team)

        player.set(x, y)

        unit.set(x, y)
        unit.rotation(90.0f)
        unit.impulse(0.0f, 3.0f)
        unit.spawnedByCore(true)
        unit.controller(player)
        unit.add()
    }

    @EventHandler
    fun onPlayerJoin(event: EventType.PlayerJoin) {
        runOnMindustryThread {
            runBlocking {
                if (event.player.team() == Team.blue) {
                    spawnPlayerUnit(event.player, Team.blue)

                    updatePlayerSpecificRules(event.player)
                }
            }
        }
    }

    @Command(["state"])
    suspend fun getState(sender: CommandSender) {
        PlagueVars.stateLock.withLock {
            sender.sendSuccess(PlagueVars.state.name)
        }
    }

    override suspend fun onInit() {
        runOnMindustryThread {
            Vars.netServer.assigner = NetServer.TeamAssigner { _, _ ->
                val team = runBlocking {
                    PlagueVars.stateLock.withLock {
                        if (PlagueVars.state == PlagueState.Prepare) return@runBlocking Team.blue
                    }

                    Team.malis
                }

                team
            }
        }
    }

    suspend fun onFirstPhase() {
        PlagueVars.stateLock.withLock {
            PlagueVars.state = PlagueState.PlayingFirstPhase
        }

        runOnMindustryThread {
            Vars.state.rules.enemyCoreBuildRadius = Vars.state.map.rules().enemyCoreBuildRadius

            Call.setRules(Vars.state.rules)

            runBlocking {
                updateAllPlayerSpecificRules()
            }

            // Move every no team player to plague team
            Groups.player.filter { it.team() === Team.blue }.forEach {
                runBlocking {
                    changePlayerTeam(it, Team.malis)
                }
            }
        }
    }

    // 45 minutes after onPlay move to second phase.
    suspend fun onSecondPhase() {
        PlagueVars.stateLock.withLock {
            PlagueVars.state = PlagueState.PlayingSecondPhase
        }

        runOnMindustryThread {
            runBlocking {
                updateAllPlayerSpecificRules()
            }
        }
    }

    // 15 minutes after onSecondPhase move to ended.
    suspend fun onEnded() {
        PlagueVars.stateLock.withLock {
            PlagueVars.state = PlagueState.Ended
        }

        runOnMindustryThread {
            runBlocking {
                updateAllPlayerSpecificRules()
            }
        }
    }

    @EventHandler
    fun onMonoUnitCreate(event: EventType.UnitCreateEvent) {
        if (event.unit.type != UnitTypes.mono) return

        if (event.unit.team.core() == null) return

        runOnMindustryThread {
            Call.label(
                "${Iconc.unitMono} created",
                5f,
                event.spawner.x,
                event.spawner.y
            )

            // .kill() instantly kill the unit makes it weird because the unit just disappear
            event.unit.health = 0f
            event.unit.dead = true

            event.unit.team.core().items().add(monoReward)
        }
    }
}