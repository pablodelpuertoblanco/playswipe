package com.playswipe.Kotlin.InicioSesion

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.playswipe.Kotlin.ConexionesBack.LoginRequest
import com.playswipe.Kotlin.ConexionesBack.RetrofitClient
import com.playswipe.Kotlin.Sesion          // ← NUEVO import
import com.playswipe.R
import com.playswipe.utils.Refuerzo
import kotlinx.coroutines.launch

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PantallaLoginPreview() {
    PantallaLoginScreen(navController = rememberNavController())
}

@Composable
fun PantallaLoginScreen(navController: NavController) {

    val usuario = remember { mutableStateOf("") }
    val contrasena = remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { innerPadding ->

        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement   = Arrangement.Center
            ) {
                LogoApp()
                Spacer(Modifier.height(10.dp))

                NombreUsuario(usuario)
                Spacer(Modifier.height(10.dp))

                Contrasena(contrasena)
                Spacer(Modifier.height(20.dp))

                BotonIniciarSesion(navController, usuario.value, contrasena.value)
                Spacer(Modifier.height(10.dp))

                BotonGoogle()
                Spacer(Modifier.height(10.dp))

                BotonRegistrarse(navController)
                Spacer(Modifier.height(50.dp))
            }
        }
    }
}


@Composable
fun LogoApp() {
    Image(
        painter = painterResource(id = R.drawable.playswipe_logo_sinfondo__1_),
        contentDescription = "PlaySwipe Logo",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .aspectRatio(1f)
            .scale(1.2f)
    )
}

@Composable
fun NombreUsuario(username: MutableState<String>) {
    OutlinedTextField(
        value = username.value,
        onValueChange = {
            if (it.length <= 20) username.value = it
        },
        label = { Text("Nombre de Usuario") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.Gray,
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Color.White,
            cursorColor = Color.White
        )
    )
}

@Composable
fun Contrasena(password: MutableState<String>) {
    OutlinedTextField(
        value = password.value,
        onValueChange = {
            if (it.length <= 20) password.value = it
        },
        label = { Text("Contraseña") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.Gray,
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Color.White,
            cursorColor = Color.White
        )
    )
}

@Composable
fun BotonIniciarSesion(navController: NavController, user: String, pass: String) {

    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    Button(
        onClick = {
            scope.launch {

                if (user.isBlank() || pass.isBlank()) {
                    Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                if (pass.length < 6 || pass.length > 20) {
                    Toast.makeText(context, "La contraseña debe tener entre 6 y 20 caracteres", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val loginRequest = LoginRequest(user.trim(), pass.trim())

                try {
                    val response = RetrofitClient.apiService.loginUsuario(loginRequest)

                    if (response.isSuccessful) {
                        val body = response.body()


                        Sesion.userId = body?.id_usuario


                        val prefs = context.getSharedPreferences("datos_usuario", Context.MODE_PRIVATE)
                        with(prefs.edit()) {
                            putString("nombre_usuario", body?.nombre ?: "Invitado")
                            putString("id_usuario",    body?.id_usuario ?: "Sin ID")
                            if (body?.urlImagenPerfil != null)
                                putString("urlImagenPerfil", body.urlImagenPerfil)
                            else
                                remove("urlImagenPerfil")
                            apply()
                        }
                        Log.d("LoginDebug", "🔐 id_usuario guardado: ${body?.id_usuario}")

                        Toast.makeText(context, body?.mensaje ?: "Sesión iniciada", Toast.LENGTH_SHORT).show()
                        navController.navigate("pagina_principal")

                    } else {
                        val msg = when (response.code()) {
                            401 -> "Contraseña incorrecta"
                            404 -> "Usuario no encontrado"
                            else -> "Error desconocido"
                        }
                        Toast.makeText(context, "❌ $msg", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Log.e("Login", "Error inesperado", e)
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White
        ),
        border = BorderStroke(1.dp, Color.White)
    ) {
        Text("Iniciar Sesión", fontSize = 16.sp)
    }
}

@Composable
fun BotonGoogle() {
    Button(
        onClick = { /* acción con Google */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        border = BorderStroke(2.dp, Color.White)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.google_sinfondo),
                contentDescription = "Google",
                modifier = Modifier.size(25.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Continuar con Google", fontSize = 14.sp)
        }
    }
}

@Composable
fun BotonRegistrarse(navController: NavController) {
    Button(
        onClick = { navController.navigate("registro") },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor   = Color.White
        )
    ) {
        Text(
            "Registrarse en PlaySwipe",
            fontSize  = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
