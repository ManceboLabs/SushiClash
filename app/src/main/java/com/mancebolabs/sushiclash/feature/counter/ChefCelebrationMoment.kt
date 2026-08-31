package com.mancebolabs.sushiclash.feature.counter

sealed interface ChefCelebrationMoment {
    data object GameStart : ChefCelebrationMoment

    data object GameFinish : ChefCelebrationMoment
}
