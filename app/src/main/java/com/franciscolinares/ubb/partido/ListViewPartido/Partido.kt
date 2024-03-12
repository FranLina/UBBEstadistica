package com.franciscolinares.ubb.partido.ListViewPartido

class Partido(
    val id: String,
    val local: String,
    val visitante: String,
    val polideportivo: String,
    val resultado: String,
    val hora: String,
    val fecha: String,
    val estado: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Partido

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}