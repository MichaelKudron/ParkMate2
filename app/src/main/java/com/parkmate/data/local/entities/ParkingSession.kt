package com.parkmate.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parking_sessions")
data class ParkingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val carId: Long? = null,
    val lat: Double,
    val lng: Double,
    val address: String? = null,
    val note: String? = null,
    val startTime: Long,
    val endTime: Long? = null,
    val paidUntil: Long? = null,
    val accuracyMeters: Float? = null
)
