package com.example.grafico.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "divisa_table")
data class Divisa(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val divisa: String,
    val valor: Double,
    val fechaActualizacion: Long = System.currentTimeMillis()
)
