package com.parkmate.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.parkmate.data.local.entities.ParkingSession

@Dao
interface ParkingSessionDao {
    @Insert
    suspend fun insert(session: ParkingSession): Long

    @Update
    suspend fun update(session: ParkingSession)

    @Delete
    suspend fun delete(session: ParkingSession)

    @Query("SELECT * FROM parking_sessions WHERE endTime IS NULL LIMIT 1")
    fun observeActiveSession(): LiveData<ParkingSession?>

    @Query("SELECT * FROM parking_sessions WHERE endTime IS NOT NULL ORDER BY startTime DESC")
    fun observeHistory(): LiveData<List<ParkingSession>>

    @Query("SELECT * FROM parking_sessions WHERE endTime IS NOT NULL AND (note LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%') ORDER BY startTime DESC")
    fun searchHistory(query: String): LiveData<List<ParkingSession>>

    @Query("SELECT * FROM parking_sessions WHERE id = :id")
    suspend fun getById(id: Long): ParkingSession?

    @Query("SELECT * FROM parking_sessions WHERE endTime IS NULL LIMIT 1")
    suspend fun getActiveSession(): ParkingSession?
}
