package com.example.jlg_czg_sicenet.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Header
import retrofit2.http.Url
import retrofit2.http.Streaming

val bodyacceso =
    """
        <?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Body>
            <accesoLogin xmlns="http://tempuri.org/">
              <strMatricula>%s</strMatricula>
              <strContrasenia>%s</strContrasenia>   
              <tipoUsuario>ALUMNO</tipoUsuario>
            </accesoLogin>
          </soap:Body>
        </soap:Envelope>
    """.trimIndent()

val bodyperfil =
    """<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <getAlumnoAcademicoWithLineamiento xmlns="http://tempuri.org/" />
  </soap:Body>
</soap:Envelope>""".trimIndent()

val bodyCargaAcademica =
    """<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <getCargaAcademicaByAlumno xmlns="http://tempuri.org/" />
  </soap:Body>
</soap:Envelope>""".trimIndent()

val bodyKardex =
    """<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <getAllKardexConPromedioByAlumno xmlns="http://tempuri.org/">
      <aluLineamiento>1</aluLineamiento>
    </getAllKardexConPromedioByAlumno>
  </soap:Body>
</soap:Envelope>""".trimIndent()

val bodyCalifUnidades =
    """<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <getCalifUnidadesByAlumno xmlns="http://tempuri.org/" />
  </soap:Body>
</soap:Envelope>""".trimIndent()

val bodyCalifFinal =
    """<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <getAllCalifFinalByAlumnos xmlns="http://tempuri.org/">
      <bytModEducativo>1</bytModEducativo>
    </getAllCalifFinalByAlumnos>
  </soap:Body>
</soap:Envelope>""".trimIndent()

interface SICENETWService {

    @Headers(
        "Content-Type: text/xml; charset=utf-8",
      "SOAPAction: \"http://tempuri.org/accesoLogin\""
    )
    @POST("/ws/wsalumnos.asmx")
    suspend fun acceso(@Body soap: RequestBody): Response<ResponseBody>

    @Headers(
        "Content-Type: text/xml; charset=utf-8",
      "SOAPAction: \"http://tempuri.org/getAlumnoAcademicoWithLineamiento\""
    )
    @POST("/ws/wsalumnos.asmx")
    suspend fun perfil(@Header("Cookie") cookie: String?, @Body soap: RequestBody): Response<ResponseBody>

    @Headers(
        "Content-Type: text/xml; charset=utf-8",
        "SOAPAction: \"http://tempuri.org/getCargaAcademicaByAlumno\""
    )
    @POST("/ws/wsalumnos.asmx")
    suspend fun cargaAcademica(@Header("Cookie") cookie: String?, @Body soap: RequestBody): Response<ResponseBody>

    @Headers(
        "Content-Type: text/xml; charset=utf-8",
        "SOAPAction: \"http://tempuri.org/getAllKardexConPromedioByAlumno\""
    )
    @POST("/ws/wsalumnos.asmx")
    suspend fun kardex(@Header("Cookie") cookie: String?, @Body soap: RequestBody): Response<ResponseBody>

    @Headers(
        "Content-Type: text/xml; charset=utf-8",
        "SOAPAction: \"http://tempuri.org/getCalifUnidadesByAlumno\""
    )
    @POST("/ws/wsalumnos.asmx")
    suspend fun califUnidades(@Header("Cookie") cookie: String?, @Body soap: RequestBody): Response<ResponseBody>

    @Headers(
        "Content-Type: text/xml; charset=utf-8",
        "SOAPAction: \"http://tempuri.org/getAllCalifFinalByAlumnos\""
    )
    @POST("/ws/wsalumnos.asmx")
    suspend fun califFinal(@Header("Cookie") cookie: String?, @Body soap: RequestBody): Response<ResponseBody>

    @GET("/frmPlataformaAlumno.aspx")
    @Streaming
    suspend fun plataforma(): ResponseBody
}  
