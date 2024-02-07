package com.example.fishfeeder.model

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class DeviceApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy {
        DeviceDatabase.getDatabase(this, applicationScope)
    }
    val repository by lazy {
        DeviceRepository(database.getDeviceDao())
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val devId = "default_dev_id"
            createNotificationChannel(devId)
        }
    }

    fun createNotificationChannel(devId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                devId,
                "Channel name",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}