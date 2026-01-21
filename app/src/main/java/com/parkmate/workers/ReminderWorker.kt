package com.parkmate.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.parkmate.utils.NotificationHelper

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val message = inputData.getString(KEY_MESSAGE) ?: "Your parking meter is about to expire!"
        
        NotificationHelper.showParkingMeterNotification(applicationContext, message)
        
        return Result.success()
    }

    companion object {
        const val KEY_MESSAGE = "message"
        const val WORK_TAG = "parking_meter_reminder"
    }
}
