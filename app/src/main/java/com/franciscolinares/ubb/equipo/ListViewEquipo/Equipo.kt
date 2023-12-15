package com.franciscolinares.ubb.equipo.ListViewEquipo

class Equipo(
    val id: String,
    val nombreEquipo: String,
    val categoria: String,
    val sexo: String,
    val localidad: String,
    val foto: String,
    val plantilla: HashMap<String, String>
)