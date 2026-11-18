package com.playswipe.Reproductor

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.playswipe.Kotlin.ConexionesBack.RetrofitClient
import com.playswipe.R
import com.playswipe.utils.Refuerzo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape

private const val SWIPE_THRESHOLD = 400f
private const val RECENT_LIMIT = 10

data class Cancion(
    val id: Int,
    val titulo: String,
    val autor: String,
    val urlImagen: String?,
    val urlAudio: String
)

data class OnboardingData(
    val titulo: String,
    val descripcion: String,
    val imagen: Int
)

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun VistaPreviaReproductor() {
    PantallaPrincipal(rememberNavController())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(navController: NavController) {
    val contexto = LocalContext.current
    val preferencias = contexto.getSharedPreferences("datos_usuario", Context.MODE_PRIVATE)
    val userId = remember { preferencias.getString("id_usuario", "") ?: "" }
    val claveTutorial = "tutorial_mostrado_$userId"
    var mostrarTutorial by remember { mutableStateOf(false) }

    // Historial de IDs de las últimas canciones reproducidas
    var recientes by remember { mutableStateOf<List<Int>>(emptyList()) }

    // Estados y scope para corutinas y errores
    val scope = rememberCoroutineScope()
    var mensajeErrorSave by remember { mutableStateOf<String?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    // ExoPlayer y gestos
    val exoPlayer = remember { ExoPlayer.Builder(contexto).build() }
    var desplazamientoX by remember { mutableStateOf(0f) }
    var arrastreX by remember { mutableStateOf(0f) }
    var opacidadInicial by remember { mutableStateOf(0.5f) }

    var canciones by remember { mutableStateOf<List<Cancion>>(emptyList()) }
    var canciónActual by remember { mutableStateOf<Cancion?>(null) }

    // Estado compartido de reproducción
    var estaReproduciendo by remember { mutableStateOf(false) }

    // Función para obtener canciones del backend
    suspend fun obtenerCanciones(): List<Cancion> {
        return try {
            val resultado = withContext(Dispatchers.IO) { RetrofitClient.apiService.getCanciones(0, 1000) }
            val lista = resultado.map {
                Cancion(it.id.toInt(), it.titulo, it.autor, it.urlImagen, it.archivo_mp3)
            }
            mensajeError = if (lista.isEmpty()) "No hay canciones disponibles" else null
            lista
        } catch (e: Exception) {
            mensajeError = "Error al cargar canciones: ${e.localizedMessage}"
            emptyList()
        }
    }

    // Cambia a una canción aleatoria sin repetir las últimas RECENT_LIMIT
    fun cambiarCanciónAleatoria() {
        val disponibles = canciones.filterNot { recientes.contains(it.id) }
        val nueva = if (disponibles.isNotEmpty()) {
            disponibles.random()
        } else {
            // Reiniciar historial si todas han sido reproducidas recientemente
            recientes = emptyList()
            canciones.random()
        }
        // Actualizar historial y truncar a RECENT_LIMIT
        recientes = (recientes + nueva.id).takeLast(RECENT_LIMIT)

        // Reiniciar animaciones y reproducir
        exoPlayer.stop()
        desplazamientoX = 0f
        arrastreX = 0f
        opacidadInicial = 0.5f
        val anteriorId = canciónActual?.id
        canciónActual = nueva
        if (anteriorId != nueva.id) {
            if (!mostrarTutorial && !exoPlayer.isPlaying) {
                exoPlayer.setMediaItem(MediaItem.fromUri(nueva.urlAudio))
                exoPlayer.prepare()
                exoPlayer.play()
                estaReproduciendo = true
            } else {
                exoPlayer.stop()
                estaReproduciendo = false
            }
        }
    }

    // Inicializa la lista y primer tema
    LaunchedEffect(Unit) {
        mostrarTutorial = !preferencias.getBoolean(claveTutorial, false)
        if (!mostrarTutorial) {
            canciones = obtenerCanciones()
            if (canciones.isNotEmpty()) {
                cambiarCanciónAleatoria()
            }
        }
    }

    // Animaciones de swipe
    val desplazamientoAnim by animateFloatAsState(
        targetValue = when {
            arrastreX > SWIPE_THRESHOLD -> 800f
            arrastreX < -SWIPE_THRESHOLD -> -800f
            else -> desplazamientoX
        }, animationSpec = tween(300)
    )
    val opacidad by animateFloatAsState(
        targetValue = if (desplazamientoAnim == 0f) 1f else opacidadInicial,
        animationSpec = tween(500)
    )

    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }

    if (mostrarTutorial) {
        TutorialOnboarding(onFinish = {
            mostrarTutorial = false
            preferencias.edit().putBoolean(claveTutorial, true).apply()
            scope.launch {
                canciones = obtenerCanciones()
                if (canciones.isNotEmpty()) {
                    cambiarCanciónAleatoria()
                }
            }
        })
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Black,
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = { navController.navigate("Ajustes") }) {
                            Icon(painter = painterResource(id = R.drawable.ajustes),
                                contentDescription = "Ajustes",
                                modifier = Modifier
                                    .size(40.dp),
                                tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Black)
                )
            },
            bottomBar = { Refuerzo.BarraNavegacionInferior(navController) }
        ) { innerPadding ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)) {

                Column(modifier = Modifier
                    .align(Alignment.Center)
                    .wrapContentSize(),
                    horizontalAlignment = Alignment.CenterHorizontally) {

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(desplazamientoAnim.roundToInt(), 0) }
                            .alpha(opacidad)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        desplazamientoX += dragAmount.x
                                        arrastreX += dragAmount.x
                                    },
                                    onDragEnd = {
                                        if (arrastreX > SWIPE_THRESHOLD && canciónActual != null) {
                                            // Guardar en playlist si se desliza a la derecha
                                            val idToSave = canciónActual!!.id
                                            scope.launch {
                                                try {
                                                    val uid = userId.ifBlank { return@launch }
                                                    val playlist = withContext(Dispatchers.IO) { RetrofitClient.apiService.getPlaylist(uid) }
                                                    if (playlist.any { it.id.toInt() == idToSave }) {
                                                        Toast.makeText(contexto, "La canción ya está en la playlist", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        val response = withContext(Dispatchers.IO) { RetrofitClient.apiService.anadirCancionPlaylist(uid, mapOf("songId" to idToSave.toLong())) }
                                                        if (response.isSuccessful) Toast.makeText(contexto, "Canción añadida a playlist", Toast.LENGTH_SHORT).show()
                                                        else mensajeErrorSave = "No se pudo guardar la canción"
                                                    }
                                                } catch (_: Exception) {
                                                    mensajeErrorSave = "No se pudo guardar la canción"
                                                }
                                            }
                                            cambiarCanciónAleatoria()
                                        } else if (arrastreX < -SWIPE_THRESHOLD) {
                                            cambiarCanciónAleatoria()
                                        }
                                        desplazamientoX = 0f
                                        arrastreX = 0f
                                    }
                                )
                            }
                    ) {
                        canciónActual?.let { TarjetaDeReproductor(it, exoPlayer, estaReproduciendo) { estaReproduciendo = it } }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    mensajeError?.let { Text(text = it,
                        color = Color.Red,
                        modifier = Modifier
                            .padding(16.dp),
                        textAlign = TextAlign.Center) }

                    mensajeErrorSave?.let { Text(text = it,
                        color = Color.Red,
                        modifier = Modifier
                            .padding(16.dp),
                        textAlign = TextAlign.Center) }
                }
            }
        }
    }
}

