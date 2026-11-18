package com.playswipe

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.playswipe.Amigos.AnadirAmigosScreen
import com.playswipe.Kotlin.Ajustes.AjustesScreen
import com.playswipe.Kotlin.AnadirCanciones.AnadirCancionesScreen
import com.playswipe.Kotlin.Buscador.BuscadorScreen
import com.playswipe.Kotlin.CambiarContrasena.CambiarContrasenaScreen
import com.playswipe.Kotlin.CambiarNombre.CambiarNombreScreen
import com.playswipe.Kotlin.ConexionesBack.RetrofitClient
import com.playswipe.Kotlin.InicioSesion.PantallaLoginScreen
import com.playswipe.Kotlin.Perfil.PerfilScreen
import com.playswipe.Kotlin.Perfil_Amigos.PerfilAmigosScreen
import com.playswipe.Kotlin.Playlist.PlayListScreen
import com.playswipe.Kotlin.Registro.PantallaRegistroScreen
import com.playswipe.Kotlin.Sesion
import com.playswipe.Reproductor.PantallaPrincipal
import com.playswipe.ui.theme.PlaySwipeTheme

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        FirebaseAppCheck.getInstance()
            .installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())

        // Sesión
        val prefs = getSharedPreferences("datos_usuario", Context.MODE_PRIVATE)
        Sesion.userId = prefs.getString("id_usuario", null)

        val api = RetrofitClient.apiService
        val startDest = if (Sesion.userId != null) "pagina_principal" else "login"

        setContent {
            PlaySwipeTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = startDest) {

                    composable("login") {
                        PantallaLoginScreen(navController)
                    }
                    composable("registro") {
                        PantallaRegistroScreen(navController)
                    }
                    composable("CambiarNombre") {
                        CambiarNombreScreen(navController)
                    }
                    composable("CambiarContraseña") {
                        CambiarContrasenaScreen(navController)
                    }
                    composable("Perfil") {
                        PerfilScreen(navController)
                    }
                    composable("Ajustes") {
                        AjustesScreen(navController)
                    }
                    composable("pagina_principal") {
                        PantallaPrincipal(navController)
                    }
                    composable("logoprincipal") {
                        PantallaPrincipal(navController)
                    }
                    composable("añadircanciones") {
                        AnadirCancionesScreen(navController)
                    }
                    composable("buscar") {
                        BuscadorScreen(navController)
                    }
                    composable("playList") {
                        PlayListScreen(navController)
                    }

                    // Pantalla de amigos
                    composable(
                        route = "Amigos/{userId}",
                        arguments = listOf(navArgument("userId") {
                            type = NavType.StringType
                            nullable = false
                        })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments!!.getString("userId")!!
                        AnadirAmigosScreen(
                            navController = navController,
                            api = api,
                            currentUserId = userId
                        )
                    }

                    // Ruta para el perfil del amigo
                    composable(
                        route = "perfilAmigo/{idAmigo}/{nombreAmigo}/{urlImagen}",
                        arguments = listOf(
                            navArgument("idAmigo") { type = NavType.StringType },
                            navArgument("nombreAmigo") { type = NavType.StringType },
                            navArgument("urlImagen") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val idAmigo = backStackEntry.arguments?.getString("idAmigo") ?: ""
                        val nombreAmigo = backStackEntry.arguments?.getString("nombreAmigo") ?: ""
                        val urlImagen = backStackEntry.arguments?.getString("urlImagen") ?: ""
                        PerfilAmigosScreen(
                            navController = navController,
                            idAmigo = idAmigo,
                            nombreAmigo = nombreAmigo,
                            urlImagen = urlImagen
                        )
                    }
                }
            }
        }

        ocultarUI()
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun ocultarUI() {
        window.decorView.post {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.apply {
                    hide(WindowInsets.Type.systemBars())
                    systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            }
        }
    }
}
