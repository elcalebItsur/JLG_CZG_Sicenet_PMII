package com.example.jlg_czg_sicenet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AcademicLoadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(academicLoad: AcademicLoadEntity)

    @Update
    suspend fun update(academicLoad: AcademicLoadEntity)

    @Query("SELECT * FROM academic_load WHERE matricula = :matricula")
    suspend fun getByMatricula(matricula: String): AcademicLoadEntity?

    @Query("DELETE FROM academic_load WHERE matricula = :matricula")
    suspend fun deleteByMatricula(matricula: String)
}