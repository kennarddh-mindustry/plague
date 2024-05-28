package com.github.kennarddh.mindustry.plague.core.commons

import kotlinx.coroutines.sync.withLock
import mindustry.content.Blocks
import mindustry.content.UnitTypes
import mindustry.type.UnitType
import mindustry.world.Block

object PlagueBanned {
    val serpuloUnitConstructorBlocks: Set<Block> = setOf(
        Blocks.groundFactory,
        Blocks.airFactory,
        Blocks.navalFactory,
        Blocks.additiveReconstructor,
        Blocks.multiplicativeReconstructor,
        Blocks.exponentialReconstructor,
        Blocks.tetrativeReconstructor,
    )

    val erekirUnitConstructorBlocks: Set<Block> = setOf(
        Blocks.shipAssembler,
        Blocks.mechAssembler,
        Blocks.tankAssembler,
        Blocks.tankFabricator,
        Blocks.shipFabricator,
        Blocks.mechFabricator,
        Blocks.tankRefabricator,
        Blocks.mechRefabricator,
        Blocks.shipRefabricator,
        Blocks.primeRefabricator
    )

    val unitConstructorBlocks = serpuloUnitConstructorBlocks + erekirUnitConstructorBlocks

    val survivorBannedBlocks: Set<Block> =
        unitConstructorBlocks - setOf(Blocks.airFactory, Blocks.additiveReconstructor)

    val plagueBannedBlocks: Set<Block> = unitConstructorBlocks -
            setOf(Blocks.airFactory, Blocks.additiveReconstructor) +
            setOf(
                // Serpulo walls
                Blocks.surgeWall,
                Blocks.surgeWallLarge,
                Blocks.thoriumWall,
                Blocks.thoriumWallLarge,
                Blocks.phaseWall,
                Blocks.phaseWallLarge,
                Blocks.titaniumWall,
                Blocks.titaniumWallLarge,
                Blocks.copperWallLarge,
                Blocks.copperWall,
                Blocks.door,
                Blocks.doorLarge,
                Blocks.plastaniumWall,
                Blocks.plastaniumWallLarge,

                // Erekir walls
                Blocks.berylliumWall,
                Blocks.berylliumWallLarge,
                Blocks.tungstenWall,
                Blocks.tungstenWallLarge,
                Blocks.blastDoor,
                Blocks.reinforcedSurgeWall,
                Blocks.reinforcedSurgeWallLarge,
                Blocks.carbideWall,
                Blocks.carbideWallLarge,
                Blocks.shieldedWall,

                // Serpulo power generators
                Blocks.combustionGenerator,
                Blocks.thermalGenerator,
                Blocks.steamGenerator,
                Blocks.differentialGenerator,
                Blocks.rtgGenerator,
                Blocks.solarPanel,
                Blocks.largeSolarPanel,
                Blocks.thoriumReactor,
                Blocks.impactReactor,

                Blocks.battery,
                Blocks.batteryLarge,

                // Erekir power generators
                Blocks.turbineCondenser,
                Blocks.chemicalCombustionChamber,
                Blocks.fluxReactor,
                Blocks.neoplasiaReactor
            )

    val alwaysAllowedUnits: Set<UnitType> = setOf(
        UnitTypes.mono,
        UnitTypes.poly,
        UnitTypes.flare
    )

    val serpuloGroundUnits: Set<UnitType> = setOf(
        // Dagger tree
        UnitTypes.dagger,
        UnitTypes.mace,
        UnitTypes.fortress,
        UnitTypes.scepter,
        UnitTypes.reign,

        // Crawler tree
        UnitTypes.crawler,
        UnitTypes.atrax,
        UnitTypes.spiroct,
        UnitTypes.arkyid,
        UnitTypes.toxopid,

        // Nova tree
        UnitTypes.nova,
        UnitTypes.pulsar,
        UnitTypes.quasar,
        UnitTypes.vela,
        UnitTypes.corvus,
    )

