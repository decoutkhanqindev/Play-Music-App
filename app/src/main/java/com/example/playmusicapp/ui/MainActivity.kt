package com.example.playmusicapp.ui

import android.content.ContentValues
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.database.getStringOrNull
import com.example.playmusicapp.data.MusicContentProvider
import com.example.playmusicapp.databinding.ActivityMainBinding
import com.example.playmusicapp.receiver.NetworkConnectivityReceiver

class MainActivity : AppCompatActivity() {
    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    // musics content provider
    private val uri = MusicContentProvider.CONTENT_URI
    private val columnId = MusicContentProvider.COLUMN_ID
    private val columnName = MusicContentProvider.COLUMN_NAME

    // network broadcast receiver
    private val receiver = NetworkConnectivityReceiver()
    private val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        displayMusicList()

        binding.insertBtn.setOnClickListener {
            val nameOfSong = binding.nameOfSong.text.toString()
            if (nameOfSong.isEmpty()) {
                Toast.makeText(this, "Name of song is empty", Toast.LENGTH_SHORT).show()
            } else {
                insertNewSong(nameOfSong)
                displayMusicList()
            }
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
            put(columnName, name)
        }
        contentResolver.insert(uri, nameValue)
            .let { Toast.makeText(this, "New Music Inserted", Toast.LENGTH_LONG).show() }
    }

    private fun displayMusicList() {
        val cursor = contentResolver.query(
            /* uri = */ uri,
            /* projection = */ arrayOf(columnId, columnName),
            /* selection = */ null,
            /* selectionArgs = */ null,
            /* sortOrder = */ "$columnId ASC"
        )
        cursor?.use {
            val idColumnIndex = cursor.getColumnIndexOrThrow(columnId)
            val nameColumnIndex = cursor.getColumnIndexOrThrow(columnName)
            val musicList = StringBuilder()
            while (cursor.moveToNext()) {
                val id = cursor.getStringOrNull(idColumnIndex)
                val name = cursor.getStringOrNull(nameColumnIndex)
                musicList.append("${id?.padEnd(50)} ${name}\n")
            }
            binding.listOfSongs.text = musicList.toString()
        }
    }
}