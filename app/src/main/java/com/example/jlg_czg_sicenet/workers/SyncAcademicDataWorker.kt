package com.example.jlg_czg_sicenet.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.jlg_czg_sicenet.JLGSICENETApplication

class SyncAcademicDataWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val application = applicationContext as JLGSICENETApplication
        val repository = application.container.snRepository
        val matricula = inputData.getString("matricula") ?: return Result.failure()

        return try {
            // Sincronizar todo lo académico
            repository.getCargaAcademica(matricula)
            repository.getKardex(matricula)
            repository.getUnidades(matricula)
            repository.getFinales(matricula)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
