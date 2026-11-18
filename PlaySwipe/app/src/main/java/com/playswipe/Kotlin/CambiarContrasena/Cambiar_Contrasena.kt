package com.playswipe.Kotlin.CambiarContrasena

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.playswipe.Kotlin.ConexionesBack.RetrofitClient
import com.playswipe.utils.Refuerzo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Preview(showBackground = true)
@Composable
fun CambiarContrasenaPreview() {
    CambiarContrasenaScreen(rememberNavController())
}


@Composable
fun CambiarContrasenaScreen(navController: NavController) {
    // Estados
    var contrasenaActual by remember { mutableStateOf("") }
    var nuevaContrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }

    val context = LocalContext.current
    var nombreUsuarioActual by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("datos_usuario", Context.MODE_PRIVATE)
        nombreUsuarioActual = prefs.getString("nombre_usuario", "") ?: ""
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        bottomBar = {
            Refuerzo.BarraNavegacionInferior(navController)
        }
    ) { innerPadding ->
        // Box para que el contenido no se superponga con la bottom bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
                .padding(horizontal = 16.dp)
        ) {
            // Columna sin scroll (si excede, se recorta)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,  // Centra verticalmente
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                TituloCambiarContrasena()

                Spacer(modifier = Modifier.height(24.dp))


                Column(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalAlignment = Alignment.Start
                ) {
                    CampoContrasenaActual(
                        contrasenaActual = contrasenaActual,
                        onValueChange = { if (it.length <= 20) contrasenaActual = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    CampoNuevaContrasena(
                        nuevaContrasena = nuevaContrasena,
                        onValueChange = { if (it.length <= 20) nuevaContrasena = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    CampoConfirmarContrasena(
                        confirmarContrasena = confirmarContrasena,
                        onValueChange = { if (it.length <= 20) confirmarContrasena = it }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))


                BotonCambiarContrasena(
                    contrasenaActual = contrasenaActual,
                    nuevaContrasena = nuevaContrasena,
                    confirmarContrasena = confirmarContrasena,
                    nombreUsuario = nombreUsuarioActual,
                    navController = navController
                )
            }
        }
    }
}

//Título
@Composable
fun TituloCambiarContrasena() {
    Text(
        text = "Cambiar Contraseña",
        color = Color.White,
        fontSize = 24.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 40.dp, bottom = 24.dp)
    )
}

//Campo: Contraseña actual
@Composable
fun CampoContrasenaActual(
    contrasenaActual: String,
    onValueChange: (String) -> Unit
) {
    Text(
        text = "Contraseña Actual",
        color = Color.White,
        fontSize = 14.sp,
        textAlign = TextAlign.Start
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = contrasenaActual,
        onValueChange = { if (it.length <= 20) onValueChange(it) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,


        textStyle = LocalTextStyle.current.copy(color = Color.White),


        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White,
            disabledBorderColor = Color.White,
            cursorColor = Color.White,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White,
            disabledLabelColor = Color.White
        )
    )
}

//Campo: Nueva contraseña
@Composable
fun CampoNuevaContrasena(
    nuevaContrasena: String,
    onValueChange: (String) -> Unit
) {
    Text(
        text = "Nueva Contraseña",
        color = Color.White,
        fontSize = 14.sp,
        textAlign = TextAlign.Start
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = nuevaContrasena,
        onValueChange = { if (it.length <= 20) onValueChange(it) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,


        textStyle = LocalTextStyle.current.copy(color = Color.White),


        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White,
            disabledBorderColor = Color.White,
            cursorColor = Color.White,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White,
            disabledLabelColor = Color.White
        )
    )
}

//Campo: Confirmar nueva contraseña
@Composable
fun CampoConfirmarContrasena(
    confirmarContrasena: String,
    onValueChange: (String) -> Unit
) {
    Text(
        text = "Confirmar Nueva Contraseña",
        color = Color.White,
        fontSize = 14.sp,
        textAlign = TextAlign.Start
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = confirmarContrasena,
        onValueChange = { if (it.length <= 20) onValueChange(it) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,


        textStyle = LocalTextStyle.current.copy(color = Color.White),


        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White,
            disabledBorderColor = Color.White,
            cursorColor = Color.White,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White,
            disabledLabelColor = Color.White
        )
    )
}

//Botón "Cambiar Contraseña"
@Composable
fun BotonCambiarContrasena(
    contrasenaActual: String,
    nuevaContrasena: String,
    confirmarContrasena: String,
    nombreUsuario: String,
    navController: NavController
) {

    Text(
        text = "Usuario actual = $nombreUsuario",
        color = Color.Gray,
        modifier = Modifier.padding(top = 8.dp)
    )
    val context = LocalContext.current
    Button(
        onClick = {
            if (nombreUsuario.isNotBlank() &&
                contrasenaActual.isNotBlank() &&
                nuevaContrasena == confirmarContrasena &&
                nuevaContrasena.length >= 6
            ) {
                val datos = mapOf(
                    "nombreUsuario" to nombreUsuario,
                    "contrasenaActual" to contrasenaActual,
                    "nuevaContrasena" to nuevaContrasena
                )

                RetrofitClient.apiService.cambiarContrasena(datos).enqueue(object : Callback<Map<String, String>> {
                    override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                        val mensajeRespuesta = response.body()?.get("mensaje") ?: "Error inesperado"
                        Toast.makeText(context, mensajeRespuesta, Toast.LENGTH_SHORT).show()
                        if (response.body()?.get("estado") == "ok") {
                            navController.popBackStack()
                        }
                    }

                    override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                        Toast.makeText(context, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(context, "Verifica que los campos estén completos y que las contraseñas coincidan.", Toast.LENGTH_SHORT).show()
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
        Text("Cambiar Contraseña", fontSize = 16.sp)
    }
}