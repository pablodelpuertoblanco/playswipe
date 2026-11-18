package com.playswipe.Kotlin.CambiarNombre

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.playswipe.R
import com.playswipe.utils.Refuerzo
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.playswipe.Kotlin.ConexionesBack.RetrofitClient

@Preview(showBackground = true)
@Composable
fun CambiarNombrePreview() {
    CambiarNombreScreen(rememberNavController())
}

@Composable
fun CambiarNombreScreen(navController: NavController) {
    // Estados
    var nombreDeUsuarioActual by remember { mutableStateOf("") }
    var nuevoNombreDeUsuario by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        bottomBar = {
            Refuerzo.BarraNavegacionInferior(navController)
        }
    ) { innerPadding ->
        // Box para manejar el padding y no solapar con la bottom bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
                .padding(horizontal = 16.dp)
        ) {
            // Column sin scroll, centrada verticalmente
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center, // centra vertical si sobra espacio
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                TituloCambiarNombre()

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalAlignment = Alignment.Start
                ) {
                    CampoUsuarioActual(
                        nombreDeUsuarioActual = nombreDeUsuarioActual,
                        onValueChange = { if (it.length <= 20) nombreDeUsuarioActual = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CampoNuevoNombre(
                        nuevoNombreDeUsuario = nuevoNombreDeUsuario,
                        onValueChange = { if (it.length <= 20) nuevoNombreDeUsuario = it }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))


                BotonCambiarNombre(
                    nombreActual = nombreDeUsuarioActual,
                    nuevoNombre = nuevoNombreDeUsuario,
                    navController = navController
                )
            }
        }
    }
}

//Título de la pantalla
@Composable
fun TituloCambiarNombre() {
    Text(
        text = "Cambiar Nombre de Usuario",
        color = Color.White,
        fontSize = 22.sp,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 24.dp, start = 17.dp)
    )
}


//Campo para el nombre de usuario actual
@Composable
fun CampoUsuarioActual(
    nombreDeUsuarioActual: String,
    onValueChange: (String) -> Unit
) {
    Text(
        text = "Nombre de Usuario Actual",
        color = Color.White,
        fontSize = 14.sp,
        textAlign = TextAlign.Start
    )
    Spacer(Modifier.height(8.dp))

    OutlinedTextField(
        value = nombreDeUsuarioActual,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White, RoundedCornerShape(4.dp)),
        textStyle = LocalTextStyle.current.copy(color = Color.White),
        colors = OutlinedTextFieldDefaults.colors( cursorColor = Color.White),
        singleLine = true
    )

}

//Campo para el nuevo nombre de usuario
@Composable
fun CampoNuevoNombre(
    nuevoNombreDeUsuario: String,
    onValueChange: (String) -> Unit
) {
    Text(
        text = "Nuevo Nombre de Usuario",
        color = Color.White,
        fontSize = 14.sp,
        textAlign = TextAlign.Start
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = nuevoNombreDeUsuario,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White, RoundedCornerShape(4.dp)),
        textStyle = LocalTextStyle.current.copy(color = Color.White),
        colors = OutlinedTextFieldDefaults.colors( cursorColor = Color.White),
        singleLine = true
    )
}

//Botón para cambiar el nombre de usuario
@Composable
fun BotonCambiarNombre(
    nombreActual: String,
    nuevoNombre: String,
    navController: NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Button(
        onClick = {
            if (nombreActual.isNotBlank() && nuevoNombre.isNotBlank()) {
                coroutineScope.launch {
                    try {
                        val cambioNombreRequest = mapOf(
                            "nombreUsuarioActual" to nombreActual,
                            "nuevoNombreUsuario" to nuevoNombre
                        )

                        val response = RetrofitClient.apiService.cambiarNombreUsuario(cambioNombreRequest)

                        if (response.isSuccessful) {
                            val mensaje = response.body()?.mensaje ?: "Nombre cambiado correctamente"
                            Toast.makeText(context, "✅ $mensaje", Toast.LENGTH_SHORT).show()
                            navController.popBackStack() // vuelve a la pantalla anterior
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Toast.makeText(context, "❌ Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                            Log.e("CambioNombre", "Error en respuesta: $errorBody")
                        }
                    } catch (e: Exception) {
                        Log.e("CambioNombre", "Excepción", e)
                        Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(context, "⚠️ Completa ambos campos", Toast.LENGTH_SHORT).show()
            }
        },
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White
        ),
        border = BorderStroke(1.dp, Color.White)
    ) {
        Text("Cambiar Nombre de Usuario", fontSize = 13.sp)
    }
}