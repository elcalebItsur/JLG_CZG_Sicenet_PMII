package com.example.jlg_czg_sicenet.data.local

import androidx.room.Entity

@Entity(tableName = "academic_data", primaryKeys = ["matricula", "dataType"])
data class AcademicDataEntity(
    val matricula: String,
    val dataType: String, // "CARGA", "KARDEX", "UNIDADES", "FINAL"
    val data: String, // JSON result
    val lastUpdated: Long = System.currentTimeMillis()
)
