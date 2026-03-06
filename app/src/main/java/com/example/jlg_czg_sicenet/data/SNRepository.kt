package com.example.jlg_czg_sicenet.data

import android.util.Log
import androidx.work.*
import com.example.jlg_czg_sicenet.data.local.AcademicDataEntity
import com.example.jlg_czg_sicenet.data.local.SNLocalDao
import com.example.jlg_czg_sicenet.data.local.toEntity
import com.example.jlg_czg_sicenet.data.local.toModel
import com.example.jlg_czg_sicenet.model.*
import com.example.jlg_czg_sicenet.network.*
import com.example.jlg_czg_sicenet.workers.SyncAcademicDataWorker
import com.example.jlg_czg_sicenet.workers.SyncProfileWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.decodeFromString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import android.content.Context

interface SNRepository {
    suspend fun acceso(matricula: String, contrasenia: String): Boolean
    suspend fun profile(matricula: String): ProfileStudent
    fun getProfileFlow(matricula: String): Flow<ProfileStudent?>
    
    suspend fun getCargaAcademica(matricula: String): List<MateriaCarga>
    suspend fun getKardex(matricula: String): KardexModel
    suspend fun getUnidades(matricula: String): List<CalificacionUnidad>
    suspend fun getFinales(matricula: String): List<CalificacionFinal>
    
    fun syncProfile(matricula: String)
    fun syncAcademicData(matricula: String)
    
    fun getAcademicDataFlow(matricula: String, dataType: String): Flow<AcademicDataEntity?>
    
    fun getCargaFlow(matricula: String): Flow<List<MateriaCarga>>
    fun getKardexFlow(matricula: String): Flow<KardexModel?>
    fun getUnidadesFlow(matricula: String): Flow<List<CalificacionUnidad>>
    fun getFinalesFlow(matricula: String): Flow<List<CalificacionFinal>>
    
    suspend fun getMatricula(): String
    fun logout()

    fun isSessionSaved(): Boolean
    fun saveSessionState(isLogged: Boolean)
    fun saveMatricula(matricula: String)
    fun getSavedMatricula(): String
    suspend fun validateSession(): Boolean
    fun saveSession(matricula: String)
    fun clearSession()
}

