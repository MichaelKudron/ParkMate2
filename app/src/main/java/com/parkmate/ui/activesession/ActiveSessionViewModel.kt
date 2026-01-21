package com.parkmate.ui.activesession

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkmate.data.local.entities.ParkingSession
import com.parkmate.data.repository.ParkingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ActiveSessionViewModel(private val repository: ParkingRepository) : ViewModel() {
    
    val activeSession: LiveData<ParkingSession?> = repository.observeActiveSession()

    private val _endSessionState = MutableStateFlow<EndSessionState>(EndSessionState.Idle)
    val endSessionState: StateFlow<EndSessionState> = _endSessionState

    fun endSession(sessionId: Long) {
        viewModelScope.launch {
            _endSessionState.value = EndSessionState.Loading
            try {
                val session = repository.getById(sessionId)
                if (session != null) {
                    val updatedSession = session.copy(endTime = System.currentTimeMillis())
                    repository.update(updatedSession)
                    _endSessionState.value = EndSessionState.Success
                }
            } catch (e: Exception) {
                _endSessionState.value = EndSessionState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun setPaidUntil(sessionId: Long, paidUntil: Long) {
        viewModelScope.launch {
            try {
                val session = repository.getById(sessionId)
                if (session != null) {
                    val updatedSession = session.copy(paidUntil = paidUntil)
                    repository.update(updatedSession)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun resetEndSessionState() {
        _endSessionState.value = EndSessionState.Idle
    }
}

sealed class EndSessionState {
    object Idle : EndSessionState()
    object Loading : EndSessionState()
    object Success : EndSessionState()
    data class Error(val message: String) : EndSessionState()
}

class ActiveSessionViewModelFactory(
    private val repository: ParkingRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActiveSessionViewModel::class.java)) {
            return ActiveSessionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
