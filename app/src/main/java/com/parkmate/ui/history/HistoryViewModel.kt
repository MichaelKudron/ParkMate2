package com.parkmate.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.parkmate.data.local.entities.ParkingSession
import com.parkmate.data.repository.ParkingRepository
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: ParkingRepository) : ViewModel() {
    
    private val _searchQuery = MutableLiveData<String>()
    
    val sessions: LiveData<List<ParkingSession>> = _searchQuery.switchMap { query ->
        if (query.isNullOrEmpty()) {
            repository.observeHistory()
        } else {
            repository.searchHistory(query)
        }
    }

    init {
        _searchQuery.value = ""
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun deleteSession(session: ParkingSession, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.delete(session)
            onComplete()
        }
    }

    fun restoreSession(session: ParkingSession) {
        viewModelScope.launch {
            repository.insert(session)
        }
    }
}

class HistoryViewModelFactory(
    private val repository: ParkingRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
