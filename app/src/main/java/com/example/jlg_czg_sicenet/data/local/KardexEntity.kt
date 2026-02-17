package com.example.jlg_czg_sicenet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kardex")
data class KardexEntity(
    @PrimaryKey
    val matricula: String,
    val data: String, // JSON string containing the kardex data
    val lastUpdated: Long = System.currentTimeMillis()
)