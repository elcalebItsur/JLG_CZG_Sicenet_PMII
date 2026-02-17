package com.example.jlg_czg_sicenet.data.local

interface LocalRepository {
    suspend fun saveAcademicLoad(matricula: String, data: String)
    suspend fun getAcademicLoad(matricula: String): String?
    suspend fun getAcademicLoadLastUpdate(matricula: String): Long?

    suspend fun saveKardex(matricula: String, data: String)
    suspend fun getKardex(matricula: String): String?
    suspend fun getKardexLastUpdate(matricula: String): Long?

    suspend fun saveGradesByUnit(matricula: String, data: String)
    suspend fun getGradesByUnit(matricula: String): String?
    suspend fun getGradesByUnitLastUpdate(matricula: String): Long?

    suspend fun saveFinalGrades(matricula: String, data: String)
    suspend fun getFinalGrades(matricula: String): String?
    suspend fun getFinalGradesLastUpdate(matricula: String): Long?

    suspend fun clearAllData(matricula: String)
}