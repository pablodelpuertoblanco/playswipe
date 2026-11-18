package com.playswipe.Kotlin.Perfil


import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.playswipe.utils.Refuerzo
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.storage.FirebaseStorage
import com.playswipe.Kotlin.ConexionesBack.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.content.edit

fun subirImagenPerfilAFirebaseYGuardarURL(
    context: Context,
    uri: Uri,
    onUrlSaved: (String) -> Unit = {}
) {
    val storageRef = FirebaseStorage.getInstance().reference
    val sharedPref = context.getSharedPreferences("datos_usuario", Context.MODE_PRIVATE)
    val idUsuario = sharedPref.getString("id_usuario", null) ?: return
    val perfilRef = storageRef.child("imagenes_perfil/$idUsuario.jpg")

    perfilRef.putFile(uri)
        .addOnSuccessListener {
            perfilRef.downloadUrl.addOnSuccessListener { url ->
                guardarUrlImagenEnBackend(context, idUsuario, url.toString()) {
                    sharedPref.edit() { putString("urlImagenPerfil", url.toString()) }
                    onUrlSaved(url.toString())
                }
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Error al subir imagen a Firebase", Toast.LENGTH_SHORT).show()
        }
}

fun guardarUrlImagenEnBackend(
    context: Context,
    idUsuario: String,
    urlImagen: String,
    onSuccess: () -> Unit = {}
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitClient.apiService.subirUrlImagen(idUsuario, urlImagen)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Imagen actualizada", Toast.LENGTH_SHORT).show()
                    onSuccess()
                } else {
                    Toast.makeText(context, "Error al guardar URL en el servidor", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PerfilViewPreview() {
    PerfilScreen(navController = rememberNavController())
}

@Composable
fun PerfilScreen(navController: NavController) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val sharedPref = context.getSharedPreferences("datos_usuario", Context.MODE_PRIVATE)
        nombreUsuario = sharedPref.getString("nombre_usuario", "Invitado") ?: "Invitado"
        idUsuario = sharedPref.getString("id_usuario", "Sin ID") ?: "Sin ID"

        if (idUsuario != "Sin ID") {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.apiService.obtenerCantidadFavoritas(idUsuario)
                    if (response.isSuccessful) {
                        val cantidad = response.body()?.toString() ?: "0"
                        withContext(Dispatchers.Main) {
                            Log.d("PerfilDebug", "Canciones favoritas recibidas: $cantidad")
                            cancionesMegusta = cantidad
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        cancionesMegusta = "0"
                    }
                }
            }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val responseUsuario = RetrofitClient.apiService.obtenerUsuarioPorId(idUsuario)
                    if (responseUsuario.isSuccessful) {
                        val usuario = responseUsuario.body()
                        withContext(Dispatchers.Main) {
                            usuario?.nombreUsuario?.let {
                                nombreUsuario = it
                                sharedPref.edit().putString("nombre_usuario", it).apply()
                            }
                        }
                    }
                } catch (e: Exception) {

                }
            }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.apiService.obtenerCantidadAmigos(idUsuario)
                    if (response.isSuccessful) {
                        val cantidadAmigos = response.body()?.toString() ?: "0"
                        withContext(Dispatchers.Main) {
                            amigos = cantidadAmigos
                            Log.d("PerfilDebug", "Amigos recibidos: $cantidadAmigos")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        amigos = "0"
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        bottomBar = {

            Refuerzo.BarraNavegacionInferior(navController)
        }
    ) { innerPadding ->
        // Box para no superponer el contenido con la bottom bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            // LazyColumn que muestra los campos
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                    Spacer(Modifier.height(10.dp))

                    TituloPantallaPerfil()

                    Spacer(Modifier.height(10.dp))

                    IconoUsuarioConSaludo()

                    Spacer(Modifier.height(40.dp))

                    SeccionDatosUsuario()

                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }

private var nombreUsuario by mutableStateOf("")
private var amigos by mutableStateOf("")
private var idUsuario by mutableStateOf("")
private var cancionesMegusta by mutableStateOf("")


@Composable
fun TituloPantallaPerfil() {
    Text(
        text = "Perfil",
        color = Color.White,
        fontSize = 22.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
    )
}

//Imagen de perfil + texto de saludo
@Composable
fun IconoUsuarioConSaludo() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sharedPref = context.getSharedPreferences("datos_usuario", Context.MODE_PRIVATE)
    val urlImagenPref = sharedPref.getString("urlImagenPerfil", null)

    var urlImagen by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(urlImagenPref) {
        urlImagen = urlImagenPref
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                subirImagenPerfilAFirebaseYGuardarURL(context, it) { nuevaUrl ->
                    urlImagen = nuevaUrl
                }
            }
        }
    }

    // Asigna la imagen local si no hay urlImagen válida
    val imageUrl = urlImagen

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(180.dp)
            .clickable {
                imagePickerLauncher.launch("image/*")
            }
    ) {
        Image(
            painter = if (!urlImagen.isNullOrEmpty())
                rememberAsyncImagePainter(urlImagen)
            else
                painterResource(id = com.playswipe.R.drawable.user_perfil),
            contentDescription = "Usuario",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )
    }

    Text(
        text = "Bienvenido $nombreUsuario",
        color = Color.White,
        fontSize = 25.sp
    )

    // Botón para eliminar la foto de perfil y restaurar la imagen por defecto
    TextButton(onClick = {
        scope.launch {
            val idUsuarioShared = sharedPref.getString("id_usuario", null)
            if (idUsuarioShared != null) {
                // Elimina la imagen personalizada del usuario en Firebase Storage
                val storageRef = FirebaseStorage.getInstance().reference
                val perfilRef = storageRef.child("imagenes_perfil/$idUsuarioShared.jpg")
                perfilRef.delete().addOnSuccessListener {
                    // Borra la URL en el backend y SharedPreferences
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = RetrofitClient.apiService.subirUrlImagen(idUsuarioShared, "")
                            withContext(Dispatchers.Main) {
                                if (response.isSuccessful) {
                                    sharedPref.edit { remove("urlImagenPerfil") }
                                    urlImagen = null
                                    Toast.makeText(context, "Foto de perfil eliminada", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Error al borrar imagen del servidor", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(context, "No hay imagen personalizada para eliminar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }) {
        Text("Eliminar foto de perfil", color = Color.Red)
    }
}

//Campo editable para el nombre de usuario
@Composable
fun SeccionDatosUsuario() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ColumnaEtiquetas()
        Spacer(Modifier.width(8.dp))
        ColumnaCampos()
    }
}

@Composable
fun ColumnaEtiquetas() {
    Column(
        modifier = Modifier.fillMaxWidth(0.4f),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            "Amigos",
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            "ID de Usuario",
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = "Favoritas",
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

@Composable
fun ColumnaCampos() {
    Column(
        modifier = Modifier.fillMaxWidth(0.6f),
        horizontalAlignment = Alignment.Start
    ) {

        Text(
            text = amigos,
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = idUsuario,
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = cancionesMegusta,
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}
