package com.example.jlg_czg_sicenet.data

import android.util.Log
import android.content.Context
import androidx.work.*
import com.example.jlg_czg_sicenet.data.local.AcademicDataEntity
import com.example.jlg_czg_sicenet.data.local.SNLocalDao
import com.example.jlg_czg_sicenet.data.local.toEntity
import com.example.jlg_czg_sicenet.data.local.toModel
import com.example.jlg_czg_sicenet.model.ProfileStudent
import com.example.jlg_czg_sicenet.network.*
import com.example.jlg_czg_sicenet.workers.SyncAcademicDataWorker
import com.example.jlg_czg_sicenet.workers.SyncProfileWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

interface SNRepository {
    suspend fun acceso(matricula: String, contrasenia: String): Boolean
    suspend fun profile(matricula: String): ProfileStudent
    fun getProfileFlow(matricula: String): Flow<ProfileStudent?>
    
    suspend fun getCargaAcademica(matricula: String): String
    suspend fun getKardex(matricula: String): String
    suspend fun getUnidades(matricula: String): String
    suspend fun getFinales(matricula: String): String
    
    fun syncProfile(matricula: String)
    fun syncAcademicData(matricula: String)
    
    fun getAcademicDataFlow(matricula: String, dataType: String): Flow<AcademicDataEntity?>
    
    suspend fun getMatricula(): String
    fun logout()
}

