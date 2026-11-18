// src/main/java/com/playswipe/utils/Refuerzo.kt
package com.playswipe.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.playswipe.Kotlin.Sesion
import com.playswipe.R

object Refuerzo {

    @Composable
    fun BarraNavegacionInferior(navController: NavController) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { navController.navigate("añadircanciones") }) {
                Image(
                    painter = painterResource(R.drawable.add),
                    contentDescription = "Añadir",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(32.dp)
                        .scale(1.5f)
                )
            }
            IconButton(onClick = { navController.navigate("buscar") }) {
                Image(
                    painter = painterResource(R.drawable.lupa),
                    contentDescription = "Buscar",
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = { navController.navigate("logoprincipal") }) {
                Image(
                    painter = painterResource(R.drawable.playswipe_logo_sinfondo_sinletras_),
                    contentDescription = "Home",
                    modifier = Modifier.size(60.dp)
                )
            }
            IconButton(onClick = { navController.navigate("playList") }) {
                Image(
                    painter = painterResource(R.drawable.menu),
                    contentDescription = "Playlist",
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = {
                Sesion.userId?.let { uid ->
                    navController.navigate("Amigos/$uid") {
                        launchSingleTop = true
                        restoreState    = true
                    }
                }
            }) {
                Image(
                    painter = painterResource(R.drawable.anadiramigo),
                    contentDescription = "Amigos",
                    modifier = Modifier.size(43.dp)
                )
            }
        }
    }
}
