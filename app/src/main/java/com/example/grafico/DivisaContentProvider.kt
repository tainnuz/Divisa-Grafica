package com.example.grafico

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.example.grafico.data.Divisa
import com.example.grafico.data.local.DivisaDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DivisaContentProvider : ContentProvider() {

    companion object {
        private const val TAG = "DivisaContentProvider"
        private const val AUTHORITY = "com.example.grafico"
        private const val TABLE_NAME = "divisa_table"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$TABLE_NAME")

        private const val CODE_DIVISAS = 1
        private const val CODE_DIVISA_ID = 2

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, TABLE_NAME, CODE_DIVISAS)
            addURI(AUTHORITY, "$TABLE_NAME/#", CODE_DIVISA_ID)
        }
    }

    private lateinit var db: DivisaDatabase

    override fun onCreate(): Boolean {
        Log.d(TAG, "onCreate: Iniciando ContentProvider")
        db = DivisaDatabase.getDatabase(context!!)
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        Log.d(TAG, "query: Recibida consulta en URI -> $uri")

        return when (uriMatcher.match(uri)) {
            CODE_DIVISAS -> {
                Log.d(TAG, "query: Obteniendo todas las divisas")
                val cursor = db.divisaDao().getAllDivisasCursor()
                cursor.setNotificationUri(context!!.contentResolver, uri)
                cursor
            }
            else -> {
                Log.e(TAG, "query: URI no soportada -> $uri")
                throw IllegalArgumentException("URI no soportada: $uri")
            }
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        Log.d(TAG, "insert: Intentando insertar datos en URI -> $uri")

        return when (uriMatcher.match(uri)) {
            CODE_DIVISAS -> {
                Log.d(TAG, "insert: Inserción de divisa")
                val divisa = Divisa(
                    divisa = values?.getAsString("divisa") ?: "",
                    valor = values?.getAsDouble("valor") ?: 0.0
                )

                var newUri: Uri? = null
                val job = GlobalScope.launch {
                    val id = db.divisaDao().insertDivisa(divisa)
                    newUri = ContentUris.withAppendedId(CONTENT_URI, id.toLong())
                    context?.contentResolver?.notifyChange(newUri!!, null)
                    Log.d(TAG, "insert: URI de la nueva divisa -> $newUri")
                }

                runBlocking { job.join() }
                newUri
            }
            else -> {
                Log.e(TAG, "insert: URI no soportada: $uri")
                throw IllegalArgumentException("URI no soportada: $uri")
            }
        }
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        Log.d(TAG, "update: Método aún no implementado")
        return 0
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        Log.d(TAG, "delete: Método aún no implementado")
        return 0
    }

    override fun getType(uri: Uri): String? {
        Log.d(TAG, "getType: Revisando tipo de URI -> $uri")
        return when (uriMatcher.match(uri)) {
            CODE_DIVISAS -> "vnd.android.cursor.dir/vnd.$AUTHORITY.$TABLE_NAME"
            CODE_DIVISA_ID -> "vnd.android.cursor.item/vnd.$AUTHORITY.$TABLE_NAME"
            else -> {
                Log.e(TAG, "getType: URI no soportada -> $uri")
                throw IllegalArgumentException("URI no soportada: $uri")
            }
        }
    }
}
