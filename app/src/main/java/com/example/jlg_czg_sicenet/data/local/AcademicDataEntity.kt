package com.example.jlg_czg_sicenet.data.local

import androidx.room.Entity

// Entidad para almacenar datos académicos en la base de datos
@Entity(tableName = "academic_data", primaryKeys = ["matricula", "dataType"]) //
data class AcademicDataEntity( // clase para almacenar datos académicos en la base de datos
    val matricula: String, // matrícula del alumno
    val dataType: String, // "CARGA", "KARDEX", "UNIDADES", "FINAL" // tipo de datos
    val data: String, // JSON result
    val lastUpdated: Long = System.currentTimeMillis() // tiempo de actualización
)
