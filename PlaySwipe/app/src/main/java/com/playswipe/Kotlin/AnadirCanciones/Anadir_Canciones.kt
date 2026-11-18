package com.playswipe.Kotlin.AnadirCanciones


import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.storage.FirebaseStorage
import com.playswipe.Kotlin.ConexionesBack.CancionRequest
import com.playswipe.Kotlin.ConexionesBack.RetrofitClient
import com.playswipe.utils.Refuerzo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID


var imagenUri by mutableStateOf<Uri?>(null)
var audioUri by mutableStateOf<Uri?>(null)

var isUploadingImagen by mutableStateOf(false)
var isUploading by mutableStateOf(false)

private fun seleccionarImagen(
    launcher: ActivityResultLauncher<String>
) = launcher.launch("image/*")

private fun seleccionarAudio(
    launcher: ActivityResultLauncher<String>
) = launcher.launch("audio/*")


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AnadirCancionesPreview() =
    AnadirCancionesScreen(rememberNavController())


@Composable
fun AnadirCancionesScreen(navController: NavController) {

    var nombre by remember { mutableStateOf("") }
    var autor by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }

    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorAutor by remember { mutableStateOf<String?>(null) }
    var errorGenero by remember { mutableStateOf<String?>(null) }


    fun resetCampos() {
        nombre = ""
        autor = ""
        genero = ""
        imagenUri = null
        audioUri = null
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        bottomBar = { Refuerzo.BarraNavegacionInferior(navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(14.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Sube tu Canción",
                        color = Color.White,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))

                    CampoTexto("Nombre de la canción", nombre, { if (it.length <= 25) nombre = it }, errorNombre)

                    Spacer(Modifier.height(10.dp))

                    CampoTexto("Autor", autor, { if (it.length <= 25) autor = it }, errorAutor)

                    Spacer(Modifier.height(10.dp))

                    DropDownGenero(genero, { genero = it }, errorGenero)

                    Spacer(Modifier.height(10.dp))

                    BotonSeleccionImagen()

                    Spacer(Modifier.height(30.dp))

                    Text(
                        "Seleccionar archivo MP3",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))

                    BotonSeleccionAudio()

                    Spacer(Modifier.height(16.dp))

                    BotonSubir(
                        nombre = nombre,
                        autor = autor,
                        genero = genero,
                        onSuccess = { resetCampos() },
                        errorNombreState = { errorNombre = it },
                        errorAutorState = { errorAutor = it },
                        errorGeneroState = { errorGenero = it }
                    )
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun CampoTexto(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    errorMessage: String? = null
) {
    Column(modifier = Modifier
        .padding(horizontal = 8.dp)) {

        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                focusedLabelColor = Color.White,
                cursorColor = Color.White,
                unfocusedBorderColor = if (errorMessage != null) Color.Red
                    else Color.Gray
            )
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun DropDownGenero(
    genero: String,
    onGeneroSel: (String) -> Unit,
    errorMessage: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val opciones = listOf("Rap", "Pop", "Reggaeton", "Rock", "Electronica", "Otros")

    Column(modifier = Modifier
        .padding(horizontal = 8.dp)) {

        OutlinedTextField(
            value = genero,
            onValueChange = {},
            readOnly = true,
            label = { Text("Género") },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null,
                    Modifier.clickable { expanded = true })
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                focusedLabelColor = Color.White,
                cursorColor = Color.White,
                unfocusedBorderColor = if (errorMessage != null) Color.Red
                    else Color.Gray
            )
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            opciones.forEach { op ->
                DropdownMenuItem(
                    text = { Text(op, color = Color.Black,
                    textAlign = TextAlign.Center) },
                    onClick = { onGeneroSel(op); expanded = false }
                )
            }
        }
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun BotonSeleccionImagen() {
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imagenUri = uri
        Toast.makeText(ctx, if (uri != null) "Imagen seleccionada"
            else "No se seleccionó imagen", Toast.LENGTH_SHORT).show()
    }

    Button(
        onClick = { seleccionarImagen(launcher) },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Gray,
            contentColor = Color.White)
    ) { Text("Seleccionar portada") }
}

@Composable
private fun BotonSeleccionAudio() {
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        audioUri = uri
        Toast.makeText(ctx, if (uri != null) "Audio seleccionado"
            else "No se seleccionó audio", Toast.LENGTH_SHORT).show()
    }

    Button(
        onClick = { seleccionarAudio(launcher) },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Gray,
            contentColor = Color.White)
    ) { Text("Seleccionar MP3") }
}

