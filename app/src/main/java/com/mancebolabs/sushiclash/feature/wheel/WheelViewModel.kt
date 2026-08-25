package com.mancebolabs.sushiclash.feature.wheel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mancebolabs.sushiclash.domain.model.DefaultRandomProvider
import com.mancebolabs.sushiclash.domain.model.PersistenceReadState
import com.mancebolabs.sushiclash.domain.model.RandomProvider
import com.mancebolabs.sushiclash.domain.model.isUnreadable
import com.mancebolabs.sushiclash.domain.repository.ParticipantsRepository
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WheelUiState(
    val participants: List<String> = emptyList(),
    val inputName: String = "",
    val wheelRotation: Float = 0f,
    val isSpinning: Boolean = false,
    val selectedWinner: String? = null,
    val showInsufficientParticipantsWarning: Boolean = false,
    val persistenceError: Boolean = false,
    val isPersistenceRetrying: Boolean = false,
) {
    val canSpin: Boolean
        get() = participants.size >= 2 && !isSpinning
}

private data class WheelParticipantsSnapshot(
    val participantsState: PersistenceReadState<List<String>>,
    val inputName: String,
    val wheelRotation: Float,
    val isSpinning: Boolean,
    val selectedWinner: String?,
)

private data class WheelPersistenceSnapshot(
    val showInsufficientParticipantsWarning: Boolean,
    val writePersistenceError: Boolean,
    val isPersistenceRetrying: Boolean,
)

