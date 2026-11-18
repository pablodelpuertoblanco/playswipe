package com.playswipe.Kotlin.Registro

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.playswipe.R
import com.playswipe.Kotlin.ConexionesBack.UsuarioRegistro
import com.playswipe.Kotlin.ConexionesBack.RetrofitClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun PantallaRegistroScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var nombreApellidos by remember { mutableStateOf("") }
    var nombreUsuario by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPass  by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            LazyColumn(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                item {
                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Registrarse",
                        color = Color.White,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(20.dp))

                    CampoTexto(
                        label = "Nombre y Apellidos",
                        valor = nombreApellidos,
                        onChange = { nombreApellidos = it }
                    )
                    Spacer(Modifier.height(5.dp))

                    CampoTexto(
                        label = "Nombre de Usuario",
                        valor = nombreUsuario,
                        onChange = { nombreUsuario = it }
                    )
                    Spacer(Modifier.height(5.dp))

                    CampoTexto(
                        label = "Correo Electrónico",
                        valor = email,
                        onChange = { email = it },
                        keyboardType = KeyboardType.Email
                    )
                    Spacer(Modifier.height(5.dp))

                    CampoPassword(
                        label = "Contraseña",
                        valor = password,
                        onChange = { password = it }
                    )
                    Spacer(Modifier.height(5.dp))

                    CampoPassword(
                        label = "Confirmar Contraseña",
                        valor = confirmarPass,
                        onChange = { confirmarPass = it }
                    )
                    Spacer(Modifier.height(10.dp))

                    BotonRegistro {
                        // 1) validaciones
                        if (password != confirmarPass ||
                            email.isBlank() ||
                            nombreUsuario.isBlank() ||
                            nombreApellidos.isBlank()
                        ) {
                            Toast.makeText(context,
                                "Completa todos los campos y verifica la contraseña",
                                Toast.LENGTH_SHORT).show()
                            return@BotonRegistro
                        }

                        if (nombreUsuario.length > 15) {
                            Toast.makeText(context,
                                "El nombre de usuario no puede superar los 15 carácteres",
                                Toast.LENGTH_SHORT).show()
                            return@BotonRegistro
                        }

                        if (nombreApellidos.length > 20) {
                            Toast.makeText(context,
                                "El nombre y apellidos no pueden superar los 20 carácteres",
                                Toast.LENGTH_SHORT).show()
                            return@BotonRegistro
                        }

                        if (password.length < 6 || password.length > 20) {
                            Toast.makeText(context,
                                "La contraseña debe tener entre 6 y 20 carácteres",
                                Toast.LENGTH_SHORT).show()
                            return@BotonRegistro
                        }

                        // 2) llamamos al backend
                        coroutineScope.launch {
                            try {
                                val dto = UsuarioRegistro(
                                    email = email,
                                    password = password,
                                    nombreUsuario = nombreUsuario,
                                    nombreApellidos = nombreApellidos,
                                    urlImagenPerfil = "https://ui-avatars.com/api/?name=" + android.net.Uri.encode(nombreUsuario)
                                )
                                val rsp = RetrofitClient.apiService.registrarUsuario(dto)

                                if (rsp.isSuccessful) {
                                    Toast.makeText(context,
                                        "✅ Registro completado",
                                        Toast.LENGTH_SHORT).show()
                                    navController.navigate("login") {
                                        popUpTo("registro") { inclusive = true }
                                    }
                                } else {
                                    Toast.makeText(context,
                                        "❌ Error backend: ${rsp.code()}",
                                        Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Log.e("RegistroApp","Excepción:",e)
                                Toast.makeText(context,
                                    "Error: ${e.message}",
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CampoTexto(
    label: String,
    valor: String,
    onChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Text(label, color = Color.White, fontSize = 14.sp)
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth().height(55.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.Gray,
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.Gray,
            cursorColor = Color.White
        )
    )
}

@Composable
private fun CampoPassword(
    label: String,
    valor: String,
    onChange: (String) -> Unit
) {
    Text(label, color = Color.White, fontSize = 14.sp)
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.Gray,
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.Gray,
            cursorColor = Color.White
        )
    )
}

@Composable
fun BotonRegistro(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White),
        border = BorderStroke(1.dp, Color.White)
    ) {
        Text("Registrarse", fontSize = 16.sp)
    }
}
