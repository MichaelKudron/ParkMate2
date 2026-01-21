package com.parkmate.ui.sessiondetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkmate.data.local.entities.ParkingSession
import com.parkmate.data.repository.ParkingRepository
import kotlinx.coroutines.launch

class SessionDetailsViewModel(private val repository: ParkingRepository) : ViewModel() {
    
    private val _session = MutableLiveData<ParkingSession?>()
    val session: LiveData<ParkingSession?> = _session

    fun loadSession(sessionId: Long) {
        viewModelScope.launch {
            _session.value = repository.getById(sessionId)
        }
    }
}

class SessionDetailsViewModelFactory(
    private val repository: ParkingRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionDetailsViewModel::class.java)) {
            return SessionDetailsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
