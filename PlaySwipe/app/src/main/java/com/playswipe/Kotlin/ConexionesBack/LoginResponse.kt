package com.playswipe.Kotlin.ConexionesBack

data class LoginResponse(
    val nombre: String,
    val id_usuario: String,
    val mensaje: String,
    val urlImagenPerfil: String?
)