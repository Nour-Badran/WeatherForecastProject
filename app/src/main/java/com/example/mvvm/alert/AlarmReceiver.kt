package com.example.mvvm.alert

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.media.Ringtone
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alertType = intent.getStringExtra("ALERT_TYPE")
        if (alertType == "ALARM") {
            // Trigger alarm sound
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val ringtone: Ringtone = RingtoneManager.getRingtone(context, alarmSound)
            ringtone.play()
            showNotification(context)
        } else if (alertType == "NOTIFICATION") {
            // Show notification
            showNotification(context)
        }
    }

    private fun showNotification(context: Context) {
        val notificationId = 1
        val channelId = "weather_alerts"

        // Create the NotificationChannel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Weather Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Weather Alert")
            .setContentText("Your weather alert is triggered!")
            .setPriority(NotificationCompat.PRIORITY_MAX)

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
