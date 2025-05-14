package com.example.lab5andr

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.net.Uri
import android.provider.Settings


class MainActivity : AppCompatActivity() {

    private lateinit var levelSensorManager: LevelSensorManager
    private lateinit var screenControlSensorManager: ScreenControlSensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestWriteSettingsPermission()

        levelSensorManager = LevelSensorManager(this)
        screenControlSensorManager = ScreenControlSensorManager(this)
    }

    private fun requestWriteSettingsPermission() {
        if (!Settings.System.canWrite(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }


    override fun onResume() {
        super.onResume()
        if (Settings.System.canWrite(this)) {
            levelSensorManager.register()
            screenControlSensorManager.register()
        } else {
            requestWriteSettingsPermission()
        }
    }


    override fun onPause() {
        super.onPause()
        levelSensorManager.unregister()
        screenControlSensorManager.unregister()
    }
}

