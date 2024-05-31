package com.github.kennarddh.mindustry.plague.core.commands.validations

import com.github.kennarddh.mindustry.genesis.core.commands.annotations.validations.CommandValidation
import com.github.kennarddh.mindustry.genesis.core.commands.annotations.validations.CommandValidationDescription
import mindustry.gen.Player


fun validateAdmin(annotation: Annotation, player: Player?): Boolean {
    if (player == null) {
        // On server console
        return true
    }

    return player.admin
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@CommandValidation
@CommandValidationDescription("You must be admin to use ':commandName:'.")
annotation class Admin
