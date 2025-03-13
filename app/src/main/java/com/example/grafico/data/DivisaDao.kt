package com.example.grafico.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.grafico.data.Divisa
import kotlinx.coroutines.flow.Flow

@Dao
interface DivisaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDivisa(divisa: Divisa): Long

    @Query("SELECT * FROM divisa_table ORDER BY fechaActualizacion DESC")
    fun getAllDivisas(): Flow<List<Divisa>>

    @Query("SELECT * FROM divisa_table ORDER BY fechaActualizacion DESC")
    fun getAllDivisasCursor(): android.database.Cursor

    @Query("SELECT * FROM divisa_table WHERE divisa = :divisa ORDER BY fechaActualizacion ASC")
    fun getHistorialDivisa(divisa: String): Flow<List<Divisa>>
}
