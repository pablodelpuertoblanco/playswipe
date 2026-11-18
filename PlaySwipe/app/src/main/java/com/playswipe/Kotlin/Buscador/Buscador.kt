package com.playswipe.Kotlin.Buscador

import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.playswipe.Kotlin.ConexionesBack.RetrofitClient
import com.playswipe.Kotlin.Sesion
import com.playswipe.R
import com.playswipe.utils.Refuerzo
import kotlinx.coroutines.launch

// Constantes para uniformidad
private val SongImageSize  = 50.dp
private val SongTitleSize  = 16.sp
private val SongAuthorSize = 12.sp

data class Cancion(
    val id: Long,
    val titulo: String,
    val autor: String,
    val archivo_mp3: String,
    val urlImagen: String
)

@Preview(showBackground = true)
@Composable
fun BuscadorPreview() {
    BuscadorScreen(rememberNavController())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscadorScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mediaPlayer = remember { MediaPlayer() }
    DisposableEffect(Unit) {
        onDispose {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                mediaPlayer.release()
            }
        }
    }

    var cancionActual by remember { mutableStateOf<String?>(null) }
    var buscadorTexto by remember { mutableStateOf("") }
    val todasLasCanciones = remember { mutableStateListOf<Cancion>() }
    var posicionActual by remember { mutableStateOf(0) }
    var colorDominante by remember { mutableStateOf(Color(0xFF2E2B29)) }

    // Carga inicial
    LaunchedEffect(Unit) {
        try {
            val lista = RetrofitClient.apiService.getCanciones(inicio = 0, cantidad = 9999)
            todasLasCanciones += lista.map {
                Cancion(it.id, it.titulo, it.autor, it.archivo_mp3, it.urlImagen ?: "")
            }
        } catch (e: Exception) {
            Log.e("Buscador", "Error backend", e)
            Toast.makeText(context, "Error al cargar canciones", Toast.LENGTH_SHORT).show()
        }
    }

    val textoBuscado = buscadorTexto.trim()
    val filtradas = todasLasCanciones
        .filter { c ->
            c.titulo.startsWith(textoBuscado, ignoreCase = true)
                    || c.autor.startsWith(textoBuscado, ignoreCase = true)
        }
        .sortedBy { it.titulo.lowercase() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        bottomBar = {
            Column {
                // Mini-player si hay canción actual
                cancionActual?.let { key ->
                    filtradas.find { it.archivo_mp3 == key }?.let { c ->
                        MiniPlayer(
                            cancion = c,
                            fondo = Color.Black,

                            colorTexto = Color.White,

                            colorSub = Color.LightGray,

                            mediaPlayer = mediaPlayer,
                            posActual = posicionActual,
                            onPosicionStore = { posicionActual = it },
                            onAddToPlaylist = { songId ->
                                Sesion.userId?.let { uid ->
                                    scope.launch {
                                        try {
                                            RetrofitClient.apiService.anadirCancionPlaylist(uid, mapOf("songId" to songId))
                                        } catch (_: Exception) {}
                                    }
                                } ?: Toast.makeText(context, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
                // Barra de navegación inferior
                Refuerzo.BarraNavegacionInferior(navController)
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(inner)
        ) {
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = buscadorTexto,
                onValueChange = { if (it.length <= 25) buscadorTexto = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),

                leadingIcon = { Icon(Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.Red) },

                placeholder = { Text("Busca canción o artista...",
                    color = Color.Gray,
                    fontSize = 14.sp) }
            )
            Spacer(Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                items(filtradas) { c ->
                    CancionRow(
                        cancion = c,
                        isSelected = cancionActual == c.archivo_mp3,
                        onClick = {
                            if (c.archivo_mp3 == cancionActual) {
                                if (mediaPlayer.isPlaying) {
                                    mediaPlayer.pause()
                                    posicionActual = mediaPlayer.currentPosition
                                } else {
                                    mediaPlayer.seekTo(posicionActual)
                                    mediaPlayer.start()
                                }
                            } else {
                                mediaPlayer.reset()
                                mediaPlayer.setDataSource(c.archivo_mp3)
                                mediaPlayer.prepare()
                                mediaPlayer.start()
                                cancionActual = c.archivo_mp3
                                posicionActual = 0
                                mediaPlayer.setOnCompletionListener {
                                    cancionActual = null
                                }
                                scope.launch {
                                    try {
                                        val loader = ImageLoader(context)
                                        val req = ImageRequest.Builder(context).data(c.urlImagen)
                                            .allowHardware(false).build()
                                        val res = loader.execute(req) as SuccessResult
                                        val bmp = (res.drawable as android.graphics.drawable.BitmapDrawable).bitmap
                                        colorDominante = Palette.from(bmp).generate()
                                            .dominantSwatch?.rgb?.let { Color(it) } ?: Color(0xFF2E2B29)
                                    } catch (_: Exception) { }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CancionRow(
    cancion: Cancion,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color.Black, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = cancion.urlImagen.ifBlank { R.drawable.playswipe_logo_sinfondo_sinletras_ },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(SongImageSize)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {

            Text(cancion.titulo, color = if (isSelected) Color(0xFFF41E40)
            else Color.White,
                fontSize = SongTitleSize)

            Text(cancion.autor, color = if (isSelected) Color(0xFFF41E40)
            else Color.Gray,
                fontSize = SongAuthorSize)
        }
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(id = R.drawable.playswipe_logo_sinfondo_sinletras_),
                contentDescription = if (isSelected) "Pausar" else "Reproducir",
                tint = Color(0xFFF41E40)
            )
        }
    }
}

@Composable
fun MiniPlayer(
    cancion: Cancion,
    fondo: Color,
    colorTexto: Color,
    colorSub: Color,
    mediaPlayer: MediaPlayer,
    posActual: Int,
    onPosicionStore: (Int) -> Unit,
    onAddToPlaylist: (Long) -> Unit
) {
    Surface(tonalElevation = 8.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(fondo)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = cancion.urlImagen.ifBlank { R.drawable.playswipe_logo_sinfondo_sinletras_ },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(SongImageSize)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {

                Text(cancion.titulo,
                    color = colorTexto,
                    fontSize = SongTitleSize)

                Text(cancion.autor,
                    color = colorSub,
                    fontSize = SongAuthorSize)
            }
            IconButton(onClick = {
                onAddToPlaylist(cancion.id)
            }) {
                Icon(
                    painter = painterResource(R.drawable.anadirplaylist),
                    contentDescription = "Añadir",
                    tint = colorTexto
                )
            }
            IconButton(onClick = {
                if (mediaPlayer.isPlaying) {
                    onPosicionStore(mediaPlayer.currentPosition)
                    mediaPlayer.pause()
                } else {
                    mediaPlayer.seekTo(posActual)
                    mediaPlayer.start()
                }
            }) {
                Icon(
                    painter = painterResource(R.drawable.playswipe_logo_sinfondo_sinletras_),
                    contentDescription = "Play/Pause",
                    tint = colorTexto
                )
            }
        }
    }
}