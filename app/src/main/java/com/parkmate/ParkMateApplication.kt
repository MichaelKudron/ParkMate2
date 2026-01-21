package com.parkmate

import android.app.Application
import com.parkmate.data.local.ParkMateDatabase
import com.parkmate.data.remote.RetrofitClient
import com.parkmate.data.repository.ParkingRepository
import com.parkmate.utils.NotificationHelper

class ParkMateApplication : Application() {
    
    private val database by lazy { ParkMateDatabase.getDatabase(this) }
    val repository by lazy {
        ParkingRepository(
            database.parkingSessionDao(),
            RetrofitClient.nominatimApi
        )
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
