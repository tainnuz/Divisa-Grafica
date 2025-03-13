package com.example.grafico.api

data class ExchangeRateResponse(
    val result: String,
    val conversion_rates: Map<String, Double>
)