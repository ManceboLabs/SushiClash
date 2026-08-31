package com.mancebolabs.sushiclash.ui.components.character

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.domain.model.ChefEventAnimation

object SushiClashCharacterAnimations {
    @RawRes
    val Celebration: Int = R.raw.chef_celebration

    @RawRes
    val GameStart: Int = R.raw.chef_inicio

    @RawRes
    val GameFinish: Int = R.raw.chef_final

    @RawRes
    val OnboardingGreeting: Int = R.raw.chef_saludo

    @RawRes
    val OnboardingTutorial: Int = R.raw.chef_tutorial

    val RandomEventPool: List<ChefEventAnimation> = ChefEventAnimation.entries

    @RawRes
    fun rawResIdFor(animation: ChefEventAnimation): Int {
        return when (animation) {
            ChefEventAnimation.DEVOURING -> R.raw.chef_event_devouring
            ChefEventAnimation.GIANT_SUSHI -> R.raw.chef_event_giant_sushi
            ChefEventAnimation.COMA -> R.raw.chef_event_coma
            ChefEventAnimation.ATTACK -> R.raw.chef_event_attack
            ChefEventAnimation.SPICY -> R.raw.chef_event_spicy
            ChefEventAnimation.NINJA -> R.raw.chef_event_ninja
            ChefEventAnimation.MONSTER -> R.raw.chef_event_monster
        }
    }

    @StringRes
    fun contentDescriptionResFor(animation: ChefEventAnimation): Int {
        return when (animation) {
            ChefEventAnimation.DEVOURING -> R.string.chef_event_devouring_content_description
            ChefEventAnimation.GIANT_SUSHI -> R.string.chef_event_giant_sushi_content_description
            ChefEventAnimation.COMA -> R.string.chef_event_coma_content_description
            ChefEventAnimation.ATTACK -> R.string.chef_event_attack_content_description
            ChefEventAnimation.SPICY -> R.string.chef_event_spicy_content_description
            ChefEventAnimation.NINJA -> R.string.chef_event_ninja_content_description
            ChefEventAnimation.MONSTER -> R.string.chef_event_monster_content_description
        }
    }
}
