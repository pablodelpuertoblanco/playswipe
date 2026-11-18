package com.playswipe.Kotlin.Perfil_Amigos

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.playswipe.Kotlin.ConexionesBack.CancionRequest
import com.playswipe.Kotlin.ConexionesBack.RetrofitClient
import com.playswipe.R
import com.playswipe.utils.Refuerzo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.palette.graphics.Palette

@Composable
fun PerfilAmigosScreen(
    navController: NavController,
    idAmigo: String,
    nombreAmigo: String,
    urlImagen: String?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(Unit) {
        onDispose {
            if (mediaPlayer.isPlaying) mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    // Estados de UI
    var cancionesMeGusta by remember { mutableStateOf("0") }
    val canciones = remember { mutableStateListOf<CancionRequest>() }
    var cancionIndex by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var progreso by remember { mutableStateOf(0f) }
    var posActual by remember { mutableStateOf(0) }
    var colorDominante by remember { mutableStateOf(Color(0xFF2E2B29)) }
    var playlistNombre by remember { mutableStateOf("Playlist") }
    var playlistImagen by remember { mutableStateOf<String?>(null) }
    var swipeHandled by remember { mutableStateOf(false) }
    var ultimaCancionSeleccionadaIndex by remember { mutableStateOf(-1) }

    // Carga inicial
    LaunchedEffect(idAmigo) {
        try {
            RetrofitClient.apiService.obtenerCantidadFavoritas(idAmigo).let { resp ->
                if (resp.isSuccessful) cancionesMeGusta = resp.body()?.toString() ?: "0"
            }
            RetrofitClient.apiService.getPlaylistInfo(idAmigo).let { resp ->
                if (resp.isSuccessful) resp.body()?.let {
                    playlistNombre = it.nombre ?: "Playlist"
                    playlistImagen = it.imagen_playlist
                }
            }
            val lista = RetrofitClient.apiService.getPlaylist(idAmigo)
            canciones.clear(); canciones.addAll(lista)
        } catch (e: Exception) {
            Log.e("PerfilAmigosScreen", "Error cargando datos", e)
        }
    }

    // Progreso del reproductor
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            progreso = if (mediaPlayer.duration > 0)
                (mediaPlayer.currentPosition / mediaPlayer.duration.toFloat()).coerceIn(0f, 1f)
            else 0f
            delay(300)
        }
    }

    // Función para reproducir canción en índice dado
    fun playSongAt(index: Int) {
        val song = canciones.getOrNull(index) ?: return
        cancionIndex = index
        ultimaCancionSeleccionadaIndex = index
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(song.archivo_mp3)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener { mp ->
                mp.start()
                isPlaying = true
                // Detectar cuándo termina la canción y pasar automáticamente a la siguiente
                mp.setOnCompletionListener {
                    isPlaying = false
                    mediaPlayer.reset()
                    scope.launch {
                        delay(200)
                        playSongAt((index + 1) % canciones.size)
                    }
                }
                // actualizar color dominante
                song.urlImagen?.let { url ->
                    scope.launch {
                        try {
                            val request = ImageRequest.Builder(context)
                                .data(url)
                                .allowHardware(false)
                                .build()
                            val result = coil.ImageLoader(context).execute(request)
                            if (result is SuccessResult) {
                                (result.drawable as? android.graphics.drawable.BitmapDrawable)
                                    ?.bitmap?.let { bmp ->
                                        val palette = Palette.from(bmp).generate()
                                        val dom = palette.getDominantColor(colorDominante.toArgb())
                                        colorDominante = Color(dom)
                                    }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PerfilAmigosScreen", "Error al reproducir canción", e)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        bottomBar = {
            if (ultimaCancionSeleccionadaIndex != -1 && canciones.isNotEmpty()) {
                Surface(
                    tonalElevation = 8.dp,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Column(
                        Modifier
                            .background(Color.Black)
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectHorizontalDragGestures { _, dragAmount ->
                                        if (!swipeHandled) {
                                            swipeHandled = true
                                            if (dragAmount > 30f)
                                                playSongAt((cancionIndex - 1).coerceAtLeast(0))
                                            else if (dragAmount < -30f)
                                                playSongAt((cancionIndex + 1)
                                                    .coerceAtMost(canciones.lastIndex))
                                        }
                                    }
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                playSongAt((cancionIndex - 1).coerceAtLeast(0))
                            }) {
                                Icon(Icons.AutoMirrored.Filled.NavigateBefore, null,
                                    tint = Color.White)
                            }
                            AsyncImage(
                                model = canciones.getOrNull(cancionIndex)?.urlImagen ?: "",
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    canciones.getOrNull(cancionIndex)?.titulo?.takeIf { !it.isNullOrBlank() } ?: "Sin título",
                                    color = Color.White, fontSize = 16.sp
                                )
                                Text(
                                    canciones.getOrNull(cancionIndex)?.autor?.takeIf { !it.isNullOrBlank() } ?: "Sin autor",
                                    color = Color.White, fontSize = 12.sp
                                )
                            }
                            IconButton(onClick = {
                                if (isPlaying) {
                                    mediaPlayer.pause()
                                    posActual = mediaPlayer.currentPosition
                                    isPlaying = false
                                } else {
                                    if (mediaPlayer.currentPosition > 0) {
                                        mediaPlayer.seekTo(posActual)
                                        mediaPlayer.start()
                                        isPlaying = true
                                    } else playSongAt(cancionIndex)
                                }
                            }) {
                                Icon(
                                    painter = painterResource(
                                        R.drawable.playswipe_logo_sinfondo_sinletras_
                                    ),
                                    contentDescription = "Play/Pause",
                                    tint = Color.White
                                )
                            }
                            IconButton(onClick = {
                                playSongAt((cancionIndex + 1).coerceAtMost(canciones.lastIndex))
                            }) {
                                Icon(Icons.AutoMirrored.Filled.NavigateNext, null,
                                    tint = Color.White)
                            }
                        }
                        LinearProgressIndicator(
                            progress = progreso,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            color = Color.LightGray,
                            trackColor = Color.DarkGray
                        )
                        Refuerzo.BarraNavegacionInferior(navController)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = urlImagen ?: "",
                    contentDescription = "Imagen amigo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("ID: $idAmigo", color = Color.White)
                    Text("Favoritas: $cancionesMeGusta", color = Color.White)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(text = playlistNombre.takeIf { !it.isNullOrBlank() } ?: "Playlist",
                color = Color.White,
                fontSize = 22.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("Playlist de ${nombreAmigo.takeIf { !it.isNullOrBlank() } ?: "Desconocido"}", color = Color.White, fontSize = 20.sp)

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(canciones) { index, cancion ->
                    Card(
                        onClick = { playSongAt(index) },
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
                                model = cancion.urlImagen?.takeIf { it.isNotBlank() } ?: "https://via.placeholder.com/50",
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    cancion.titulo?.takeIf { it?.isNotBlank() == true } ?: "Sin título",
                                    color = if (ultimaCancionSeleccionadaIndex == index)
                                        Color(0xFFF41E40) else Color.White,
                                    fontSize = 16.sp
                                )
                                Text(
                                    cancion.autor?.takeIf { it?.isNotBlank() == true } ?: "Sin autor",
                                    color = if (ultimaCancionSeleccionadaIndex == index)
                                        Color(0xFFF41E40) else Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                            IconButton(onClick = { playSongAt(index) }) {
                                Icon(
                                    painter = painterResource(
                                        R.drawable.playswipe_logo_sinfondo_sinletras_
                                    ),
                                    contentDescription = "Play",
                                    tint = Color(0xFFF41E40)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