@Composable
private fun BotonSubir(
    nombre: String,
    autor : String,
    genero: String,
    onSuccess: () -> Unit,
    errorNombreState: (String?) -> Unit,
    errorAutorState: (String?) -> Unit,
    errorGeneroState: (String?) -> Unit
) {
    val ctx = LocalContext.current

    Button(
        onClick = {
            val nombreError = if (nombre.isBlank()) "El nombre es obligatorio" else null
            val autorError = if (autor.isBlank()) "El autor es obligatorio" else null
            val generoError = if (genero.isBlank()) "El género es obligatorio" else null

            errorNombreState(nombreError)
            errorAutorState(autorError)
            errorGeneroState(generoError)

            if (nombreError != null || autorError != null || generoError != null || imagenUri == null || audioUri == null) {
                Toast.makeText(ctx, "Completa todos los campos y selecciona archivos", Toast.LENGTH_SHORT).show()
                return@Button
            }

            subirImagenAFirebase(ctx) { urlPortada ->
                if (urlPortada == null) return@subirImagenAFirebase

                subirAudioYRegistrar(
                    ctx, nombre, autor, genero, urlPortada
                ) { ok -> if (ok) onSuccess() }
            }
        },
        enabled = !isUploading && !isUploadingImagen,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isUploading || isUploadingImagen) Color.Gray
                else Color.Black,
            contentColor   = Color.White
        )
    ) {
        if (isUploading || isUploadingImagen) {
            CircularProgressIndicator(color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier
                    .size(24.dp))
        } else {
            Text("Añadir")
        }
    }
}

private fun subirImagenAFirebase(
    contexto: Context,
    onUrl: (String?) -> Unit
) {
    val uri = imagenUri ?: run {
        Toast.makeText(contexto, "Selecciona una imagen", Toast.LENGTH_SHORT).show()
        onUrl(null); return
    }

    val documento = DocumentFile.fromSingleUri(contexto, uri)
    if (documento == null || !documento.exists()) {
        Toast.makeText(contexto, "Imagen no disponible", Toast.LENGTH_SHORT).show()
        onUrl(null); return
    }

    val input = contexto.contentResolver.openInputStream(uri) ?: run {
        Toast.makeText(contexto, "No se pudo abrir la imagen", Toast.LENGTH_SHORT).show(); onUrl(null); return
    }

    isUploadingImagen = true
    val ref = FirebaseStorage.getInstance().reference
        .child("imagenes/${documento.name ?: UUID.randomUUID()}.jpg")

    ref.putStream(input)
        .addOnSuccessListener {
            isUploadingImagen = false
            // Obtén el nombre real del fichero
            val uploadedImageName = documento.name ?: "imagen.jpg"
            // Muestra el Toast correctamente
            Toast.makeText(contexto, "Imagen subida: $uploadedImageName", Toast.LENGTH_SHORT).show()

            ref.downloadUrl.addOnSuccessListener { url ->
                onUrl(url.toString())
            }
        }
        .addOnFailureListener {
            isUploadingImagen = false
            Toast.makeText(contexto, "Error subiendo imagen: ${it.message}", Toast.LENGTH_SHORT).show()
            onUrl(null)
        }
}

    private fun subirAudioYRegistrar(
        contexto: Context,
        titulo: String,
        autor: String,
        genero: String,
        urlImagen: String,
        callback: (Boolean) -> Unit
) {
    val uri = audioUri ?: run {
        Toast.makeText(contexto, "Selecciona un MP3", Toast.LENGTH_SHORT).show()
        callback(false); return
    }

    val documento = DocumentFile.fromSingleUri(contexto, uri)
    if (documento == null || !documento.exists()) {
        Toast.makeText(contexto, "Audio no disponible", Toast.LENGTH_SHORT).show()
        callback(false); return
    }

    val input = contexto.contentResolver.openInputStream(uri) ?: run {
        Toast.makeText(contexto, "No se pudo abrir el MP3", Toast.LENGTH_SHORT).show(); callback(false); return
    }

    isUploading = true
    val ref = FirebaseStorage.getInstance().reference
        .child("audios/${documento.name ?: UUID.randomUUID()}.mp3")

    ref.putStream(input)
        .addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { urlMp3 ->

                val dto = CancionRequest(
                    id = 0L,
                    titulo = titulo,
                    autor = autor,
                    genero = genero,
                    archivo_mp3 = urlMp3.toString(),
                    urlImagen = urlImagen
                )

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val resp = RetrofitClient.apiService.registrarCancion(dto)
                        withContext(Dispatchers.Main) {
                            isUploading = false
                            if (resp.isSuccessful) {
                                Toast.makeText(contexto, "Canción añadida", Toast.LENGTH_SHORT).show()
                                callback(true)
                            } else {
                                Toast.makeText(contexto, "Error backend (${resp.code()})", Toast.LENGTH_SHORT).show()
                                callback(false)
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            isUploading = false
                            Toast.makeText(contexto, "Error conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                            callback(false)
                        }
                    }
                }
            }
        }
        .addOnFailureListener {
            isUploading = false
            Toast.makeText(contexto, "Error subiendo MP3: ${it.message}", Toast.LENGTH_SHORT).show()
            callback(false)
        }
}