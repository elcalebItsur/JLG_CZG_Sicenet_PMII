package com.example.jlg_czg_sicenet.workers

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.work.*
import java.util.concurrent.TimeUnit

class SyncManager(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedules sync for profile and academic data after successful login
     */
    fun schedulePostLoginSync(matricula: String) {
        Log.d(TAG, "Scheduling post-login sync for matricula: $matricula")

        // Create unique work names
        val profileSyncWorkName = "profile_sync_$matricula"
        val academicLoadSyncWorkName = "academic_load_sync_$matricula"

        // Schedule profile sync (access + perfil)
        scheduleProfileSync(matricula, profileSyncWorkName)

        // Schedule academic data sync
        scheduleAcademicDataSync(matricula, academicLoadSyncWorkName)
    }

    /**
     * Schedules sync for specific academic data when requested
     */
    fun scheduleAcademicDataSync(matricula: String, dataType: String) {
        Log.d(TAG, "Scheduling academic data sync for $dataType, matricula: $matricula")

        val workName = "${dataType}_sync_$matricula"

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncAcademicDataWorker>()
            .setInputData(workDataOf(
                SyncAcademicDataWorker.KEY_MATRICULA to matricula,
                SyncAcademicDataWorker.KEY_DATA_TYPE to dataType
            ))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.beginUniqueWork(
            workName,
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        ).enqueue()
    }

    private fun scheduleProfileSync(matricula: String, workName: String) {
        // For now, profile sync is handled separately
        // This could be extended to sync profile data as well
        Log.d(TAG, "Profile sync scheduled (placeholder)")
    }

    private fun scheduleAcademicDataSync(matricula: String, workName: String) {
        // Schedule sync for all academic data types
        val dataTypes = listOf(
            FetchAcademicDataWorker.DATA_TYPE_ACADEMIC_LOAD,
            FetchAcademicDataWorker.DATA_TYPE_KARDEX,
            FetchAcademicDataWorker.DATA_TYPE_GRADES_BY_UNIT,
            FetchAcademicDataWorker.DATA_TYPE_FINAL_GRADES
        )

        dataTypes.forEach { dataType ->
            scheduleAcademicDataSync(matricula, dataType)
        }
    }

    /**
     * Cancels all sync work for a specific matricula
     */
    fun cancelSyncForMatricula(matricula: String) {
        Log.d(TAG, "Canceling sync work for matricula: $matricula")

        val workNames = listOf(
            "profile_sync_$matricula",
            "academic_load_sync_$matricula",
            "kardex_sync_$matricula",
            "grades_by_unit_sync_$matricula",
            "final_grades_sync_$matricula"
        )

        workNames.forEach { workName ->
            workManager.cancelUniqueWork(workName)
        }
    }

    /**
     * Gets the status of sync work for a specific data type
     */
    fun getSyncStatus(matricula: String, dataType: String): LiveData<WorkInfo> {
        val workName = "${dataType}_sync_$matricula"
        return workManager.getWorkInfosForUniqueWorkLiveData(workName)
            .map { workInfos ->
                workInfos.firstOrNull() ?: WorkInfo()
            }
    }

    companion object {
        const val TAG = "SyncManager"
    }
}