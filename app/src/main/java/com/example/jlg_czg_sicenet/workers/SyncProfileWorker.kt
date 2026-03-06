package com.example.jlg_czg_sicenet.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.jlg_czg_sicenet.JLGSICENETApplication
import com.example.jlg_czg_sicenet.data.SNRepository

class SyncProfileWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Obtener el repositorio de datos desde el Application
        val application = applicationContext as JLGSICENETApplication
        val repository = application.container.snRepository
        // Leer el valor de entrada
        val matricula = inputData.getString("matricula") ?: return Result.failure()

        return try {
            // Llama a la función de sincronización, hace la llamada a la API y almacena los datos si la respuesta es exitosa
            repository.profile(matricula)
            // Devuelve un resultado exitoso
            Result.success()
        } catch (e: Exception) {
            // si algo falla, devuelve un resultado de reintento, workmager se encarga de volver a intentar
            Result.retry()
        }
    }
}
