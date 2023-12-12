package com.example.fishfeeder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val DEV_ID_EXTRA = "devId"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val devId = intent?.getStringExtra(DEV_ID_EXTRA)
        if (devId != null) {
            showNotification(context, devId)
        }
    }

    private fun showNotification(context: Context?, devId: String) {
        val notificationManager = NotificationManagerCompat.from(context!!)
        val notification = NotificationCompat.Builder(context, devId)
            .setContentTitle(devId)  // Set the title to devId
            .setContentText("This notification was scheduled at a specific time.")
            .setSmallIcon(R.drawable.logo_fish_auto_feeder_mid)
            .build()

        notificationManager.notify(1, notification)
    }
}
