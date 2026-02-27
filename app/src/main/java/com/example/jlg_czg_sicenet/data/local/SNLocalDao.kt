package com.example.jlg_czg_sicenet.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SNLocalDao {
    // Profile
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: StudentProfileEntity)

    @Query("SELECT * FROM student_profile WHERE matricula = :matricula")
    suspend fun getProfile(matricula: String): StudentProfileEntity?

    @Query("SELECT * FROM student_profile WHERE matricula = :matricula")
    fun getProfileFlow(matricula: String): Flow<StudentProfileEntity?>

    // Academic Data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAcademicData(data: AcademicDataEntity)

    @Query("SELECT * FROM academic_data WHERE matricula = :matricula AND dataType = :dataType")
    suspend fun getAcademicData(matricula: String, dataType: String): AcademicDataEntity?

    @Query("SELECT * FROM academic_data WHERE matricula = :matricula AND dataType = :dataType")
    fun getAcademicDataFlow(matricula: String, dataType: String): Flow<AcademicDataEntity?>

    @Query("DELETE FROM academic_data WHERE matricula = :matricula")
    suspend fun clearAcademicData(matricula: String)
}
