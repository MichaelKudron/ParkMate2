package com.parkmate.data.repository

import androidx.lifecycle.LiveData
import com.parkmate.data.local.ParkingSessionDao
import com.parkmate.data.local.entities.ParkingSession
import com.parkmate.data.remote.NominatimApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ParkingRepository(
    private val parkingSessionDao: ParkingSessionDao,
    private val nominatimApi: NominatimApi
) {
    fun observeActiveSession(): LiveData<ParkingSession?> =
        parkingSessionDao.observeActiveSession()

    fun observeHistory(): LiveData<List<ParkingSession>> =
        parkingSessionDao.observeHistory()

    fun searchHistory(query: String): LiveData<List<ParkingSession>> =
        parkingSessionDao.searchHistory(query)

    suspend fun getActiveSession(): ParkingSession? = withContext(Dispatchers.IO) {
        parkingSessionDao.getActiveSession()
    }

    suspend fun getById(id: Long): ParkingSession? = withContext(Dispatchers.IO) {
        parkingSessionDao.getById(id)
    }

    suspend fun insert(session: ParkingSession): Long = withContext(Dispatchers.IO) {
        parkingSessionDao.insert(session)
    }

    suspend fun update(session: ParkingSession) = withContext(Dispatchers.IO) {
        parkingSessionDao.update(session)
    }

    suspend fun delete(session: ParkingSession) = withContext(Dispatchers.IO) {
        parkingSessionDao.delete(session)
    }

    suspend fun reverseGeocode(lat: Double, lon: Double): String? = withContext(Dispatchers.IO) {
        try {
            val response = nominatimApi.reverseGeocode(lat = lat, lon = lon)
            response.displayName
        } catch (e: Exception) {
            null
        }
    }
}
