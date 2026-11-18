package com.playswipe.Kotlin.Playlist


import kotlinx.coroutines.withContext
import androidx.compose.ui.res.painterResource
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.storage.FirebaseStorage
import com.playswipe.Kotlin.ConexionesBack.RetrofitClient
import com.playswipe.Kotlin.Sesion
import com.playswipe.R
import com.playswipe.utils.Refuerzo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.content.edit

// Constantes de UI
private val AvatarSize = 100.dp
private val SongImageSize = 50.dp
private val SongTitleSize = 16.sp
private val SongAuthorSize = 12.sp

// Modelo de datos
data class Cancion(
    val id: Long,
    val titulo: String,
    val autor: String,
    val archivo_mp3: String,
    val urlImagen: String
)

fun subirImagenPlaylistAFirebaseYGuardarURL(
    context: Context,
    uri: Uri,
    onUrlSaved: (String) -> Unit = {}
) {
    val storageRef = FirebaseStorage.getInstance().reference
    val userId = Sesion.userId ?: return
    val imgRef = storageRef.child("imagenes_playlist/$userId.jpg")

    imgRef.putFile(uri)
        .addOnSuccessListener {
            imgRef.downloadUrl.addOnSuccessListener { url ->
                val urlStr = url.toString()
                // Guarda localmente
                context.getSharedPreferences("datos_usuario", Context.MODE_PRIVATE)
                    .edit() { putString("urlImagenPlaylist", urlStr) }

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        RetrofitClient.apiService.actualizarImagenPlaylist(
                            userId,
                            mapOf("imagenPlaylist" to urlStr)
                        )
                    } catch (e: Exception) {
                        Log.e("Playlist", "Error backend imagen", e)
                    }
                }
                // Actualiza UI
                onUrlSaved(urlStr)
                Toast.makeText(context, "Imagen de playlist actualizada", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Error subiendo imagen", Toast.LENGTH_SHORT).show()
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayListScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val media = remember { MediaPlayer() }

    // Cuando la pantalla se quite, paramos la reproducción
    DisposableEffect(Unit) {
        onDispose {
            if (media.isPlaying) {
                media.stop()
            }
            media.release()
        }
    }

    // Nombre editable
    var playlistName by remember { mutableStateOf(TextFieldValue("Mi playlist")) }
    var editingName  by remember { mutableStateOf(false) }

    // Avatar persistente (ahora se carga desde backend)
    var avatarUrl by remember { mutableStateOf<String?>(null) }

    // Selector de imagen
    val pick = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { subirImagenPlaylistAFirebaseYGuardarURL(context, it) { avatarUrl = it } }
    }

    // Lista de canciones y estado de reproducción
    val canciones = remember { mutableStateListOf<Cancion>() }
    var cancionIndex by remember { mutableStateOf(-1) } // Ninguna resaltada al inicio
    val cancionActual by remember { derivedStateOf { canciones.getOrNull(cancionIndex)?.archivo_mp3 } }
    var posActual by remember { mutableStateOf(0) }
    val colorDom by remember { mutableStateOf(Color.Black) }
    var primeraCarga by remember { mutableStateOf(true) }

    // Carga única de nombre, imagen y canciones de la playlist
    LaunchedEffect(Unit) {
        Sesion.userId?.let { uid ->
            try {
                // Obtener nombre
                val infoResponse = RetrofitClient.apiService.getPlaylistInfo(uid)
                if (infoResponse.isSuccessful) {
                    playlistName = TextFieldValue(infoResponse.body()?.nombre ?: "Mi playlist")
                }

                // Obtener imagen
                val imgResponse = RetrofitClient.apiService.obtenerImagenPlaylist(uid)
                if (imgResponse.isSuccessful) {
                    avatarUrl = imgResponse.body()?.get("urlImagen") ?: ""
                }

                // Obtener canciones
                val list = RetrofitClient.apiService.getPlaylist(uid)
                canciones.clear()
                canciones += list.map {
                    Cancion(it.id, it.titulo, it.autor, it.archivo_mp3, it.urlImagen.orEmpty())
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Error cargando datos de la playlist", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Cada vez que cambie cancionActual, recarga y reproduce (controlando reproducción automática)
    LaunchedEffect(cancionActual) {
        cancionActual?.let { ruta ->
            media.reset()
            media.setDataSource(ruta)
            media.prepare()
            if (!primeraCarga) {
                media.start()
            } else {
                primeraCarga = false
            }
            media.setOnCompletionListener {
                // Avanza automáticamente al terminar
                cancionIndex = (cancionIndex + 1).coerceAtMost(canciones.lastIndex)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        bottomBar = {
            Column {
                canciones.getOrNull(cancionIndex)?.let { song ->
                    MiniPlayer(
                        cancion = song,
                        canciones = canciones,
                        fondo = colorDom,
                        colorTexto = if (colorDom.luminance() > .5f) Color.Black else Color.White,
                        colorSub = if (colorDom.luminance() > .5f) Color.DarkGray else Color.LightGray,
                        mediaPlayer = media,
                        posActual = posActual,
                        onPosicionStore = { posActual = it },
                        onPrev = { cancionIndex = (cancionIndex - 1).coerceAtLeast(0) },
                        onNext = { cancionIndex = (cancionIndex + 1).coerceAtMost(canciones.lastIndex) },
                        cancionIndex = cancionIndex,
                        onActualizarIndex = { cancionIndex = it }
                    )
                }
                Refuerzo.BarraNavegacionInferior(navController)
            }
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
        ) {
            // Cabecera con avatar y nombre
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(AvatarSize)
                            .clip(CircleShape)
                            .background(Color.DarkGray)
                            .clickable { pick.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUrl.isNullOrBlank()) {
                            Image(
                                painter = painterResource(R.drawable.user_perfil),
                                contentDescription = "Avatar por defecto",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Avatar Playlist",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    // Botón para eliminar la imagen de la playlist y restaurar la predeterminada
                    val context = LocalContext.current
                    TextButton(onClick = {
                        Sesion.userId?.let { uid ->
                            val storageRef = FirebaseStorage.getInstance().reference
                            val imgRef = storageRef.child("imagenes_playlist/$uid.jpg")
                            imgRef.delete().addOnSuccessListener {
                                // Borrar URL de imagen de playlist en backend y local
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val response = RetrofitClient.apiService.actualizarImagenPlaylist(
                                            uid,
                                            mapOf("imagenPlaylist" to "")
                                        )
                                        withContext(Dispatchers.Main) {
                                            if (response.isSuccessful) {
                                                context.getSharedPreferences("datos_usuario", Context.MODE_PRIVATE)
                                                    .edit { remove("urlImagenPlaylist") }
                                                avatarUrl = null
                                                Toast.makeText(context, "Imagen eliminada", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Error en el backend", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }.addOnFailureListener {
                                Toast.makeText(context, "No hay imagen personalizada", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Text("Eliminar imagen de playlist", color = Color.Red)
                    }
                }
                Spacer(Modifier.width(12.dp))
                TextField(
                    value = playlistName,
                    onValueChange = { if (editingName && it.text.length <= 10) playlistName = it },
                    modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White,
                        fontSize = 20.sp),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    enabled = editingName,
                    trailingIcon = {
                        IconButton(onClick = {
                            editingName = !editingName
                            if (!editingName) {
                                Sesion.userId?.let { uid ->
                                    scope.launch {
                                        RetrofitClient.apiService
                                            .renombrarPlaylist(uid, mapOf("nombre" to playlistName.text))
                                    }
                                }
                            }
                        }) {
                            Icon(
                                imageVector = if (editingName) Icons.Default.Check
                                else Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                )
            }

            // Lista de canciones
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(20.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(canciones) { index, c ->
                    SongCard(c, index == cancionIndex) {
                        cancionIndex = index
                        if (media.isPlaying) media.stop()
                        media.reset()
                        media.setDataSource(canciones[index].archivo_mp3)
                        media.prepare()
                        media.start()
                        primeraCarga = false
                        posActual = 0
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MiniPlayer(
    cancion: Cancion,
    canciones: SnapshotStateList<Cancion>,
    fondo: Color,
    colorTexto: Color,
    colorSub: Color,
    mediaPlayer: MediaPlayer,
    posActual: Int,
    onPosicionStore: (Int) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    cancionIndex: Int,
    onActualizarIndex: (Int) -> Unit
) {
    var progreso by remember { mutableStateOf(0f) }
    var isPlaying by remember { mutableStateOf(mediaPlayer.isPlaying) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            progreso = if (mediaPlayer.duration > 0)
                (mediaPlayer.currentPosition / mediaPlayer.duration.toFloat()).coerceIn(0f, 1f)
            else 0f
            delay(300)
            isPlaying = mediaPlayer.isPlaying
        }
    }

    Surface(tonalElevation = 8.dp) {
        AnimatedContent(
            targetState = cancion,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
            }
        ) { currentCancion ->
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(fondo)
                        .pointerInput(currentCancion.id) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                if (dragAmount > 100) onPrev()
                                else if (dragAmount < -100) onNext()
                            }
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        Sesion.userId?.let { uid ->
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    RetrofitClient.apiService.EliminarCancionPlaylist(uid, currentCancion.id)
                                } catch (e: Exception) {
                                    Log.e("MiniPlayer", "Error eliminando del backend", e)
                                }
                            }
                        }

                        val index = canciones.indexOfFirst { it.id == currentCancion.id }
                        if (index != -1) {
                            canciones.removeAt(index)
                            if (index < cancionIndex) {
                                onActualizarIndex(cancionIndex - 1)
                            } else if (index == cancionIndex) {
                                if (index >= canciones.size) {
                                    onActualizarIndex((canciones.size - 1).coerceAtLeast(0))
                                }
                                mediaPlayer.stop()
                                mediaPlayer.reset()
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar canción",
                            tint = colorTexto
                        )
                    }
                    IconButton(
                        onClick = {
                            val nuevoIndex = (cancionIndex - 1).coerceAtLeast(0)
                            onActualizarIndex(nuevoIndex)
                            canciones.getOrNull(nuevoIndex)?.let { nueva ->
                                mediaPlayer.reset()
                                mediaPlayer.setDataSource(nueva.archivo_mp3)
                                mediaPlayer.prepare()
                                mediaPlayer.start()
                                isPlaying = true
                                progreso = 0f
                                onPosicionStore(0)
                            }
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.NavigateBefore,
                            contentDescription = "Anterior",
                            tint = colorTexto
                        )
                    }
                    AsyncImage(
                        model = currentCancion.urlImagen,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(SongImageSize)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(currentCancion.titulo,
                            color = colorTexto,
                            fontSize = SongTitleSize)

                        Text(currentCancion.autor,  color = colorSub,
                            fontSize = SongAuthorSize)
                    }
                    IconButton(onClick = {
                        if (mediaPlayer.isPlaying) {
                            onPosicionStore(mediaPlayer.currentPosition)
                            mediaPlayer.pause()
                        } else {
                            mediaPlayer.seekTo(posActual)
                            mediaPlayer.start()
                            isPlaying = true
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.playswipe_logo_sinfondo_sinletras_),
                            contentDescription = "Play/Pause",
                            tint = colorTexto
                        )
                    }
                    IconButton(
                        onClick = {
                            val nuevoIndex = (cancionIndex + 1).coerceAtMost(canciones.lastIndex)
                            onActualizarIndex(nuevoIndex)
                            canciones.getOrNull(nuevoIndex)?.let { nueva ->
                                mediaPlayer.reset()
                                mediaPlayer.setDataSource(nueva.archivo_mp3)
                                mediaPlayer.prepare()
                                mediaPlayer.start()
                                isPlaying = true
                                progreso = 0f
                                onPosicionStore(0)
                            }
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.NavigateNext,
                            contentDescription = "Siguiente",
                            tint = colorTexto
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = Color(0xFFF81D3D)
                )

            }
        }
    }
}

@Composable
fun SongCard(cancion: Cancion, isPlaying: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            Modifier
                .background(Color.Black)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = cancion.urlImagen,
                contentDescription= null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(SongImageSize)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    cancion.titulo,
                    color = if (isPlaying) Color(0xFFF41E40)
                    else Color.White,
                    fontSize = SongTitleSize
                )
                Text(
                    cancion.autor,
                    color = if (isPlaying) Color(0xFFF41E40)
                    else Color.LightGray,
                    fontSize = SongAuthorSize
                )
            }
            IconButton(onClick = onClick) {
                Icon(
                    painter = painterResource(R.drawable.playswipe_logo_sinfondo_sinletras_),
                    contentDescription = if (isPlaying) "Pausar" else "Play",
                    tint = Color(0xFFF41E40)
                )
            }
        }
    }
}