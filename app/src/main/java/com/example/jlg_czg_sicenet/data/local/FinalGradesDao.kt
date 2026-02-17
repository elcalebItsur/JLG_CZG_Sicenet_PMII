package com.example.jlg_czg_sicenet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface FinalGradesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(finalGrades: FinalGradesEntity)

    @Update
    suspend fun update(finalGrades: FinalGradesEntity)

    @Query("SELECT * FROM final_grades WHERE matricula = :matricula")
    suspend fun getByMatricula(matricula: String): FinalGradesEntity?

    @Query("DELETE FROM final_grades WHERE matricula = :matricula")
    suspend fun deleteByMatricula(matricula: String)
}