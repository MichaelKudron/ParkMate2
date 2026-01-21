package com.parkmate.ui.startsession

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkmate.data.local.entities.ParkingSession
import com.parkmate.data.repository.ParkingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StartSessionViewModel(
    private val repository: ParkingRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<StartSessionUiState>(StartSessionUiState.Idle)
    val uiState: StateFlow<StartSessionUiState> = _uiState

    var note: String
        get() = savedStateHandle.get<String>("note") ?: ""
        set(value) {
            savedStateHandle["note"] = value
        }

    fun startSession(lat: Double, lng: Double, accuracy: Float?, note: String) {
        viewModelScope.launch {
            _uiState.value = StartSessionUiState.Loading
            try {
                val session = ParkingSession(
                    lat = lat,
                    lng = lng,
                    note = note,
                    startTime = System.currentTimeMillis(),
                    accuracyMeters = accuracy
                )
                val sessionId = repository.insert(session)
                
                // Fetch address asynchronously
                val address = repository.reverseGeocode(lat, lng)
                if (address != null) {
                    val updatedSession = session.copy(id = sessionId, address = address)
                    repository.update(updatedSession)
                }
                
                _uiState.value = StartSessionUiState.Success
            } catch (e: Exception) {
                _uiState.value = StartSessionUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _uiState.value = StartSessionUiState.Idle
    }
}

sealed class StartSessionUiState {
    object Idle : StartSessionUiState()
    object Loading : StartSessionUiState()
    object Success : StartSessionUiState()
    data class Error(val message: String) : StartSessionUiState()
}

class StartSessionViewModelFactory(
    private val repository: ParkingRepository,
    private val owner: androidx.savedstate.SavedStateRegistryOwner
) : androidx.lifecycle.AbstractSavedStateViewModelFactory(owner, null) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(StartSessionViewModel::class.java)) {
            return StartSessionViewModel(repository, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
