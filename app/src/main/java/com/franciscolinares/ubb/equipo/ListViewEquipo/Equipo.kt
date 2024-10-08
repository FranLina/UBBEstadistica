package com.franciscolinares.ubb.equipo.ListViewEquipo

class Equipo(
    val id: String,
    val nombreEquipo: String,
    val categoria: String,
    val sexo: String,
    val localidad: String,
    val foto: String,
    val plantilla: HashMap<String, String>
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Equipo

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}