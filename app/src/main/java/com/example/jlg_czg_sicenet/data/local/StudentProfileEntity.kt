package com.example.jlg_czg_sicenet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.jlg_czg_sicenet.model.ProfileStudent

@Entity(tableName = "student_profile")
data class StudentProfileEntity(
    @PrimaryKey val matricula: String,
    val nombre: String,
    val apellidos: String,
    val carrera: String,
    val semestre: String,
    val promedio: String,
    val estado: String,
    val statusMatricula: String,
    val especialidad: String,
    val cdtsReunidos: String,
    val cdtsActuales: String,
    val semActual: String,
    val inscrito: String,
    val estatusAcademico: String,
    val estatusAlumno: String,
    val reinscripcionFecha: String,
    val sinAdeudos: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

fun StudentProfileEntity.toModel(): ProfileStudent { // Room -> Model UI
    return ProfileStudent(
        matricula = matricula,
        nombre = nombre,
        apellidos = apellidos,
        carrera = carrera,
        semestre = semestre,
        promedio = promedio,
        estado = estado,
        statusMatricula = statusMatricula,
        especialidad = especialidad,
        cdtsReunidos = cdtsReunidos,
        cdtsActuales = cdtsActuales,
        semActual = semActual,
        inscrito = inscrito,
        estatusAcademico = estatusAcademico,
        estatusAlumno = estatusAlumno,
        reinscripcionFecha = reinscripcionFecha,
        sinAdeudos = sinAdeudos
    )
}

fun ProfileStudent.toEntity(lastUpdated: Long = System.currentTimeMillis()): StudentProfileEntity { // Model UI -> Room
    return StudentProfileEntity(
        matricula = matricula,
        nombre = nombre,
        apellidos = apellidos,
        carrera = carrera,
        semestre = semestre,
        promedio = promedio,
        estado = estado,
        statusMatricula = statusMatricula,
        especialidad = especialidad,
        cdtsReunidos = cdtsReunidos,
        cdtsActuales = cdtsActuales,
        semActual = semActual,
        inscrito = inscrito,
        estatusAcademico = estatusAcademico,
        estatusAlumno = estatusAlumno,
        reinscripcionFecha = reinscripcionFecha,
        sinAdeudos = sinAdeudos,
        lastUpdated = lastUpdated
    )
}

//Esto separa la capa de datos local de los modelos del negocio