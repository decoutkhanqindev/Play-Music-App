package com.example.playmusicapp.ui

import android.content.ContentValues
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.database.getStringOrNull
import com.example.playmusicapp.R
import com.example.playmusicapp.data.MusicContentProvider
import com.example.playmusicapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        private val URI = MusicContentProvider.CONTENT_URI
        private const val ID = MusicContentProvider.COLUMN_ID
        private const val NAME = MusicContentProvider.COLUMN_NAME
    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }

    private fun insertMusic(name: String) {
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
            /* sortOrder = */ "ID ASC"
        )
        cursor?.use {
            val idColumnIndex = cursor.getColumnIndexOrThrow(ID)
            val nameColumnIndex = cursor.getColumnIndexOrThrow(NAME)
            val arrayOfMusics = StringBuilder()
            while (cursor.moveToNext()) {
                val id = cursor.getStringOrNull(idColumnIndex)
                val name = cursor.getStringOrNull(nameColumnIndex)
                arrayOfMusics.append("$id\t\t\t$name\n")
            }
        }
    }
}