package com.example.playmusicapp.ui

import android.content.ContentValues
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.database.getStringOrNull
import com.example.playmusicapp.data.MusicContentProvider
import com.example.playmusicapp.databinding.ActivityMainBinding
import com.example.playmusicapp.receiver.NetworkConnectivityReceiver

class MainActivity : AppCompatActivity() {

    companion object {
        // constants of musics content provider
        private val URI = MusicContentProvider.CONTENT_URI
        private const val ID = MusicContentProvider.COLUMN_ID
        private const val NAME = MusicContentProvider.COLUMN_NAME

        // network broadcast receiver
        private val receiver = NetworkConnectivityReceiver()
        private val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        queryMusics()

        binding.insertBtn.setOnClickListener {
            val nameOfSong = binding.nameOfSong.text.toString()
            if (nameOfSong.isEmpty()) {
                Toast.makeText(this, "Name of song is empty", Toast.LENGTH_SHORT).show()
            } else {
                insertSong(nameOfSong)
                queryMusics()
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

    private fun insertSong(name: String) {
        val nameValue = ContentValues().apply {
            put(NAME, name)
        }
        contentResolver.insert(URI, nameValue)
            .let { Toast.makeText(this, "New Music Inserted", Toast.LENGTH_LONG).show() }
    }

    private fun queryMusics() {
        val cursor = contentResolver.query(
            /* uri = */ URI,
            /* projection = */ arrayOf(ID, NAME),
            /* selection = */ null,
            /* selectionArgs = */ null,
            /* sortOrder = */ "$ID ASC"
        )
        cursor?.use {
            val idColumnIndex = cursor.getColumnIndexOrThrow(ID)
            val nameColumnIndex = cursor.getColumnIndexOrThrow(NAME)
            val arrayOfMusics = StringBuilder()
            while (cursor.moveToNext()) {
                val id = cursor.getStringOrNull(idColumnIndex)
                val name = cursor.getStringOrNull(nameColumnIndex)
                arrayOfMusics.append("${id?.padEnd(50)} ${name}\n")
            }
            binding.listOfSongs.text = arrayOfMusics.toString()
        }
    }
}