    val serpuloAirUnits: Set<UnitType> = setOf(
        // Mono tree
        UnitTypes.mono,
        UnitTypes.poly,
        UnitTypes.mega,
        UnitTypes.quad,
        UnitTypes.oct,

        // Flare tree
        UnitTypes.flare,
        UnitTypes.horizon,
        UnitTypes.zenith,
        UnitTypes.antumbra,
        UnitTypes.eclipse,
    )

    val serpuloNavalUnits: Set<UnitType> = setOf(
        // Risso tree
        UnitTypes.risso,
        UnitTypes.minke,
        UnitTypes.bryde,
        UnitTypes.sei,
        UnitTypes.omura,

        // Retusa tree
        UnitTypes.retusa,
        UnitTypes.oxynoe,
        UnitTypes.cyerce,
        UnitTypes.aegires,
        UnitTypes.navanax,
    )

    val serpuloUnits = serpuloGroundUnits + serpuloAirUnits + serpuloNavalUnits

    val erekirTankUnits: Set<UnitType> = setOf(
        UnitTypes.stell,
        UnitTypes.locus,
        UnitTypes.precept,
        UnitTypes.vanquish,
        UnitTypes.conquer,
    )

    val erekirShipUnits: Set<UnitType> = setOf(
        UnitTypes.elude,
        UnitTypes.avert,
        UnitTypes.obviate,
        UnitTypes.quell,
        UnitTypes.disrupt,
    )

    val erekirMechUnits: Set<UnitType> = setOf(
        UnitTypes.merui,
        UnitTypes.cleroi,
        UnitTypes.anthicus,
        UnitTypes.tecta,
        UnitTypes.collaris,
    )

    val erekirUnits = erekirTankUnits + erekirShipUnits + erekirMechUnits

    val allUnits = serpuloUnits + erekirUnits

    suspend fun getCurrentPlagueBannedUnits(inStateLock: Boolean = false): Set<UnitType> {
        val innerMethod = { state: PlagueState ->
            when (state) {
                PlagueState.Prepare,
                PlagueState.PlayingFirstPhase -> allUnits - alwaysAllowedUnits - serpuloGroundUnits - serpuloNavalUnits - erekirTankUnits - erekirMechUnits

                PlagueState.PlayingSecondPhase,
                PlagueState.Ended -> allUnits - alwaysAllowedUnits - serpuloGroundUnits - serpuloNavalUnits - serpuloAirUnits - erekirTankUnits - erekirShipUnits - erekirMechUnits
            }
        }

        return if (inStateLock) {
            innerMethod(PlagueVars.state)
        } else {
            PlagueVars.stateLock.withLock {
                innerMethod(PlagueVars.state)
            }
        }
    }

    suspend fun getCurrentSurvivorsBannedUnits(inStateLock: Boolean = false): Set<UnitType> {
        val innerMethod = { state: PlagueState ->
            when (state) {
                PlagueState.Prepare,
                PlagueState.PlayingFirstPhase,
                PlagueState.PlayingSecondPhase,
                PlagueState.Ended -> allUnits - alwaysAllowedUnits
            }
        }

        return if (inStateLock) {
            innerMethod(PlagueVars.state)
        } else {
            PlagueVars.stateLock.withLock {
                innerMethod(PlagueVars.state)
            }
        }
    }


    suspend fun getCurrentPlagueBannedBlocks(inStateLock: Boolean = false): Set<Block> {
        val innerMethod = { state: PlagueState ->
            when (state) {
                PlagueState.Prepare -> plagueBannedBlocks

                PlagueState.PlayingFirstPhase,
                PlagueState.PlayingSecondPhase,
                PlagueState.Ended -> plagueBannedBlocks - unitConstructorBlocks
            }
        }

        return if (inStateLock) {
            innerMethod(PlagueVars.state)
        } else {
            PlagueVars.stateLock.withLock {
                innerMethod(PlagueVars.state)
            }
        }
    }

    fun getCurrentSurvivorsBannedBlocks(): Set<Block> = survivorBannedBlocks
}