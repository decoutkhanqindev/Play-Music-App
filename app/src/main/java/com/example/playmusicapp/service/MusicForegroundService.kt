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
import com.example.playmusicapp.data.MusicContentProvider

class MusicForegroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "MusicForegroundServiceChannel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val nameSong = intent?.getStringExtra("nameSong") ?: ""
        val songData = querySongByName(this, nameSong)

        if (songData != null) {
            val action = intent?.getStringExtra("actionKey") ?: ""
            when (action) {
                "Start" -> {
                    val notification = createNotification(songData.name)
                    startForeground(NOTIFICATION_ID, notification)
                }
                "Stop" -> {
                    stopSelf()
                }
            }
        } else {
            Toast.makeText(this, "Song not found: $nameSong", Toast.LENGTH_SHORT).show()
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
            setContentText("$nameSong is playing")
        }.build()
    }

    private fun querySongByName(context: Context, nameSong: String): SongData? {
        val cursor = context.contentResolver.query(
            /* uri = */ MusicContentProvider.CONTENT_URI,
            /* projection = */ arrayOf(MusicContentProvider.COLUMN_ID, MusicContentProvider.COLUMN_NAME),
            /* selection = */ "${MusicContentProvider.COLUMN_NAME} = ?",
            /* selectionArgs = */ arrayOf(nameSong),
            /* sortOrder = */ null
        )
        cursor?.use {
            if (cursor.moveToFirst()) {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(MusicContentProvider.COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(MusicContentProvider.COLUMN_NAME))
                return SongData(id, name)
            }
        }
        return null
    }
}

data class SongData(val id: String, val name: String)
