package com.example.jlg_czg_sicenet.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SNLocalDao {
    // Profile
    @Insert(onConflict = OnConflictStrategy.REPLACE) // si hay un conflicto, reemplaza
    suspend fun insertProfile(profile: StudentProfileEntity)

    @Query("SELECT * FROM student_profile WHERE matricula = :matricula") // devuelve un StudentProfileEntity
    suspend fun getProfile(matricula: String): StudentProfileEntity? //

    @Query("SELECT * FROM student_profile WHERE matricula = :matricula")
    fun getProfileFlow(matricula: String): Flow<StudentProfileEntity?> // devuelve un Flow de StudentProfileEntity, es Flow porque puede cambiar

    @Query("DELETE FROM student_profile WHERE matricula = :matricula")
    suspend fun clearProfile(matricula: String)




    // Academic Data
    @Insert(onConflict = OnConflictStrategy.REPLACE) // si hay un conflicto, reemplaza
    suspend fun insertAcademicData(data: AcademicDataEntity)

    @Query("SELECT * FROM academic_data WHERE matricula = :matricula AND dataType = :dataType")
    suspend fun getAcademicData(matricula: String, dataType: String): AcademicDataEntity? // devuelve un AcademicDataEntity


    @Query("SELECT * FROM academic_data WHERE matricula = :matricula AND dataType = :dataType")
    fun getAcademicDataFlow(matricula: String, dataType: String): Flow<AcademicDataEntity?> // devuelve un Flow de AcademicDataEntity, es Flow porque puede cambiar


    @Query("DELETE FROM academic_data WHERE matricula = :matricula")
    suspend fun clearAcademicData(matricula: String)
}
