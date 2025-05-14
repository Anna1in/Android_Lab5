package com.example.lab5andr

import android.animation.ValueAnimator
import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.TextView
import android.provider.Settings

class ScreenControlSensorManager(private val activity: Activity) : SensorEventListener {

    private val sensorManager = activity.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private val lightText: TextView = activity.findViewById(R.id.light_text)
    private val proximityText: TextView = activity.findViewById(R.id.proximity_text)

    private var currentBrightness = 0.5f  // Початкове значення яскравості

    fun register() {
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun unregister() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_LIGHT -> {
                    val lightValue = it.values[0]
                    lightText.text = "Освітленість: %.2f лк".format(lightValue)
                    adjustBrightnessGloballyAndLocally(lightValue)
                }

                Sensor.TYPE_PROXIMITY -> {
                    val proximityValue = it.values[0]
                    proximityText.text = "Відстань: %.2f см".format(proximityValue)

                    if (proximitySensor?.maximumRange != null && proximityValue < proximitySensor.maximumRange) {
                        dimScreen()
                    } else {
                        restoreScreen()
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun adjustBrightnessGloballyAndLocally(lux: Float) {
        if (Settings.System.canWrite(activity)) {
            val brightnessValue = when {
                lux < 10 -> 30
                lux < 100 -> 100
                lux < 500 -> 180
                else -> 255
            }

            // Зміна глобальної яскравості (0..255)
            Settings.System.putInt(
                activity.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightnessValue
            )

            // Одночасно плавна локальна анімація для activity (0f..1f)
            val targetBrightness = brightnessValue / 255f

            val animator = ValueAnimator.ofFloat(currentBrightness, targetBrightness)
            animator.duration = 300
            animator.interpolator = LinearInterpolator()

            animator.addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                val layoutParams = activity.window.attributes
                layoutParams.screenBrightness = animatedValue
                activity.window.attributes = layoutParams
            }

            animator.start()
            currentBrightness = targetBrightness
        }
    }


    private fun dimScreen() {
        val layoutParams = activity.window.attributes
        layoutParams.screenBrightness = 0.01f
        activity.window.attributes = layoutParams
    }

    private fun restoreScreen() {
        val layoutParams = activity.window.attributes
        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        activity.window.attributes = layoutParams
    }
}
