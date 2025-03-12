package com.example.grafico.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Divisa::class], version = 1, exportSchema = false)
abstract class DivisaDatabase : RoomDatabase() {

    abstract fun divisaDao(): DivisaDao

    companion object {
        @Volatile
        private var INSTANCE: DivisaDatabase? = null

        fun getDatabase(context: Context): DivisaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DivisaDatabase::class.java,
                    "divisa_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