class WheelViewModel(
    private val participantsRepository: ParticipantsRepository,
    private val randomProvider: RandomProvider = DefaultRandomProvider(),
) : ViewModel() {

    private val inputName = MutableStateFlow("")
    private val wheelRotation = MutableStateFlow(0f)
    private val isSpinning = MutableStateFlow(false)
    private val selectedWinner = MutableStateFlow<String?>(null)
    private val showInsufficientParticipantsWarning = MutableStateFlow(false)
    private val writePersistenceError = MutableStateFlow(false)
    private val isPersistenceRetrying = MutableStateFlow(false)
    private var pendingParticipantWrite: PendingParticipantWrite? = null

    init {
        viewModelScope.launch {
            seedGroupParticipants()
        }
    }

    val uiState: StateFlow<WheelUiState> = combine(
        combine(
            participantsRepository.participants,
            inputName,
            wheelRotation,
            isSpinning,
            selectedWinner,
        ) { participantsState, name, rotation, spinning, winner ->
            WheelParticipantsSnapshot(
                participantsState = participantsState,
                inputName = name,
                wheelRotation = rotation,
                isSpinning = spinning,
                selectedWinner = winner,
            )
        },
        combine(
            showInsufficientParticipantsWarning,
            writePersistenceError,
            isPersistenceRetrying,
        ) { insufficientParticipants, writeError, retrying ->
            WheelPersistenceSnapshot(
                showInsufficientParticipantsWarning = insufficientParticipants,
                writePersistenceError = writeError,
                isPersistenceRetrying = retrying,
            )
        },
    ) { snapshot, persistence ->
        val participants = when (val state = snapshot.participantsState) {
            is PersistenceReadState.Data -> state.value
            PersistenceReadState.Missing,
            PersistenceReadState.Corrupted,
            PersistenceReadState.Unavailable -> emptyList()
        }
        WheelUiState(
            participants = participants,
            inputName = snapshot.inputName,
            wheelRotation = snapshot.wheelRotation,
            isSpinning = snapshot.isSpinning,
            selectedWinner = snapshot.selectedWinner,
            showInsufficientParticipantsWarning = persistence.showInsufficientParticipantsWarning,
            persistenceError = snapshot.participantsState.isUnreadable() || persistence.writePersistenceError,
            isPersistenceRetrying = persistence.isPersistenceRetrying,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WheelUiState(),
    )

    fun onInputChanged(value: String) {
        inputName.value = value
    }

    fun onAddParticipant() {
        val name = inputName.value.trim()
        if (name.isEmpty()) return

        viewModelScope.launch {
            persistAddParticipant(name)
        }
    }

    fun onRemoveParticipant(name: String) {
        viewModelScope.launch {
            persistRemoveParticipant(name)
        }
    }

    fun onSpin() {
        val participants = uiState.value.participants
        if (participants.size < 2 || isSpinning.value) return

        viewModelScope.launch {
            isSpinning.value = true
            selectedWinner.value = null

            val winnerIndex = randomProvider.nextInt(0, participants.size)
            val winner = participants[winnerIndex]
            val segmentAngle = 360f / participants.size
            val winnerCenterAngle = winnerIndex * segmentAngle + segmentAngle / 2f - 90f
            val currentRotation = wheelRotation.value
            val normalizedCurrent = ((currentRotation % 360f) + 360f) % 360f
            val targetOffset = ((-90f - winnerCenterAngle) - normalizedCurrent + 360f) % 360f
            val extraSpins = randomProvider.nextInt(MIN_EXTRA_SPINS, MAX_EXTRA_SPINS + 1)
            val targetRotation = currentRotation + extraSpins * 360f + targetOffset

            wheelRotation.value = targetRotation

            kotlinx.coroutines.delay(SPIN_DURATION_MS)
            isSpinning.value = false
            selectedWinner.value = winner
        }
    }

    fun onWinnerDialogDismissed() {
        selectedWinner.value = null
    }

    fun onInsufficientParticipantsDismissed() {
        showInsufficientParticipantsWarning.value = false
    }

    fun onPersistenceRetry() {
        if (!isPersistenceRetrying.compareAndSet(expect = false, update = true)) return
        viewModelScope.launch {
            try {
                val pending = pendingParticipantWrite
                if (pending != null) {
                    when (pending) {
                        is PendingParticipantWrite.Add -> persistAddParticipant(pending.name)
                        is PendingParticipantWrite.Remove -> persistRemoveParticipant(pending.name)
                    }
                } else {
                    seedGroupParticipants()
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: IOException) {
                writePersistenceError.value = true
            } finally {
                isPersistenceRetrying.value = false
            }
        }
    }

    private suspend fun persistAddParticipant(name: String) {
        try {
            val added = participantsRepository.addParticipant(name)
            pendingParticipantWrite = null
            writePersistenceError.value = false
            if (added) {
                inputName.value = ""
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: IOException) {
            pendingParticipantWrite = PendingParticipantWrite.Add(name)
            writePersistenceError.value = true
        }
    }

    private suspend fun persistRemoveParticipant(name: String) {
        try {
            participantsRepository.removeParticipant(name)
            pendingParticipantWrite = null
            writePersistenceError.value = false
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: IOException) {
            pendingParticipantWrite = PendingParticipantWrite.Remove(name)
            writePersistenceError.value = true
        }
    }

    private suspend fun seedGroupParticipants() {
        try {
            val seeded = participantsRepository.ensureGroupParticipantsSeeded()
            val state = participantsRepository.participants.first()
            if (seeded && state is PersistenceReadState.Data) {
                writePersistenceError.value = false
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: IOException) {
            writePersistenceError.value = true
        }
    }

    fun onAutoSpinRequested() {
        viewModelScope.launch {
            participantsRepository.ensureGroupParticipantsSeeded()
            val participants = when (val state = participantsRepository.participants.first()) {
                is PersistenceReadState.Data -> state.value
                PersistenceReadState.Missing,
                PersistenceReadState.Corrupted,
                PersistenceReadState.Unavailable -> emptyList()
            }

            if (participants.size >= 2) {
                onSpin()
            } else {
                showInsufficientParticipantsWarning.value = true
            }
        }
    }

    companion object {
        private const val MIN_EXTRA_SPINS = 4
        private const val MAX_EXTRA_SPINS = 6
        const val SPIN_DURATION_MS = 3_500L

        fun factory(
            participantsRepository: ParticipantsRepository,
            randomProvider: RandomProvider = DefaultRandomProvider(),
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WheelViewModel(participantsRepository, randomProvider) as T
                }
            }
        }
    }
}

private sealed interface PendingParticipantWrite {
    data class Add(val name: String) : PendingParticipantWrite
    data class Remove(val name: String) : PendingParticipantWrite
}