class NetworSNRepository(
    private val snApiService: SICENETWService,
    private val snLocalDao: SNLocalDao,
    private val context: Context
) : SNRepository {

    private var userMatricula: String = ""
    // private var sessionCookie: String? = null
    private val workManager = WorkManager.getInstance(context)
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
        coerceInputValues = true
    }

    private fun normalizeMatricula(m: String): String = m.trim().uppercase()

    private fun cleanJson(input: String): String {
        if (input.isEmpty()) return ""
        var cleaned = input.trim()
        // Decodificar entidades comunes si están presentes
        if (cleaned.contains("&lt;") || cleaned.contains("&quot;") || cleaned.contains("&amp;")) {
            cleaned = cleaned.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
        }

        // Extraer específicamente el contenido entre [] o {} para evitar ruido XML
        val firstBracket = cleaned.indexOf('[')
        val firstBrace = cleaned.indexOf('{')
        val start = if (firstBracket != -1 && (firstBrace == -1 || firstBracket < firstBrace)) firstBracket else firstBrace

        if (start != -1) {
            val lastBracket = cleaned.lastIndexOf(']')
            val lastBrace = cleaned.lastIndexOf('}')
            val end = if (lastBracket != -1 && lastBracket > lastBrace) lastBracket else lastBrace

            if (end != -1 && end > start) {
                cleaned = cleaned.substring(start, end + 1)
            }
        }

        // Remover comillas envolventes si el servicio devolvió el JSON como string entrecomillado
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length - 1)
        }

        // Reemplazar secuencias de escape comunes (ej: \" o \\)
        cleaned = cleaned.replace("\\\"", "\"")
            .replace("\\\\", "\\")

        cleaned = cleaned.trim()

        Log.d("SNRepository", "cleanJson -> length=${cleaned.length} preview=${cleaned.take(80)}")

        return cleaned
    }

    override fun getCargaFlow(matricula: String): Flow<List<MateriaCarga>> =
        snLocalDao.getAcademicDataFlow(normalizeMatricula(matricula), "CARGA").map { 
            it?.let { 
                try { 
                    val cleaned = cleanJson(it.data)
                    json.decodeFromString<List<MateriaCarga>>(cleaned) 
                } catch(e: Exception) { 
                    Log.e("SNRepository", "Error parseando CARGA: ${e.message}")
                    emptyList() 
                } 
            } ?: emptyList()
        }

    override fun getKardexFlow(matricula: String): Flow<KardexModel?> =
        snLocalDao.getAcademicDataFlow(normalizeMatricula(matricula), "KARDEX").map { 
            it?.let { 
                try { 
                    val cleaned = cleanJson(it.data)
                    json.decodeFromString<KardexModel>(cleaned) 
                } catch(e: Exception) { 
                    Log.e("SNRepository", "Error parseando KARDEX: ${e.message}")
                    null 
                } 
            }
        }

    override fun getUnidadesFlow(matricula: String): Flow<List<CalificacionUnidad>> =
        snLocalDao.getAcademicDataFlow(normalizeMatricula(matricula), "UNIDADES").map { 
            it?.let { 
                try { 
                    val cleaned = cleanJson(it.data)
                    json.decodeFromString<List<CalificacionUnidad>>(cleaned) 
                } catch(e: Exception) { 
                    Log.e("SNRepository", "Error parseando UNIDADES: ${e.message}")
                    emptyList() 
                } 
            } ?: emptyList()
        }

    override fun getFinalesFlow(matricula: String): Flow<List<CalificacionFinal>> =
        snLocalDao.getAcademicDataFlow(normalizeMatricula(matricula), "FINAL").map { 
            it?.let { 
                try { 
                    val cleaned = cleanJson(it.data)
                    json.decodeFromString<List<CalificacionFinal>>(cleaned) 
                } catch(e: Exception) { 
                    Log.e("SNRepository", "Error parseando FINAL: ${e.message}")
                    emptyList() 
                } 
            } ?: emptyList()
        }

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

            /*
            val cookieHeader = response.headers()["Set-Cookie"]
            if (!cookieHeader.isNullOrEmpty()) {
                sessionCookie = cookieHeader.split(';')[0]
            }
            */

            val envelope = response.body()
            val result = envelope?.body?.accesoLoginResponse?.accesoLoginResult

            val isSuccess = result != null && (
                result.equals("true", ignoreCase = true) || 
                result == "1" || 
                result.contains("\"acceso\":true", ignoreCase = true)
            )

            if (isSuccess) {
                userMatricula = normalizeMatricula(matricula)
                syncProfile(userMatricula)
                syncAcademicData(userMatricula)
                saveMatricula(matricula)
                return true
            }
            false
        } catch (e: Exception) {
            Log.e("SNRepository", "Error en acceso: ${e.message}")
            false
        }
    }

    override suspend fun profile(matricula: String): ProfileStudent {
        return try {
            val response = snApiService.perfil(bodyperfil.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            val body = response.body()?.body
            val result = body?.getAlumnoAcademicoWithLineamientoResponse?.getAlumnoAcademicoWithLineamientoResult
                ?: body?.getAlumnoAcademicoResponse?.getAlumnoAcademicoResult
                ?: ""

            if (result.isEmpty()) return ProfileStudent(matricula = matricula)
            
            val processed = cleanJson(result)
            
            if (processed.startsWith("{")) {
                val jsonEl = json.parseToJsonElement(processed).jsonObject
                val profile = ProfileStudent(
                    matricula = jsonEl["matricula"]?.jsonPrimitive?.content ?: matricula,
                    nombre = jsonEl["nombre"]?.jsonPrimitive?.content ?: "",
                    carrera = jsonEl["carrera"]?.jsonPrimitive?.content ?: "",
                    semestre = jsonEl["semActual"]?.jsonPrimitive?.content ?: jsonEl["semestre"]?.jsonPrimitive?.content ?: "0",
                    promedio = jsonEl["promedio"]?.jsonPrimitive?.content ?: "0",
                    estado = jsonEl["estatus"]?.jsonPrimitive?.content ?: "",
                    statusMatricula = if (jsonEl["inscrito"]?.jsonPrimitive?.content?.equals("true", true) == true) "Activo" else "Inactivo",
                    especialidad = jsonEl["especialidad"]?.jsonPrimitive?.content ?: "",
                    cdtsReunidos = jsonEl["cdtosAcumulados"]?.jsonPrimitive?.content ?: "",
                    cdtsActuales = jsonEl["cdtosActuales"]?.jsonPrimitive?.content ?: "",
                    semActual = jsonEl["semActual"]?.jsonPrimitive?.content ?: "",
                    inscrito = jsonEl["inscrito"]?.jsonPrimitive?.content ?: "",
                    estatusAcademico = jsonEl["estatus"]?.jsonPrimitive?.content ?: "",
                    reinscripcionFecha = jsonEl["fechaReins"]?.jsonPrimitive?.content ?: "",
                    sinAdeudos = jsonEl["adeudo"]?.jsonPrimitive?.content ?: ""
                )
                snLocalDao.insertProfile(profile.toEntity())
                return profile
            }
            ProfileStudent(matricula = matricula)
        } catch (e: Exception) {
            snLocalDao.getProfile(matricula)?.toModel() ?: ProfileStudent(matricula = matricula)
        }
    }

    override fun getProfileFlow(matricula: String): Flow<ProfileStudent?> {
        return snLocalDao.getProfileFlow(normalizeMatricula(matricula)).map { it?.toModel() }
    }

    override suspend fun getCargaAcademica(matricula: String): List<MateriaCarga> {
        val m = normalizeMatricula(matricula)
        return try {
            val response = snApiService.getCargaAcademica( bodyCarga.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            val result = response.body()?.body?.response?.result ?: ""
            Log.d("SNRepository", "Carga RAW: $result")
            if (result.isNotEmpty()) {
                snLocalDao.insertAcademicData(AcademicDataEntity(m, "CARGA", result))
                val cleaned = cleanJson(result)
                json.decodeFromString<List<MateriaCarga>>(cleaned)
            } else emptyList()
        } catch (e: Exception) {
            Log.e("SNRepository", "Error en getCargaAcademica: ${e.message}")
            val local = snLocalDao.getAcademicData(m, "CARGA")?.data
            if (local != null) try { json.decodeFromString(cleanJson(local)) } catch(e: Exception) { emptyList() } else emptyList()
        }
    }

    override suspend fun getKardex(matricula: String): KardexModel {
        val m = normalizeMatricula(matricula)
        return try {
            val response = snApiService.getKardex(bodyKardex.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            val result = response.body()?.body?.response?.result ?: ""
            Log.d("SNRepository", "Kardex RAW: $result")
            if (result.isNotEmpty()) {
                snLocalDao.insertAcademicData(AcademicDataEntity(m, "KARDEX", result))
                json.decodeFromString<KardexModel>(cleanJson(result))
            } else KardexModel()
        } catch (e: Exception) {
            Log.e("SNRepository", "Error en getKardex: ${e.message}")
            val local = snLocalDao.getAcademicData(m, "KARDEX")?.data
            if (local != null) try { json.decodeFromString(cleanJson(local)) } catch(e: Exception) { KardexModel() } else KardexModel()
        }
    }

    override suspend fun getUnidades(matricula: String): List<CalificacionUnidad> {
        val m = normalizeMatricula(matricula)
        return try {
            val response = snApiService.getUnidades(bodyUnidades.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            val result = response.body()?.body?.response?.result ?: ""
            Log.d("SNRepository", "Unidades RAW: $result")
            if (result.isNotEmpty()) {
                snLocalDao.insertAcademicData(AcademicDataEntity(m, "UNIDADES", result))
                json.decodeFromString<List<CalificacionUnidad>>(cleanJson(result))
            } else emptyList()
        } catch (e: Exception) {
            Log.e("SNRepository", "Error en getUnidades: ${e.message}")
            val local = snLocalDao.getAcademicData(m, "UNIDADES")?.data
            if (local != null) try { json.decodeFromString(cleanJson(local)) } catch(e: Exception) { emptyList() } else emptyList()
        }
    }

    override suspend fun getFinales(matricula: String): List<CalificacionFinal> {
        val m = normalizeMatricula(matricula)
        return try {
            val response = snApiService.getFinales(bodyFinal.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            val result = response.body()?.body?.response?.result ?: ""
            Log.d("SNRepository", "Finales RAW: $result")
            if (result.isNotEmpty()) {
                snLocalDao.insertAcademicData(AcademicDataEntity(m, "FINAL", result))
                json.decodeFromString<List<CalificacionFinal>>(cleanJson(result))
            } else emptyList()
        } catch (e: Exception) {
            Log.e("SNRepository", "Error en getFinales: ${e.message}")
            val local = snLocalDao.getAcademicData(m, "FINAL")?.data
            if (local != null) try { json.decodeFromString(cleanJson(local)) } catch(e: Exception) { emptyList() } else emptyList()
        }
    }

    override fun getAcademicDataFlow(matricula: String, dataType: String): Flow<AcademicDataEntity?> {
        return snLocalDao.getAcademicDataFlow(normalizeMatricula(matricula), dataType)
    }

    override suspend fun getMatricula(): String = userMatricula

    override fun logout() {
        userMatricula = ""
    }

    override fun isSessionSaved(): Boolean {
        val prefs = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_logged", false)
    }

    override fun saveSessionState(isLogged: Boolean) {
        val prefs = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_logged", isLogged).apply()
    }

    override fun saveSession(matricula: String) {
        val prefs = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("is_logged", true)
            .putString("matricula", matricula)
            .apply()
    }

    override fun saveMatricula(matricula: String) {
        val prefs = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("matricula", matricula).apply()
    }

    override fun getSavedMatricula(): String {
        val prefs = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
        return prefs.getString("matricula", "") ?: ""
    }
    override fun clearSession() {
        val prefs = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    override suspend fun validateSession(): Boolean {
        return try {
            val m = getSavedMatricula()
            profile(m)
            true
        } catch (e: Exception) {
            saveSessionState(false)
            false
        }
    }

}
