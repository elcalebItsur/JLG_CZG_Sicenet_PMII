package com.example.jlg_czg_sicenet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StudentProfileEntity::class, AcademicDataEntity::class], version = 1, exportSchema = false)
abstract class SNDatabase : RoomDatabase() {
    abstract fun snDao(): SNLocalDao // DAO para acceder a los datos de la base de datos


    companion object {
        @Volatile // vuelve a leer el valor de la variable desde la memoria principal, en lugar de desde la cache
        private var Instance: SNDatabase? = null

        fun getDatabase(context: Context): SNDatabase { // devuelve una instancia de la base de datos
            return Instance ?: synchronized(this) { // si no existe una instancia, la crea
                Room.databaseBuilder(context, SNDatabase::class.java, "sicenet_database") // nombre de la base de datos
                    .fallbackToDestructiveMigration() // si se actualiza la base de datos, se elimina y se crea una nueva
                    .build() // construye la base de datos
                    .also { Instance = it } // guarda la instancia en la variable
            }
        }
    }
}
