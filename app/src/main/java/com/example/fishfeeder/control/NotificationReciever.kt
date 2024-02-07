package com.example.fishfeeder.control

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.fishfeeder.R

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
            .setContentTitle("There is Feeding Schedule")  // Set the title to devId
            .setContentText("Feeding schedule for $devId")
            .setSmallIcon(R.drawable.logo_fish_auto_feeder_mid)
            .build()

        notificationManager.notify(1, notification)
    }
}
