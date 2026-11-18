// src/main/java/com/playswipe/Kotlin/ConexionesBack/AmigosRequest.kt
package com.playswipe.Kotlin.ConexionesBack

// Respuesta genérica

// Body para enviar solicitud
data class SendFriendRequest(val toUserId: String)

// Body para responder solicitud
data class RespondFriendRequest(val accept: Boolean)

// DTO para solicitudes pendientes
data class AmigoDTO(
    val idSolicitud: Long,
    val fromUserId: String,
    val fromUserName: String,
    val nombreUsuario: String?,
    val urlImagenPerfil: String?

)

// DTO para lista de amigos
data class UsuarioDTO(
    val idUsuario: String,
    val nombreUsuario: String,
    val urlImagenPerfil: String?
)
