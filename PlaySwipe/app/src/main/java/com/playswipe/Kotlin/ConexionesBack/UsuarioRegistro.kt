package com.playswipe.Kotlin.ConexionesBack

data class UsuarioRegistro(
    val email: String,
    val password: String,
    val nombreUsuario: String,
    val nombreApellidos: String,
    val urlImagenPerfil: String?
)
