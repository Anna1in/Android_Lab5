package com.example.lab5andr

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.PowerManager
import android.view.WindowManager
import android.widget.TextView

class ScreenControlSensorManager(private val activity: Activity) : SensorEventListener {

    private val sensorManager = activity.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private val lightText: TextView = activity.findViewById(R.id.light_text)
    private val proximityText: TextView = activity.findViewById(R.id.proximity_text)

    private val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
    private var wakeLock: PowerManager.WakeLock? = null

    init {
        wakeLock = powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            "MyApp::ProximityLock"
        )
    }

    fun register() {
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun unregister() {
        sensorManager.unregisterListener(this)
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val sensor = it.sensor ?: return
            when (sensor.type) {
                Sensor.TYPE_LIGHT -> {
                    val lightValue = it.values[0]
                    lightText.text = "Освітленість: %.2f лк".format(lightValue)
                    adjustScreenBrightness(lightValue)
                }

                Sensor.TYPE_PROXIMITY -> {
                    val proximityValue = it.values[0]
                    proximityText.text = "Відстань: %.2f см".format(proximityValue)
                    proximitySensor?.let { proxSensor ->
                        if (proximityValue == 0.0f) {
                            turnScreenOff()
                        } else {
                            turnScreenOn()
                        }
                    }
                }

                else -> {

                }
            }
        }
    }



    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun adjustScreenBrightness(lux: Float) {
        val layoutParams = activity.window.attributes
        layoutParams.screenBrightness = when {
            lux < 10 -> 0.1f
            lux < 100 -> 0.3f
            lux < 500 -> 0.6f
            else -> 1.0f
        }
        activity.window.attributes = layoutParams
    }

    private fun turnScreenOff() {
        if (wakeLock?.isHeld == false) {
            wakeLock?.acquire()
        }
    }

    private fun turnScreenOn() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }
}

