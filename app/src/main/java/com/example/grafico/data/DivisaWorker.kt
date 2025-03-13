package com.example.grafico.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.grafico.api.ApiService
import com.example.grafico.api.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DivisaWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val startTime = System.currentTimeMillis()

        try {
            val apiService = RetrofitInstance.retrofit.create(ApiService::class.java)
            val response = apiService.getExchangeRates("MXN")
            val networkTime = System.currentTimeMillis()

            Log.d("DivisaWorker", "Tiempo de la solicitud de red: ${networkTime - startTime} ms")

            if (response.isSuccessful) {
                val divisaRates = response.body()?.conversion_rates
                Log.d("DivisaWorker", "Datos obtenidos: $divisaRates")

                withContext(Dispatchers.IO) {
                    val contentResolver = applicationContext.contentResolver
                    val contentUri = Uri.parse("content://com.example.grafico/divisa_table")

                    divisaRates?.forEach { (currency, value) ->
                        val values = ContentValues().apply {
                            put("divisa", currency)
                            put("valor", value)
                        }
                        contentResolver.insert(contentUri, values)
                    }
                }

                val insertTime = System.currentTimeMillis()
                Log.d("DivisaWorker", "Tiempo de inserción de datos: ${insertTime - networkTime} ms")

                return Result.success()
            } else {
                Log.e("DivisaWorker", "Error en la respuesta de la API")
                return Result.failure()
            }
        } catch (e: Exception) {
            Log.e("DivisaWorker", "Excepción: ${e.message}")
            return Result.failure()
        }
    }
}
