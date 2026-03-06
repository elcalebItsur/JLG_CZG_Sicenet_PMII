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
        // Leer el valor de entrada (matricula)
        val matricula = inputData.getString("matricula") ?: return Result.failure()

        return try {
            // Sincronizar todo lo académico
            repository.getCargaAcademica(matricula) //API -> guarda carga en ROOM
            repository.getKardex(matricula) //API -> guarda kardex en ROOM
            repository.getUnidades(matricula) //API -> guarda unidades en ROOM
            repository.getFinales(matricula) //API -> guarda finales en ROOM
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
