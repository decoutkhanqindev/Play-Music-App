package com.example.playmusicapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.playmusicapp.R

class MusicForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "MusicForegroundServiceChannel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        // get name song
        val nameSong = intent?.getStringExtra("nameSong") ?: ""

        val notification = createNotification(nameSong)

        startForeground(NOTIFICATION_ID, notification)

        val action = intent?.getStringExtra("actionKey") ?: ""
        when (action) {
            "Start" -> Toast.makeText(this, "$nameSong is playing", Toast.LENGTH_LONG).show()
            "Stop" -> {
                stopSelf()
                Toast.makeText(this, "$nameSong is stopped", Toast.LENGTH_LONG).show()
            }
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel == null) {
                val name = getString(R.string.notification_name)
                val descriptionText = getString(R.string.notification_description)
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private fun createNotification(nameSong: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setSmallIcon(R.mipmap.ic_launcher)
            setContentTitle(getString(R.string.notification_name))
            setContentText("$nameSong is playing)}")
        }.build()
    }
}