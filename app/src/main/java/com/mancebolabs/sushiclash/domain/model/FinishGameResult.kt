package com.mancebolabs.sushiclash.domain.model

sealed interface FinishGameResult {
    data object Success : FinishGameResult

    data object NoActiveGame : FinishGameResult

    data class Failure(
        val cause: Throwable,
    ) : FinishGameResult
}

class CorruptGameHistoryException : IllegalStateException(
    "The target game history cannot be decoded",
)

class InvalidActiveGameException : IllegalStateException(
    "The active game cannot be decoded or is invalid",
)
