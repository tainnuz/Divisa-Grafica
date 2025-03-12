package com.example.grafico

import android.app.Application
import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Constraints
import androidx.work.NetworkType
import com.example.grafico.data.DivisaWorker
import com.example.grafico.data.local.Divisa
import com.example.grafico.data.local.DivisaDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DivisaViewModel(application: Application) : AndroidViewModel(application) {

    private val contentResolver: ContentResolver = application.contentResolver
    private val divisaDao = DivisaDatabase.getDatabase(application).divisaDao()

    private val _divisas = MutableLiveData<List<Divisa>>()
    val divisas: LiveData<List<Divisa>> get() = _divisas

    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            fetchDivisas() // Se actualizan los datos cuando hay cambios en la BD
        }
    }

    init {
        contentResolver.registerContentObserver(
            Uri.parse("content://com.example.grafico/divisa_table"),
            true,
            contentObserver
        )
        fetchDivisas() // Cargar datos al inicio
    }

    fun fetchDivisas() {
        CoroutineScope(Dispatchers.IO).launch {
            divisaDao.getAllDivisas().collect { divisasList ->
                _divisas.postValue(divisasList)
            }
        }
    }

    // Método para actualizar los datos llamando al DivisaWorker
    fun actualizarDivisas() {
        val workRequest = OneTimeWorkRequestBuilder<DivisaWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        // Encolar el trabajo para que se ejecute
        WorkManager.getInstance(getApplication()).enqueue(workRequest)

        // Log para verificar que el Worker se está ejecutando
        println("Actualización de divisas iniciada")
    }

    override fun onCleared() {
        super.onCleared()
        contentResolver.unregisterContentObserver(contentObserver)
    }
}
