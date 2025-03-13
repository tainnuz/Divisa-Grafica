package com.example.grafico.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("latest/MXN ")
    suspend fun getExchangeRates(@Query("baseCurrency") baseCurrency: String): Response<ExchangeRateResponse>
}
