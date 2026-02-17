package com.example.jlg_czg_sicenet.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.jlg_czg_sicenet.data.AppContainer
import com.example.jlg_czg_sicenet.data.DefaultAppContainer

class StoreAcademicDataWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val container: AppContainer = DefaultAppContainer(appContext)

    override suspend fun doWork(): Result {
        val matricula = inputData.getString(KEY_MATRICULA) ?: return Result.failure()
        val data = inputData.getString(KEY_DATA) ?: return Result.failure()
        val dataType = inputData.getString(KEY_DATA_TYPE) ?: return Result.failure()

        return try {
            Log.d(TAG, "Storing $dataType data for matricula: $matricula")

            when (dataType) {
                DATA_TYPE_ACADEMIC_LOAD -> container.localRepository.saveAcademicLoad(matricula, data)
                DATA_TYPE_KARDEX -> container.localRepository.saveKardex(matricula, data)
                DATA_TYPE_GRADES_BY_UNIT -> container.localRepository.saveGradesByUnit(matricula, data)
                DATA_TYPE_FINAL_GRADES -> container.localRepository.saveFinalGrades(matricula, data)
                else -> return Result.failure(workDataOf(ERROR_KEY to "Unknown data type: $dataType"))
            }

            Log.d(TAG, "Successfully stored $dataType data")
            Result.success(workDataOf(
                OUTPUT_MATRICULA_KEY to matricula,
                OUTPUT_DATA_TYPE_KEY to dataType
            ))

        } catch (e: Exception) {
            Log.e(TAG, "Error storing $dataType data", e)
            Result.failure(workDataOf(ERROR_KEY to e.message))
        }
    }

    companion object {
        const val TAG = "StoreAcademicDataWorker"
        const val KEY_MATRICULA = "matricula"
        const val KEY_DATA = "data"
        const val KEY_DATA_TYPE = "data_type"
        const val OUTPUT_MATRICULA_KEY = "output_matricula"
        const val OUTPUT_DATA_TYPE_KEY = "output_data_type"
        const val ERROR_KEY = "error"

        const val DATA_TYPE_ACADEMIC_LOAD = "academic_load"
        const val DATA_TYPE_KARDEX = "kardex"
        const val DATA_TYPE_GRADES_BY_UNIT = "grades_by_unit"
        const val DATA_TYPE_FINAL_GRADES = "final_grades"
    }
}