package com.example.lab5andr


import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.TextView
import android.view.View

class LevelSensorManager(private val activity: Activity) : SensorEventListener {

    private val sensorManager = activity.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val horizonLine: View = activity.findViewById(R.id.horizon_line)
    private val angleText: TextView = activity.findViewById(R.id.angle_text)

    fun register() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun unregister() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = it.values[0]
            val angle = -x * 9  // коефіцієнт для кутового зміщення лінії
            horizonLine.rotation = angle
            angleText.text = "Кут нахилу: %.2f°".format(angle)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