@Composable
fun TarjetaDeReproductor(
    cancion: Cancion,
    exoPlayer: ExoPlayer,
    estaReproduciendo: Boolean,
    onReproduccionChange: (Boolean) -> Unit
) {
    var progreso by remember(cancion.id) { mutableStateOf(0f) }
    var duracion by remember(cancion.id) { mutableStateOf(0L) }

    LaunchedEffect(estaReproduciendo, cancion.id) {
        // No reiniciar progreso ni duración al pausar
        while (estaReproduciendo && currentCoroutineContext().isActive) {
            try {
                val dur = exoPlayer.duration
                val pos = exoPlayer.currentPosition
                if (dur > 0) {
                    duracion = dur
                    progreso = pos.toFloat() / dur
                } else {
                    progreso = 0f
                    duracion = 0L
                }
            } catch (_: Exception) {
                progreso = 0f
                duracion = 0L
            }
            delay(500)
        }
    }

    Card(modifier = Modifier.fillMaxWidth(0.8f),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4A4A4A))) {

        Column(modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            if (!cancion.urlImagen.isNullOrBlank()) {
                AsyncImage(model = cancion.urlImagen,
                    contentDescription = "Portada del álbum",
                    modifier = Modifier
                        .size(250.dp)
                        .padding(8.dp),
                    contentScale = ContentScale.Crop)
            } else {
                Image(painter = painterResource(id = R.drawable.album),
                    contentDescription = "Portada por defecto",
                    modifier = Modifier
                        .size(250.dp)
                        .padding(8.dp),
                    contentScale = ContentScale.Crop)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = cancion.titulo,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = cancion.autor,
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(12.dp))

    IconButton(onClick = {
        if (!estaReproduciendo) {
            if (!exoPlayer.isPlaying) {
                exoPlayer.play()
                onReproduccionChange(true)
            }
        } else {
            exoPlayer.pause()
            onReproduccionChange(false)
        }
    }) {
                Icon(
                    painter = painterResource(id = if (estaReproduciendo) R.drawable.playswipe_logo_sinfondo_sinletras_
                    else R.drawable.perfil),

                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
            }

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color.Black)) {

                Box(modifier = Modifier
                    .fillMaxWidth(progreso)
                    .height(8.dp)
                    .background(Color(0XFFF41E40)))
            }
        }
    }
}

