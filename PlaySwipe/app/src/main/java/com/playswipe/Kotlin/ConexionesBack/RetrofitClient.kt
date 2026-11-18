package com.playswipe.Kotlin.ConexionesBack

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Dirección del backend. 10.0.2.2 es el propio PC visto desde el emulador de Android;
    // para un móvil real o un servidor, pon aquí su IP o dominio.
    private const val BASE_URL = "http://10.0.2.2:8080/"
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
