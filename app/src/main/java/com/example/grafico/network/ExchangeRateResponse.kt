package com.example.grafico.network

data class ExchangeRateResponse(
    val result: String,
    val conversion_rates: Map<String, Double>
)