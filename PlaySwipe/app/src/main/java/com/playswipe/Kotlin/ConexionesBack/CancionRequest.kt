package com.playswipe.Kotlin.ConexionesBack

data class CancionRequest(
    val id: Long,
    val titulo: String,
    val autor: String,
    val genero: String?,
    val archivo_mp3: String,
    val urlImagen: String?
)
