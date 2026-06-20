package com.mancebolabs.sushicounter.feature.wheel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mancebolabs.sushicounter.domain.repository.ParticipantsRepository
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WheelUiState(
    val participants: List<String> = emptyList(),
    val inputName: String = "",
    val wheelRotation: Float = 0f,
    val isSpinning: Boolean = false,
    val selectedWinner: String? = null,
) {
    val canSpin: Boolean
        get() = participants.size >= 2 && !isSpinning
}

class WheelViewModel(
    private val participantsRepository: ParticipantsRepository,
) : ViewModel() {

    private val inputName = MutableStateFlow("")
    private val wheelRotation = MutableStateFlow(0f)
    private val isSpinning = MutableStateFlow(false)
    private val selectedWinner = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            participantsRepository.ensureGroupParticipantsSeeded()
        }
    }

    val uiState: StateFlow<WheelUiState> = combine(
        participantsRepository.participants,
        inputName,
        wheelRotation,
        isSpinning,
        selectedWinner,
    ) { participants, name, rotation, spinning, winner ->
        WheelUiState(
            participants = participants,
            inputName = name,
            wheelRotation = rotation,
            isSpinning = spinning,
            selectedWinner = winner,
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
            participantsRepository.addParticipant(name)
            inputName.value = ""
        }
    }

    fun onRemoveParticipant(name: String) {
        viewModelScope.launch {
            participantsRepository.removeParticipant(name)
        }
    }

    fun onSpin() {
        val participants = uiState.value.participants
        if (participants.size < 2 || isSpinning.value) return

        viewModelScope.launch {
            isSpinning.value = true
            selectedWinner.value = null

            val winnerIndex = Random.nextInt(participants.size)
            val winner = participants[winnerIndex]
            val segmentAngle = 360f / participants.size
            val winnerCenterAngle = winnerIndex * segmentAngle + segmentAngle / 2f - 90f
            val currentRotation = wheelRotation.value
            val normalizedCurrent = ((currentRotation % 360f) + 360f) % 360f
            val targetOffset = ((-90f - winnerCenterAngle) - normalizedCurrent + 360f) % 360f
            val extraSpins = Random.nextInt(MIN_EXTRA_SPINS, MAX_EXTRA_SPINS + 1)
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

    companion object {
        private const val MIN_EXTRA_SPINS = 4
        private const val MAX_EXTRA_SPINS = 6
        const val SPIN_DURATION_MS = 3_500L

        fun factory(participantsRepository: ParticipantsRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WheelViewModel(participantsRepository) as T
                }
            }
        }
    }
}
