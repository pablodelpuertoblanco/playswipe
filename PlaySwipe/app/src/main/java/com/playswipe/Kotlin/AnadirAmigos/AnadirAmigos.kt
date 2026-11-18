package com.playswipe.Amigos

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.playswipe.Kotlin.ConexionesBack.*
import com.playswipe.utils.Refuerzo
import kotlinx.coroutines.launch

@Composable
fun AnadirAmigosScreen(
    navController: NavController,
    api: ApiService,
    currentUserId: String
) {
    val scope = rememberCoroutineScope()
    val snackHost = remember { SnackbarHostState() }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    var idAmigo by remember { mutableStateOf("") }
    var solicitudes by remember { mutableStateOf<List<AmigoDTO>>(emptyList()) }
    var amigos by remember { mutableStateOf<List<UsuarioDTO>>(emptyList()) }

    LaunchedEffect(currentUserId) {
        try {
            solicitudes = api.getFriendRequests(currentUserId)
        } catch (e: Exception) {
            errorMsg = "No pude cargar solicitudes: ${e.localizedMessage}"
        }
        try {
            amigos = api.getFriends(currentUserId)
        } catch (e: Exception) {
            errorMsg = "No pude cargar amigos: ${e.localizedMessage}"
        }
    }

    Scaffold(
        containerColor = Color.Black,
        bottomBar = { Refuerzo.BarraNavegacionInferior(navController) },
        snackbarHost = { SnackbarHost(snackHost) }
    ) { innerPadding ->
        errorMsg?.let { msg ->
            LaunchedEffect(msg) {
                snackHost.showSnackbar(msg)
                errorMsg = null
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Añade un Amigo",
                    color = Color.White,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(top = 40.dp)
                )

                Spacer(Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Escribe el ID de tu amigo",
                        color = Color.White,
                        fontSize = 18.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = idAmigo,
                            onValueChange = { if (it.length <= 20) idAmigo = it },
                            placeholder = { Text("ID del amigo", color = Color.Gray) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = {
                            scope.launch {
                                try {
                                    val trimmedId = idAmigo.trim()
                                    if (trimmedId.isBlank()) {
                                        errorMsg = "Por favor, rellena el campo con un ID válido."
                                        return@launch
                                    }
                                    if (trimmedId == currentUserId) {
                                        errorMsg = "No puedes enviarte una solicitud a ti mismo."
                                    } else if (amigos.any { it.idUsuario == trimmedId }) {
                                        errorMsg = "Ya sois amigos."
                                    } else if (solicitudes.any { it.fromUserId == trimmedId }) {
                                        errorMsg = "Ya tienes una solicitud pendiente."
                                    } else {
                                        api.sendFriendRequest(
                                            currentUserId,
                                            SendFriendRequest(toUserId = trimmedId)
                                        )
                                        solicitudes = api.getFriendRequests(currentUserId)
                                        amigos = api.getFriends(currentUserId)
                                        idAmigo = ""
                                        errorMsg = "Solicitud enviada con éxito."
                                    }
                                } catch (e: Exception) {
                                    errorMsg = "Error enviando solicitud: ${e.localizedMessage}"
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Añadir amigo",
                                tint = Color.White
                            )
                        }
                    }
                }

                Text(
                    text = "Solicitudes de amistad",
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 20.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp)
                ) {
                    if (solicitudes.isEmpty()) {
                        Text(
                            text = "No tienes solicitudes de amistad",
                            color = Color.Gray,
                            fontSize = 18.sp
                        )
                    } else {
                        solicitudes.forEach { req ->
                            val safeFromUserName = req.nombreUsuario?.takeIf { it.isNotBlank() } ?: "Usuario"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = (req.urlImagenPerfil ?: "").ifBlank {
                                        "https://ui-avatars.com/api/?name=${Uri.encode(safeFromUserName)}"
                                    },
                                    contentDescription = "Imagen perfil",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Text(
                                    text = safeFromUserName,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                                Row {
                                    IconButton(onClick = {
                                        scope.launch {
                                            try {
                                                api.respondFriendRequest(
                                                    currentUserId,
                                                    req.idSolicitud,
                                                    RespondFriendRequest(accept = true)
                                                )
                                                solicitudes = api.getFriendRequests(currentUserId)
                                                amigos = api.getFriends(currentUserId)
                                            } catch (e: Exception) {
                                                errorMsg = "Error aceptando: ${e.localizedMessage}"
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Default.Check, contentDescription = "Aceptar", tint = Color.White)
                                    }
                                    IconButton(onClick = {
                                        scope.launch {
                                            try {
                                                api.respondFriendRequest(
                                                    currentUserId,
                                                    req.idSolicitud,
                                                    RespondFriendRequest(accept = false)
                                                )
                                                solicitudes = api.getFriendRequests(currentUserId)
                                                amigos = api.getFriends(currentUserId)
                                            } catch (e: Exception) {
                                                errorMsg = "Error rechazando: ${e.localizedMessage}"
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Rechazar", tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "Amigos",
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 20.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .heightIn(max = 300.dp)
                        .padding(16.dp)
                ) {
                    if (amigos.isEmpty()) {
                        item {
                            Text(
                                text = "No tienes amigos aún",
                                color = Color.Gray,
                                fontSize = 18.sp
                            )
                        }
                    } else {
                        items(amigos) { friend ->
                            // Asegura que el nombre de usuario nunca sea null ni inválido
                            val safeNombreUsuario = friend.nombreUsuario?.takeIf { it.isNotBlank() } ?: "Usuario"
                            val displayName = safeNombreUsuario
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try {
                                            Log.d(
                                                "Amigos",
                                                "ID: ${friend.idUsuario}, Nombre: $displayName, Imagen: ${friend.urlImagenPerfil}"
                                            )
                                            navController.navigate(
                                                "perfilAmigo/${
                                                    Uri.encode((friend.idUsuario ?: "").ifBlank { "desconocido" })
                                                }/${
                                                    Uri.encode(displayName)
                                                }/${
                                                    Uri.encode(
                                                        (friend.urlImagenPerfil ?: "").ifBlank {
                                                            "https://ui-avatars.com/api/?name=${Uri.encode(displayName)}"
                                                        }
                                                    )
                                                }"
                                            )
                                        } catch (e: Exception) {
                                            Log.e("AmigosError", "Error navegando: ${e.localizedMessage}")
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = (friend.urlImagenPerfil ?: "").ifBlank {
                                        "https://ui-avatars.com/api/?name=${Uri.encode(displayName)}"
                                    },
                                    contentDescription = "Imagen amigo",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = displayName,
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
