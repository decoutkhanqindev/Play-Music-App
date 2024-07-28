package com.example.playmusicapp.ui

import android.content.ContentValues
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.database.getStringOrNull
import com.example.playmusicapp.data.MusicContentProvider
import com.example.playmusicapp.databinding.ActivityMainBinding
import com.example.playmusicapp.receiver.NetworkConnectivityReceiver
import com.example.playmusicapp.service.MusicForegroundService

class MainActivity : AppCompatActivity() {

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    // musics content provider
    private val uri = MusicContentProvider.CONTENT_URI
    private val idColumn = MusicContentProvider.COLUMN_ID
    private val nameColumn = MusicContentProvider.COLUMN_NAME

    // network broadcast receiver
    private val receiver = NetworkConnectivityReceiver()
    private val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        displayMusicList()

        binding.insertNewSongBtn.setOnClickListener {
            val nameSong = binding.enterNameSong.text.toString()
            if (nameSong.isEmpty()) {
                Toast.makeText(this, "Name of song is empty", Toast.LENGTH_SHORT).show()
            } else {
                insertNewSong(nameSong)
                displayMusicList()
            }
        }

        binding.startNameSongBtn.setOnClickListener {
            val nameSong = binding.enterNameSongToPlay.text.toString()
            val startBtnText = binding.startNameSongBtn.text.toString()
            if (nameSong.isEmpty()) {
                Toast.makeText(this, "Name of song is empty", Toast.LENGTH_SHORT).show()
            } else {
                ContextCompat.startForegroundService(
                    /* context = */ this,
                    /* intent = */ Intent(this, MusicForegroundService::class.java).apply {
                        putExtra("nameSong", nameSong)
                        putExtra("actionKey", startBtnText)

                    })
            }
        }

        binding.stopNameSongBtn.setOnClickListener {
            val nameSong = binding.enterNameSongToPlay.text.toString()
            val stopBtnText = binding.stopNameSongBtn.text.toString()
            ContextCompat.startForegroundService(
                /* context = */ this,
                /* intent = */ Intent(this, MusicForegroundService::class.java).apply {
                    putExtra("nameSong", nameSong)
                    putExtra("actionKey", stopBtnText)
                })
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(receiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(receiver)
    }

    private fun insertNewSong(name: String) {
        val nameValue = ContentValues().apply {
            put(nameColumn, name)
        }
        contentResolver.insert(uri, nameValue)
            .let { Toast.makeText(this, "New Music Inserted", Toast.LENGTH_LONG).show() }
    }

    private fun displayMusicList() {
        val cursor = contentResolver.query(/* uri = */ uri,/* projection = */
            arrayOf(idColumn, nameColumn),/* selection = */
            null,/* selectionArgs = */
            null,/* sortOrder = */
            "$idColumn ASC"
        )
        cursor?.use {
            val idColumnIndex = cursor.getColumnIndexOrThrow(idColumn)
            val nameColumnIndex = cursor.getColumnIndexOrThrow(nameColumn)
            val musicList = StringBuilder()
            while (cursor.moveToNext()) {
                val id = cursor.getStringOrNull(idColumnIndex)
                val name = cursor.getStringOrNull(nameColumnIndex)
                musicList.append("${id?.padEnd(40)} ${name}\n")
            }
            binding.musicList.text = musicList.toString()
        }
    }
}