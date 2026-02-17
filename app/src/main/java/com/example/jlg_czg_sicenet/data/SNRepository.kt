package com.example.jlg_czg_sicenet.data

import android.util.Log
import com.example.jlg_czg_sicenet.model.ProfileStudent
import com.example.jlg_czg_sicenet.model.Usuario
import com.example.jlg_czg_sicenet.network.SICENETWService
import com.example.jlg_czg_sicenet.network.bodyacceso
import com.example.jlg_czg_sicenet.network.bodyperfil
import com.example.jlg_czg_sicenet.network.bodyCargaAcademica
import com.example.jlg_czg_sicenet.network.bodyKardex
import com.example.jlg_czg_sicenet.network.bodyCalifUnidades
import com.example.jlg_czg_sicenet.network.bodyCalifFinal
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

interface SNRepository {
    suspend fun acceso(matricula: String, contrasenia: String): Boolean
    suspend fun profile(matricula: String): ProfileStudent
    suspend fun getCargaAcademica(): String
    suspend fun getKardex(): String
    suspend fun getCalifUnidades(): String
    suspend fun getCalifFinal(): String
    suspend fun getMatricula(): String
    fun logout()
}

class NetworSNRepository(
    private val snApiService: SICENETWService
) : SNRepository {
    
    private var userMatricula: String = ""
    private var sessionCookie: String? = null

    private fun escapeXml(input: String): String {
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    override suspend fun acceso(matricula: String, contrasenia: String): Boolean {
        Log.d("SNRepository", "===== INICIANDO AUTENTICACIÓN =====")
        Log.d("SNRepository", "Matrícula: $matricula")
        
        return try {
            val safeMatricula = escapeXml(matricula)
            val safeContrasenia = escapeXml(contrasenia)
            val soapBody = bodyacceso.format(safeMatricula.uppercase(), safeContrasenia)
            
            Log.d("SNRepository", "Enviando SOAP Body...")
            
            val response = try {
                snApiService.acceso(soapBody.toRequestBody("text/xml;charset=utf-8".toMediaType()))
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("SNRepository", "HTTP Error ${e.code()}: $errorBody")
                return false
            }

            // Capturar cookie de sesión desde encabezados
            try {
                val cookieHeader = response.headers()["Set-Cookie"]
                if (!cookieHeader.isNullOrEmpty()) {
                    sessionCookie = cookieHeader.split(';')[0]
                    Log.d("SNRepository", "Cookie de sesión capturada: $sessionCookie")
                }
            } catch (e: Exception) {
                Log.w("SNRepository", "No se pudo leer Set-Cookie: ${e.message}")
            }

            val xmlString = response.body()?.string() ?: response.errorBody()?.string() ?: ""
            Log.d("SNRepository", "Respuesta XML recibida: $xmlString")
            
            if (xmlString.contains("true", ignoreCase = true) || xmlString.contains(">1<")) {
                userMatricula = matricula
                Log.d("SNRepository", " Autenticación exitosa")
                return true
            }
            
            Log.d("SNRepository", " Autenticación fallida")
            false
        } catch (e: Exception) {
            Log.e("SNRepository", " Error en autenticación: ${e.message}", e)
            false
        }
    }

    override suspend fun profile(matricula: String): ProfileStudent {
        Log.d("SNRepository", "===== INICIANDO OBTENCIÓN DE PERFIL =====")
        
        return try {
            val soapBody = bodyperfil
            Log.d("SNRepository", "Enviando petición SOAP de perfil...")
            
            val response = try {
                snApiService.perfil(sessionCookie, soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            } catch (e: HttpException) {
                Log.e("SNRepository", "Error HTTP ${e.code()}")
                return ProfileStudent(matricula = matricula, nombre = "Error")
            }

            val xmlString = response.body()?.string() ?: response.errorBody()?.string() ?: ""
            Log.d("SNRepository", "Respuesta recibida: ${xmlString.take(100)}...")
            
            // Extraer el resultado XML/JSON (soporta la variante WithLineamiento)
            var resultText: String? = null
            resultText = Regex("<getAlumnoAcademicoWithLineamientoResult>(.*?)</getAlumnoAcademicoWithLineamientoResult>", RegexOption.DOT_MATCHES_ALL)
                .find(xmlString)?.groupValues?.get(1)
            if (resultText.isNullOrEmpty()) {
                resultText = Regex("<getAlumnoAcademicoResult>(.*?)</getAlumnoAcademicoResult>", RegexOption.DOT_MATCHES_ALL)
                    .find(xmlString)?.groupValues?.get(1)
            }
            var result = resultText ?: ""
            
            if (result.isEmpty()) {
                Log.e("SNRepository", "No se encontró resultado en la respuesta")
                return ProfileStudent(matricula = matricula, nombre = "Perfil no disponible")
            }
            
            // Limpiar el contenido si está codificado
            var processed = result
            if (processed.contains("&lt;")) {
                processed = processed.replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&amp;", "&")
            }
            
            Log.d("SNRepository", "Contenido procesado: ${processed.take(100)}...")
            
            // Intentar parsear como JSON
            if (processed.trim().startsWith("{")) {
                try {
                    val json = Json.parseToJsonElement(processed.trim()).jsonObject
                    val nombre = json["nombre"]?.jsonPrimitive?.content ?: "No disponible"
                    val carrera = json["carrera"]?.jsonPrimitive?.content ?: "No disponible"
                    // semActual viene en la respuesta con clave "semActual"
                    val semActual = json["semActual"]?.jsonPrimitive?.content ?: json["semestre"]?.jsonPrimitive?.content ?: "0"
                    val promedio = json["promedio"]?.jsonPrimitive?.content ?: "0.0"
                    val especialidad = json["especialidad"]?.jsonPrimitive?.content ?: ""
                    val cdtsReunidos = json["cdtosAcumulados"]?.jsonPrimitive?.content ?: json["cdtosAcumulados"]?.toString() ?: ""
                    val cdtsActuales = json["cdtosActuales"]?.jsonPrimitive?.content ?: ""
                    val inscrito = json["inscrito"]?.jsonPrimitive?.content ?: ""
                    val estatusAcademico = json["estatus"]?.jsonPrimitive?.content ?: ""
                    val reinscripcionFecha = json["fechaReins"]?.jsonPrimitive?.content ?: ""
                    val sinAdeudos = json["adeudo"]?.jsonPrimitive?.content ?: ""

                    Log.d("SNRepository", "Perfil parseado: $nombre - $carrera")

                    return ProfileStudent(
                        matricula = json["matricula"]?.jsonPrimitive?.content ?: matricula,
                        nombre = nombre,
                        apellidos = "",
                        carrera = carrera,
                        semestre = semActual,
                        promedio = promedio,
                        estado = estatusAcademico,
                        statusMatricula = if (inscrito.equals("true", true)) "Activo" else "Inactivo",
                        especialidad = especialidad,
                        cdtsReunidos = cdtsReunidos,
                        cdtsActuales = cdtsActuales,
                        semActual = semActual,
                        inscrito = inscrito,
                        estatusAcademico = estatusAcademico,
                        estatusAlumno = "",
                        reinscripcionFecha = reinscripcionFecha,
                        sinAdeudos = sinAdeudos
                    )
                } catch (e: Exception) {
                    Log.e("SNRepository", "Error parseando JSON: ${e.message}")
                }
            }
            
            ProfileStudent(matricula = matricula, nombre = "Perfil obtenido")
            
        } catch (e: Exception) {
            Log.e("SNRepository", "Error obteniendo perfil: ${e.message}", e)
            ProfileStudent(matricula = matricula, nombre = "Error de conexión")
        }
    }

    override suspend fun getCargaAcademica(): String {
        Log.d("SNRepository", "===== INICIANDO OBTENCIÓN DE CARGA ACADÉMICA =====")

        return try {
            val soapBody = bodyCargaAcademica
            Log.d("SNRepository", "Enviando petición SOAP de carga académica...")

            val response = try {
                snApiService.cargaAcademica(sessionCookie, soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            } catch (e: HttpException) {
                Log.e("SNRepository", "Error HTTP ${e.code()}")
                return ""
            }

            val xmlString = response.body()?.string() ?: response.errorBody()?.string() ?: ""
            Log.d("SNRepository", "Respuesta recibida: ${xmlString.take(100)}...")

            // Extraer el resultado XML
            val resultText = Regex("<getCargaAcademicaByAlumnoResult>(.*?)</getCargaAcademicaByAlumnoResult>", RegexOption.DOT_MATCHES_ALL)
                .find(xmlString)?.groupValues?.get(1) ?: ""

            if (resultText.isEmpty()) {
                Log.e("SNRepository", "No se encontró resultado en la respuesta")
                return ""
            }

            // Limpiar el contenido si está codificado
            var processed = resultText
            if (processed.contains("&lt;")) {
                processed = processed.replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&amp;", "&")
            }

            Log.d("SNRepository", "Carga académica obtenida exitosamente")
            processed

        } catch (e: Exception) {
            Log.e("SNRepository", "Error obteniendo carga académica: ${e.message}", e)
            ""
        }
    }

    override suspend fun getKardex(): String {
        Log.d("SNRepository", "===== INICIANDO OBTENCIÓN DE KARDEX =====")

        return try {
            val soapBody = bodyKardex
            Log.d("SNRepository", "Enviando petición SOAP de kardex...")

            val response = try {
                snApiService.kardex(sessionCookie, soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            } catch (e: HttpException) {
                Log.e("SNRepository", "Error HTTP ${e.code()}")
                return ""
            }

            val xmlString = response.body()?.string() ?: response.errorBody()?.string() ?: ""
            Log.d("SNRepository", "Respuesta recibida: ${xmlString.take(100)}...")

            // Extraer el resultado XML
            val resultText = Regex("<getAllKardexConPromedioByAlumnoResult>(.*?)</getAllKardexConPromedioByAlumnoResult>", RegexOption.DOT_MATCHES_ALL)
                .find(xmlString)?.groupValues?.get(1) ?: ""

            if (resultText.isEmpty()) {
                Log.e("SNRepository", "No se encontró resultado en la respuesta")
                return ""
            }

            // Limpiar el contenido si está codificado
            var processed = resultText
            if (processed.contains("&lt;")) {
                processed = processed.replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&amp;", "&")
            }

            Log.d("SNRepository", "Kardex obtenido exitosamente")
            processed

        } catch (e: Exception) {
            Log.e("SNRepository", "Error obteniendo kardex: ${e.message}", e)
            ""
        }
    }

    override suspend fun getCalifUnidades(): String {
        Log.d("SNRepository", "===== INICIANDO OBTENCIÓN DE CALIFICACIONES POR UNIDADES =====")

        return try {
            val soapBody = bodyCalifUnidades
            Log.d("SNRepository", "Enviando petición SOAP de calificaciones por unidades...")

            val response = try {
                snApiService.califUnidades(sessionCookie, soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            } catch (e: HttpException) {
                Log.e("SNRepository", "Error HTTP ${e.code()}")
                return ""
            }

            val xmlString = response.body()?.string() ?: response.errorBody()?.string() ?: ""
            Log.d("SNRepository", "Respuesta recibida: ${xmlString.take(100)}...")

            // Extraer el resultado XML
            val resultText = Regex("<getCalifUnidadesByAlumnoResult>(.*?)</getCalifUnidadesByAlumnoResult>", RegexOption.DOT_MATCHES_ALL)
                .find(xmlString)?.groupValues?.get(1) ?: ""

            if (resultText.isEmpty()) {
                Log.e("SNRepository", "No se encontró resultado en la respuesta")
                return ""
            }

            // Limpiar el contenido si está codificado
            var processed = resultText
            if (processed.contains("&lt;")) {
                processed = processed.replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&amp;", "&")
            }

            Log.d("SNRepository", "Calificaciones por unidades obtenidas exitosamente")
            processed

        } catch (e: Exception) {
            Log.e("SNRepository", "Error obteniendo calificaciones por unidades: ${e.message}", e)
            ""
        }
    }

    override suspend fun getCalifFinal(): String {
        Log.d("SNRepository", "===== INICIANDO OBTENCIÓN DE CALIFICACIONES FINALES =====")

        return try {
            val soapBody = bodyCalifFinal
            Log.d("SNRepository", "Enviando petición SOAP de calificaciones finales...")

            val response = try {
                snApiService.califFinal(sessionCookie, soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            } catch (e: HttpException) {
                Log.e("SNRepository", "Error HTTP ${e.code()}")
                return ""
            }

            val xmlString = response.body()?.string() ?: response.errorBody()?.string() ?: ""
            Log.d("SNRepository", "Respuesta recibida: ${xmlString.take(100)}...")

            // Extraer el resultado XML
            val resultText = Regex("<getAllCalifFinalByAlumnosResult>(.*?)</getAllCalifFinalByAlumnosResult>", RegexOption.DOT_MATCHES_ALL)
                .find(xmlString)?.groupValues?.get(1) ?: ""

            if (resultText.isEmpty()) {
                Log.e("SNRepository", "No se encontró resultado en la respuesta")
                return ""
            }

            // Limpiar el contenido si está codificado
            var processed = resultText
            if (processed.contains("&lt;")) {
                processed = processed.replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&amp;", "&")
            }

            Log.d("SNRepository", "Calificaciones finales obtenidas exitosamente")
            processed

        } catch (e: Exception) {
            Log.e("SNRepository", "Error obteniendo calificaciones finales: ${e.message}", e)
            ""
        }
    }

    override suspend fun getMatricula(): String {
        return userMatricula
    }

    override fun logout() {
        userMatricula = ""
        sessionCookie = null
        Log.d("SNRepository", "Sesión cerrada: Datos limpiados")
    }
}
