package com.example.jlg_czg_sicenet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "final_grades")
data class FinalGradesEntity(
    @PrimaryKey
    val matricula: String,
    val data: String, // JSON string containing the final grades data
    val lastUpdated: Long = System.currentTimeMillis()
)