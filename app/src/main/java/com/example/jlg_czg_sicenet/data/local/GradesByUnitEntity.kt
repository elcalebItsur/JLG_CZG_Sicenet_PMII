package com.example.jlg_czg_sicenet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grades_by_unit")
data class GradesByUnitEntity(
    @PrimaryKey
    val matricula: String,
    val data: String, // JSON string containing the grades by unit data
    val lastUpdated: Long = System.currentTimeMillis()
)