@Composable
fun TutorialOnboarding(onFinish: () -> Unit) {
    val paginas = listOf(
        OnboardingData(titulo = "Fucionamiento principal",
            descripcion = "Desliza a izquierda o derecha para descubrir nueva música. Derecha significa que te gusta, Izquierda que no te gusta.",
            imagen = R.drawable.flechas),

        OnboardingData(titulo = "Sube tus canciones",
            descripcion = "Pulsa el botón de añadir canciones para poder subir tu música.",
            imagen = R.drawable.add),

        OnboardingData(titulo = "Conecta con amigos",
            descripcion = "Agrega amigos y comparte tu música.",
            imagen = R.drawable.anadiramigo)
    )
    val pagerState = rememberPagerState(pageCount = { paginas.size }, initialPage = 0)
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .padding(24.dp),
        contentAlignment = Alignment.Center) {

        Column(modifier = Modifier
            .fillMaxWidth(0.9f)
            .background(color = Color(0xCCF81C3D),
                shape = RoundedCornerShape(20.dp))
            .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {

            Text(text = "TUTORIAL",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black)

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalPager(state = pagerState,
                modifier = Modifier
                .weight(1f)) { page ->

                OnboardingPage(titulo = paginas[page].titulo,
                    descripcion = paginas[page].descripcion,
                    imagenRes = paginas[page].imagen,
                    esUltima = page == paginas.lastIndex,
                    onFinish = onFinish)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()) {

                repeat(paginas.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(modifier = Modifier.padding(4.dp).
                    size(if (isSelected) 12.dp else 8.dp).
                    background(color = if (isSelected) Color.White
                    else Color.Gray,
                        shape = RoundedCornerShape(50)))
                }
            }
        }
    }
}

@Composable
fun OnboardingPage(titulo: String, descripcion: String, imagenRes: Int, esUltima: Boolean, onFinish: (() -> Unit)? = null) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {

        Image(painter = painterResource(id = imagenRes),
            contentDescription = titulo,
            modifier = Modifier
                .size(150.dp)
                .padding(8.dp))

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = titulo,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.White)

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = descripcion,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = Color.White)

        Spacer(modifier = Modifier.height(24.dp))

        if (esUltima && onFinish != null) {
            Button(onClick = { onFinish() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                Text(text = "¡Empezar!", color = Color.White)
            }
        }
    }
}
