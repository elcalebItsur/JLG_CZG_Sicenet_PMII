package com.example.jlg_czg_sicenet.data.local

class RoomLocalRepository(
    private val database: SicenetDatabase
) : LocalRepository {

    override suspend fun saveAcademicLoad(matricula: String, data: String) {
        val entity = AcademicLoadEntity(
            matricula = matricula,
            data = data,
            lastUpdated = System.currentTimeMillis()
        )
        database.academicLoadDao().insert(entity)
    }

    override suspend fun getAcademicLoad(matricula: String): String? {
        return database.academicLoadDao().getByMatricula(matricula)?.data
    }

    override suspend fun getAcademicLoadLastUpdate(matricula: String): Long? {
        return database.academicLoadDao().getByMatricula(matricula)?.lastUpdated
    }

    override suspend fun saveKardex(matricula: String, data: String) {
        val entity = KardexEntity(
            matricula = matricula,
            data = data,
            lastUpdated = System.currentTimeMillis()
        )
        database.kardexDao().insert(entity)
    }

    override suspend fun getKardex(matricula: String): String? {
        return database.kardexDao().getByMatricula(matricula)?.data
    }

    override suspend fun getKardexLastUpdate(matricula: String): Long? {
        return database.kardexDao().getByMatricula(matricula)?.lastUpdated
    }

    override suspend fun saveGradesByUnit(matricula: String, data: String) {
        val entity = GradesByUnitEntity(
            matricula = matricula,
            data = data,
            lastUpdated = System.currentTimeMillis()
        )
        database.gradesByUnitDao().insert(entity)
    }

    override suspend fun getGradesByUnit(matricula: String): String? {
        return database.gradesByUnitDao().getByMatricula(matricula)?.data
    }

    override suspend fun getGradesByUnitLastUpdate(matricula: String): Long? {
        return database.gradesByUnitDao().getByMatricula(matricula)?.lastUpdated
    }

    override suspend fun saveFinalGrades(matricula: String, data: String) {
        val entity = FinalGradesEntity(
            matricula = matricula,
            data = data,
            lastUpdated = System.currentTimeMillis()
        )
        database.finalGradesDao().insert(entity)
    }

    override suspend fun getFinalGrades(matricula: String): String? {
        return database.finalGradesDao().getByMatricula(matricula)?.data
    }

    override suspend fun getFinalGradesLastUpdate(matricula: String): Long? {
        return database.finalGradesDao().getByMatricula(matricula)?.lastUpdated
    }

    override suspend fun clearAllData(matricula: String) {
        database.academicLoadDao().deleteByMatricula(matricula)
        database.kardexDao().deleteByMatricula(matricula)
        database.gradesByUnitDao().deleteByMatricula(matricula)
        database.finalGradesDao().deleteByMatricula(matricula)
    }
}