class NetworSNRepository(
    private val snApiService: SICENETWService,
    private val snLocalDao: SNLocalDao,
    private val context: Context
) : SNRepository {
    
    private var userMatricula: String = ""
    private var sessionCookie: String? = null
    private val workManager = WorkManager.getInstance(context)

    override fun syncProfile(matricula: String) {
        val data = Data.Builder().putString("matricula", matricula).build()
        val request = OneTimeWorkRequestBuilder<SyncProfileWorker>()
            .setInputData(data)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        workManager.enqueueUniqueWork("sync_profile_$matricula", ExistingWorkPolicy.REPLACE, request)
    }

    override fun syncAcademicData(matricula: String) {
        val data = Data.Builder().putString("matricula", matricula).build()
        val request = OneTimeWorkRequestBuilder<SyncAcademicDataWorker>()
            .setInputData(data)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        workManager.enqueueUniqueWork("sync_academic_$matricula", ExistingWorkPolicy.REPLACE, request)
    }

    private fun escapeXml(input: String): String {
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    override suspend fun acceso(matricula: String, contrasenia: String): Boolean {
        Log.d("SNRepository", "===== INICIANDO AUTENTICACIÓN =====")
        return try {
            val safeMatricula = escapeXml(matricula)
            val safeContrasenia = escapeXml(contrasenia)
            val soapBody = bodyacceso.format(safeMatricula.uppercase(), safeContrasenia)
            
            val response = try {
                snApiService.acceso(soapBody.toRequestBody("text/xml;charset=utf-8".toMediaType()))
            } catch (e: HttpException) {
                return false
            }

            // Capturar cookie
            val cookieHeader = response.headers()["Set-Cookie"]
            if (!cookieHeader.isNullOrEmpty()) {
                sessionCookie = cookieHeader.split(';')[0]
            }

            val envelope = response.body()
            val result = envelope?.body?.accesoLoginResponse?.accesoLoginResult

            val isSuccess = result != null && (
                result.equals("true", ignoreCase = true) || 
                result == "1" || 
                result.contains("\"acceso\":true", ignoreCase = true)
            )

            if (isSuccess) {
                userMatricula = matricula
                // Trigger sync workers as requested (Case A)
                syncProfile(matricula)
                syncAcademicData(matricula)
                return true
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun profile(matricula: String): ProfileStudent {
        return try {
            val response = snApiService.perfil(sessionCookie, bodyperfil.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            val body = response.body()?.body
            val result = body?.getAlumnoAcademicoWithLineamientoResponse?.getAlumnoAcademicoWithLineamientoResult
                ?: body?.getAlumnoAcademicoResponse?.getAlumnoAcademicoResult
                ?: ""

            if (result.isEmpty()) return ProfileStudent(matricula = matricula)
            
            var processed = result
            if (processed.contains("&lt;")) {
                processed = processed.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&")
            }
            
            if (processed.trim().startsWith("{")) {
                val json = Json.parseToJsonElement(processed.trim()).jsonObject
                val profile = ProfileStudent(
                    matricula = json["matricula"]?.jsonPrimitive?.content ?: matricula,
                    nombre = json["nombre"]?.jsonPrimitive?.content ?: "",
                    carrera = json["carrera"]?.jsonPrimitive?.content ?: "",
                    semestre = json["semActual"]?.jsonPrimitive?.content ?: json["semestre"]?.jsonPrimitive?.content ?: "0",
                    promedio = json["promedio"]?.jsonPrimitive?.content ?: "0",
                    estado = json["estatus"]?.jsonPrimitive?.content ?: "",
                    statusMatricula = if (json["inscrito"]?.jsonPrimitive?.content?.equals("true", true) == true) "Activo" else "Inactivo",
                    especialidad = json["especialidad"]?.jsonPrimitive?.content ?: "",
                    cdtsReunidos = json["cdtosAcumulados"]?.jsonPrimitive?.content ?: "",
                    cdtsActuales = json["cdtosActuales"]?.jsonPrimitive?.content ?: "",
                    semActual = json["semActual"]?.jsonPrimitive?.content ?: "",
                    inscrito = json["inscrito"]?.jsonPrimitive?.content ?: "",
                    estatusAcademico = json["estatus"]?.jsonPrimitive?.content ?: "",
                    reinscripcionFecha = json["fechaReins"]?.jsonPrimitive?.content ?: "",
                    sinAdeudos = json["adeudo"]?.jsonPrimitive?.content ?: ""
                )
                
                // Guardar en local
                snLocalDao.insertProfile(profile.toEntity())
                return profile
            }
            ProfileStudent(matricula = matricula)
        } catch (e: Exception) {
            snLocalDao.getProfile(matricula)?.toModel() ?: ProfileStudent(matricula = matricula)
        }
    }

    override fun getProfileFlow(matricula: String): Flow<ProfileStudent?> {
        return snLocalDao.getProfileFlow(matricula).map { it?.toModel() }
    }

    override suspend fun getCargaAcademica(matricula: String): String {
        return try {
            val response = snApiService.getCargaAcademica(sessionCookie, bodyCarga.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            val result = response.body()?.body?.response?.result ?: ""
            if (result.isNotEmpty()) {
                snLocalDao.insertAcademicData(AcademicDataEntity(matricula, "CARGA", result))
            }
            result
        } catch (e: Exception) {
            snLocalDao.getAcademicData(matricula, "CARGA")?.data ?: ""
        }
    }

    override suspend fun getKardex(matricula: String): String {
        return try {
            val response = snApiService.getKardex(sessionCookie, bodyKardex.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            val result = response.body()?.body?.response?.result ?: ""
            if (result.isNotEmpty()) {
                snLocalDao.insertAcademicData(AcademicDataEntity(matricula, "KARDEX", result))
            }
            result
        } catch (e: Exception) {
            snLocalDao.getAcademicData(matricula, "KARDEX")?.data ?: ""
        }
    }

    override suspend fun getUnidades(matricula: String): String {
        return try {
            val response = snApiService.getUnidades(sessionCookie, bodyUnidades.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            val result = response.body()?.body?.response?.result ?: ""
            if (result.isNotEmpty()) {
                snLocalDao.insertAcademicData(AcademicDataEntity(matricula, "UNIDADES", result))
            }
            result
        } catch (e: Exception) {
            snLocalDao.getAcademicData(matricula, "UNIDADES")?.data ?: ""
        }
    }

    override suspend fun getFinales(matricula: String): String {
        return try {
            val response = snApiService.getFinales(sessionCookie, bodyFinal.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            val result = response.body()?.body?.response?.result ?: ""
            if (result.isNotEmpty()) {
                snLocalDao.insertAcademicData(AcademicDataEntity(matricula, "FINAL", result))
            }
            result
        } catch (e: Exception) {
            snLocalDao.getAcademicData(matricula, "FINAL")?.data ?: ""
        }
    }

    override fun getAcademicDataFlow(matricula: String, dataType: String): Flow<AcademicDataEntity?> {
        return snLocalDao.getAcademicDataFlow(matricula, dataType)
    }

    override suspend fun getMatricula(): String = userMatricula

    override fun logout() {
        userMatricula = ""
        sessionCookie = null
    }
}
