package com.example.jlg_czg_sicenet.model

import kotlinx.serialization.Serializable
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.NamespaceList
import org.simpleframework.xml.Root

// ============ ACCESO LOGIN RESPONSE ============

@Serializable
@Root(name = "Envelope", strict = false)
@Namespace(prefix = "soap", reference = "http://schemas.xmlsoap.org/soap/envelope/")
data class EnvelopeSobreAcceso(
    @field:Element(name = "Body", required = false)
    @param:Element(name = "Body", required = false)
    val body: BodyAccesoResponse? = null
)

@Serializable
@Root(name = "Body", strict = false)
data class BodyAccesoResponse(
    @field:Element(name = "accesoLoginResponse", required = false)
    @param:Element(name = "accesoLoginResponse", required = false)
    @Namespace(reference = "http://tempuri.org/")
    val accesoLoginResponse: AccesoLoginResponse? = null
)

@Serializable
@Root(name = "accesoLoginResponse", strict = false)
data class AccesoLoginResponse(
    @field:Element(name = "accesoLoginResult", required = false)
    @param:Element(name = "accesoLoginResult", required = false)
    @Namespace(reference = "http://tempuri.org/")
    val accesoLoginResult: String? = null
)

// ============ GET ALUMNO ACADEMICO RESPONSE ============

@Serializable
@Root(name = "Envelope", strict = false)
@Namespace(prefix = "soap", reference = "http://schemas.xmlsoap.org/soap/envelope/")
data class EnvelopeSobreAlumno(
    @field:Element(name = "Body", required = false)
    @param:Element(name = "Body", required = false)
    val body: BodyAlumnoResponse? = null
)

@Serializable
@Root(name = "Body", strict = false)
data class BodyAlumnoResponse(
    @field:Element(name = "getAlumnoAcademicoWithLineamientoResponse", required = false)
    @param:Element(name = "getAlumnoAcademicoWithLineamientoResponse", required = false)
    val getAlumnoAcademicoWithLineamientoResponse: AlumnoAcademicoWithLineamientoResponse? = null,

    @field:Element(name = "getAlumnoAcademicoResponse", required = false)
    @param:Element(name = "getAlumnoAcademicoResponse", required = false)
    val getAlumnoAcademicoResponse: AlumnoAcademicoResponse? = null
)

@Serializable
@Root(name = "getAlumnoAcademicoWithLineamientoResponse", strict = false)
data class AlumnoAcademicoWithLineamientoResponse(
    @field:Element(name = "getAlumnoAcademicoWithLineamientoResult", required = false)
    @param:Element(name = "getAlumnoAcademicoWithLineamientoResult", required = false)
    val getAlumnoAcademicoWithLineamientoResult: String? = null
)

@Serializable
@Root(name = "getAlumnoAcademicoResponse", strict = false)
data class AlumnoAcademicoResponse(
    @field:Element(name = "getAlumnoAcademicoResult", required = false)
    @param:Element(name = "getAlumnoAcademicoResult", required = false)
    val getAlumnoAcademicoResult: String? = null
)

// Clases auxiliares para parsear el DataSet XML del perfil
@Serializable
@Root(name = "DataSet", strict = false)
data class PerfilDataSet(
    @field:Element(name = "Alumno", required = false)
    @param:Element(name = "Alumno", required = false)
    val alumno: AlumnoInfo? = null
)

@Serializable
@Root(name = "Alumno", strict = false)
data class AlumnoInfo(
    @field:Element(name = "Matricula", required = false)
    @param:Element(name = "Matricula", required = false)
    val matricula: String? = null,
    
    @field:Element(name = "Nombre", required = false)
    @param:Element(name = "Nombre", required = false)
    val nombre: String? = null,
    
    @field:Element(name = "Apellidos", required = false)
    @param:Element(name = "Apellidos", required = false)
    val apellidos: String? = null,
    
    @field:Element(name = "Carrera", required = false)
    @param:Element(name = "Carrera", required = false)
    val carrera: String? = null,
    
    @field:Element(name = "Semestre", required = false)
    @param:Element(name = "Semestre", required = false)
    val semestre: String? = null,
    
    @field:Element(name = "Promedio", required = false)
    @param:Element(name = "Promedio", required = false)
    val promedio: String? = null,
    
    @field:Element(name = "Estado", required = false)
    @param:Element(name = "Estado", required = false)
    val estado: String? = null,
    
    @field:Element(name = "StatusMatricula", required = false)
    @param:Element(name = "StatusMatricula", required = false)
    val statusMatricula: String? = null
)
