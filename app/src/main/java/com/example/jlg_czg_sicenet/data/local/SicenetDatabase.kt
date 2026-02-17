package com.example.jlg_czg_sicenet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AcademicLoadEntity::class,
        KardexEntity::class,
        GradesByUnitEntity::class,
        FinalGradesEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SicenetDatabase : RoomDatabase() {
    abstract fun academicLoadDao(): AcademicLoadDao
    abstract fun kardexDao(): KardexDao
    abstract fun gradesByUnitDao(): GradesByUnitDao
    abstract fun finalGradesDao(): FinalGradesDao

    companion object {
        @Volatile
        private var INSTANCE: SicenetDatabase? = null

        fun getDatabase(context: Context): SicenetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SicenetDatabase::class.java,
                    "sicenet_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}