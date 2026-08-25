package com.mancebolabs.sushiclash.domain.model

sealed interface PersistenceReadState<out T> {
    data object Missing : PersistenceReadState<Nothing>
    data class Data<T>(val value: T) : PersistenceReadState<T>
    data object Corrupted : PersistenceReadState<Nothing>
    data object Unavailable : PersistenceReadState<Nothing>
}

fun PersistenceReadState<*>.isUnreadable(): Boolean {
    return this is PersistenceReadState.Corrupted || this is PersistenceReadState.Unavailable
}
