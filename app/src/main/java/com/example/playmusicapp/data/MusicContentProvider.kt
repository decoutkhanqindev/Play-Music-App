package com.example.playmusicapp.data

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri

internal class MusicContentProvider : ContentProvider() {

    companion object {
        // Authority cua content provider
        private const val AUTHORITY = "com.example.musicapp.provider"
        private const val URL = "content://$AUTHORITY/musics"
        val CONTENT_URI: Uri = Uri.parse(URL)

        // cac constants cua database
        private const val DATABASE_NAME = "musics.db"
        private const val TABLE_NAME = "musics.tb"
        internal const val COLUMN_ID = "id"
        internal const val COLUMN_NAME = "name"
        private const val DATABASE_VERSION = 1
        private const val CREATE_TABLE = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL
            );
        """

        // URI matching codes
        private const val URI_CODE = 1
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            // dang ki 1 URI pattern de truy van all musics
            // content://com.example.musicapp.provider/musics
            addURI(AUTHORITY, "musics", URI_CODE)
            // dang ki 1 URI pattern de truy van mot music cu the
            // content://com.example.musicapp.provider/musics/123
            addURI(AUTHORITY, "musics/*", URI_CODE)
        }
    }

    private var db: SQLiteDatabase? = null

    override fun onCreate(): Boolean {
        val dbHelper = MusicsDatabaseHelper(context!!)
        db = dbHelper.writableDatabase // database co the thay doi
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        // xau dung cau truy van SQL
        val qb = SQLiteQueryBuilder().apply {
            tables = TABLE_NAME
            when (uriMatcher.match(uri)) { // xac dinh loai truy van
                // neu khop voi 1 trong nhung URI pattern da dang ky
                URI_CODE -> projectionMap = null // cac cot cua bang se dc tra ve
                else -> throw IllegalArgumentException("Unknown URI $uri")
            }
        }
        val cursor = qb.query( // chua ket qua tra ve
            /* db = */ db,
            /* projectionIn = */ projection,
            /* selection = */ selection,
            /* selectionArgs = */ selectionArgs,
            /* groupBy = */ null,
            /* having = */ null,
            /* sortOrder = */ sortOrder
        )
        // dang ki uri voi content resolver de nhan thong bao neu data trong bang thay doi
        // uri = null -> tat ca cac uri lien quan den content resolver se dc thong bao
        cursor?.setNotificationUri(context!!.contentResolver, null)
        return cursor.use { it } // tu dong close cursor
    }

    override fun getType(uri: Uri): String {
        return when (uriMatcher.match(uri)) {
            URI_CODE -> "vnd.android.cursor.dir/musics"
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        val rowId = db!!.insert(
            /* table = */ TABLE_NAME,
            /* nullColumnHack = */ null,
            /* values = */ values
        )
        if (rowId > 0) { // chen thanh cong
            // tao uri moi + id
            val newUri = ContentUris.withAppendedId(CONTENT_URI!!, rowId)
            // dang ky nhan thong bao cho content resolver rang
            // doi voi bat ki observer nao da dang ki voi uri moi da thay doi
            context!!.contentResolver.notifyChange(newUri, null)
            return newUri
        }
        throw SQLiteException("Failed to add a record into $uri")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val numberOfRowAffected = when (uriMatcher.match(uri)) {
            URI_CODE -> db!!.delete(TABLE_NAME, selection, selectionArgs)
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return numberOfRowAffected
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val numberOfRowAffected = when (uriMatcher.match(uri)) {
            URI_CODE -> db!!.update(TABLE_NAME, values, selection, selectionArgs)
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return numberOfRowAffected
    }

    private class MusicsDatabaseHelper(context: Context) : SQLiteOpenHelper(
        /* context = */ context,
        /* name = */ DATABASE_NAME,
        /* factory = */ null,
        /* version = */ DATABASE_VERSION
    ) {
        override fun onCreate(db: SQLiteDatabase?) {
            db?.execSQL(CREATE_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }
    }
}