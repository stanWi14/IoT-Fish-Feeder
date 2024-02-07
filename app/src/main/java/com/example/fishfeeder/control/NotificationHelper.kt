package com.example.fishfeeder.control

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.fishfeeder.R
import java.util.*

class NotificationHelper(
    private val context: Context,
    private val devId: String,
    private val devNum: Int
) {

    private val ALARM_REQUEST_CODE_BASE = 1000

    fun scheduleNotification(day: Int, time: String, devId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK)

        val dayDifference = (day - currentDay + 7) % 7
        calendar.add(Calendar.DAY_OF_WEEK, dayDifference)
        val (hour, minute) = parseTime(time)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        val requestCode = ALARM_REQUEST_CODE_BASE + day * 10000 + hour * 100 + minute

        val intent = Intent(context, NotificationReceiver::class.java)
        intent.putExtra(NotificationReceiver.DEV_ID_EXTRA, devId)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    fun cancelNotification(day: Int, time: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCodeToDelete =
            ALARM_REQUEST_CODE_BASE + day * 10000 + parseHourMinute(time)

        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCodeToDelete,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    fun cancelAllNotifications() {
        val notificationManager = NotificationManagerCompat.from(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Loop through the days and cancel notifications for each day
        for (day in Calendar.SUNDAY until Calendar.SATURDAY + 1) {
            val requestCode = ALARM_REQUEST_CODE_BASE + day * 10000

            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Cancel the notification for the specified day and device ID
            notificationManager.cancel(requestCode)
            alarmManager.cancel(pendingIntent)
        }
    }

    fun showNotification() {
        val notificationManager = NotificationManagerCompat.from(context)

        val notification = NotificationCompat.Builder(context, "channel_id")
            .setContentTitle(devId)
            .setContentText("This notification was scheduled at a specific time.")
            .setSmallIcon(R.drawable.logo_fish_auto_feeder_mid)
            .build()

        notificationManager.notify(devNum, notification)
    }


    private fun parseTime(time: String): Pair<Int, Int> {
        val hour = time.substring(0, 2).toInt()
        val minute = time.substring(2, 4).toInt()
        return Pair(hour, minute)
    }

    private fun parseHourMinute(time: String): Int {
        return time.substring(0, 4).toInt()
    }
}
