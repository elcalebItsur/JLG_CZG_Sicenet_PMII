package com.example.jlg_czg_sicenet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface GradesByUnitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gradesByUnit: GradesByUnitEntity)

    @Update
    suspend fun update(gradesByUnit: GradesByUnitEntity)

    @Query("SELECT * FROM grades_by_unit WHERE matricula = :matricula")
    suspend fun getByMatricula(matricula: String): GradesByUnitEntity?

    @Query("DELETE FROM grades_by_unit WHERE matricula = :matricula")
    suspend fun deleteByMatricula(matricula: String)
}