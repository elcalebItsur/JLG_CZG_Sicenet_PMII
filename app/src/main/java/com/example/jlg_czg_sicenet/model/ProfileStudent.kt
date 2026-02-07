package com.example.jlg_czg_sicenet.model

import kotlinx.serialization.Serializable
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Serializable
data class ProfileStudent(
    val matricula: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val carrera: String = "",
    val semestre: String = "",
    val promedio: String = "",
    val estado: String = "",
    val statusMatricula: String = "",
    val especialidad: String = "",
    val cdtsReunidos: String = "",
    val cdtsActuales: String = "",
    val semActual: String = "",
    val inscrito: String = "",
    val estatusAcademico: String = "",
    val estatusAlumno: String = "",
    val reinscripcionFecha: String = "",
    val sinAdeudos: String = ""
)

@Serializable
data class Usuario(
    val matricula: String = ""
)

@Serializable
@Root(name = "MateriaKardex", strict = false)
data class MateriaKardex(
    @field:Element(name = "Materia", required = false)
    @param:Element(name = "Materia", required = false)
    val materia: String? = null,
    
    @field:Element(name = "Calificacion", required = false)
    @param:Element(name = "Calificacion", required = false)
    val calificacion: String? = null
)

@Serializable
@Root(name = "MateriaCarga", strict = false)
data class MateriaCarga(
    @field:Element(name = "Materia", required = false)
    @param:Element(name = "Materia", required = false)
    val materia: String? = null
)

@Serializable
@Root(name = "MateriaParcial", strict = false)
data class MateriaParcial(
    val materia: String = "",
    val parciales: List<String> = emptyList()
)
