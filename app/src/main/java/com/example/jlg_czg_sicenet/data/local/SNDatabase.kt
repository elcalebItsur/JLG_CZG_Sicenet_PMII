package com.example.jlg_czg_sicenet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StudentProfileEntity::class, AcademicDataEntity::class], version = 1, exportSchema = false)
abstract class SNDatabase : RoomDatabase() {
    abstract fun snDao(): SNLocalDao

    companion object {
        @Volatile
        private var Instance: SNDatabase? = null

        fun getDatabase(context: Context): SNDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, SNDatabase::class.java, "sicenet_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
