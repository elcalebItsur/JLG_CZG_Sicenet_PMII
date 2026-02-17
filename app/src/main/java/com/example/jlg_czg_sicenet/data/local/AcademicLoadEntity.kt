package com.example.jlg_czg_sicenet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "academic_load")
data class AcademicLoadEntity(
    @PrimaryKey
    val matricula: String,
    val data: String, // JSON string containing the academic load data
    val lastUpdated: Long = System.currentTimeMillis()
)