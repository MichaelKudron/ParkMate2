package com.parkmate.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class CompassHelper(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    
    private var azimuthListener: ((Float) -> Unit)? = null
    
    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, it.values)
                
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                
                val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                azimuthListener?.invoke(azimuth)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Not used
        }
    }

    fun startListening(listener: (Float) -> Unit) {
        azimuthListener = listener
        rotationSensor?.let {
            sensorManager.registerListener(
                sensorEventListener,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(sensorEventListener)
        azimuthListener = null
    }
}
