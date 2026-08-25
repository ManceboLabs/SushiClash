package com.mancebolabs.sushiclash.data.datastore

import android.util.Log
import androidx.datastore.preferences.core.Preferences
import com.mancebolabs.sushiclash.domain.model.PersistenceReadState
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen

interface PersistenceLogger {
    fun logFailure(operation: String, errorClassName: String)
}

object NoOpPersistenceLogger : PersistenceLogger {
    override fun logFailure(operation: String, errorClassName: String) = Unit
}

internal class AndroidPersistenceLogger : PersistenceLogger {
    override fun logFailure(operation: String, errorClassName: String) {
        Log.w(TAG, "$operation $errorClassName")
    }

    private companion object {
        const val TAG = "SushiClashPersistence"
    }
}

internal fun <T> Flow<Preferences>.mapWithPersistenceReadState(
    logger: PersistenceLogger,
    operation: String,
    transform: (Preferences) -> PersistenceReadState<T>,
): Flow<PersistenceReadState<T>> {
    return map(transform).retryWhen { cause, _ ->
        if (cause is CancellationException) throw cause
        if (cause !is IOException) {
            return@retryWhen false
        }
        logger.logFailure(operation, cause::class.java.simpleName)
        emit(PersistenceReadState.Unavailable)
        // Space retries so a persistent disk failure cannot busy-loop the collector.
        delay(1_000L)
        true
    }
}
