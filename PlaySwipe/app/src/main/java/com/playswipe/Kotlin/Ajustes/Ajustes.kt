package com.playswipe.Kotlin.Ajustes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.playswipe.R
import com.playswipe.utils.Refuerzo
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit


@Preview(showBackground = true)
@Composable
fun AjustesScreenPreview() {
    AjustesScreen(rememberNavController())
}


@Composable
fun AjustesScreen(navController: NavController) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        bottomBar = {
            Refuerzo.BarraNavegacionInferior(navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Columna centrada verticalmente sin scroll
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                TituloAjustes()

                Spacer(modifier = Modifier.height(60.dp))

                BotonCambiarNombre(navController)

                Spacer(modifier = Modifier.height(30.dp))

                BotonCambiarContrasena(navController)

                Spacer(modifier = Modifier.height(30.dp))

                BotonPerfil(navController)

                Spacer(modifier = Modifier.height(30.dp))

                BotonCerrarSesion(navController)
            }
        }
    }
}

@Composable
fun TituloAjustes() {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ajustes),
            contentDescription = "Icono Ajustes",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(70.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = "Ajustes",
            color = Color.White,
            fontSize = 50.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BotonCambiarNombre(navController: NavController) {
    Button(
        onClick = { navController.navigate("CambiarNombre") },
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White
        ),
        border = BorderStroke(1.dp, Color.White)
    ) {
        Text("Cambiar Nombre de Usuario", fontSize = 16.sp)
    }
}

@Composable
fun BotonCambiarContrasena(navController: NavController) {
    Button(
        onClick = { navController.navigate("CambiarContraseña") },
        modifier = Modifier
            .fillMaxWidth(0.8f)
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

@Composable
fun BotonPerfil(navController: NavController) {
    Button(
        onClick = { navController.navigate("Perfil") },
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White
        ),
        border = BorderStroke(1.dp, Color.White)
    ) {
        Text("Perfil", fontSize = 16.sp)
    }
}

@Composable
fun BotonCerrarSesion(navController: NavController) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("datos_usuario", Context.MODE_PRIVATE)

    Button(
        onClick = {
            sharedPref.edit() {
                remove("id_usuario")
                    .remove("nombre_usuario")
                    .remove("urlImagenPerfil")
            }
            navController.navigate("login") {
                popUpTo("Ajustes") { inclusive = true }  // Evita que el usuario vuelva atrás
            }
        },
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White
        ),
        border = BorderStroke(1.dp, Color.White)
    ) {
        Text("Cerrar Sesión", fontSize = 16.sp)
    }
}