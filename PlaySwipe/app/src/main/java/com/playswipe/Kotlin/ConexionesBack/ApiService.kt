package com.playswipe.Kotlin.ConexionesBack

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*


data class RegistroRespuesta(val mensaje: String)

data class PlaylistInfo(
    val id_playlist: String,
    val id_usuario: String,
    val nombre: String,
    val tipo: String,
    val fecha_creacion: String,
    val imagen_playlist: String?
)


interface ApiService {

    /* ---------- Registro / Login ---------- */
    @POST("api/registro")
    suspend fun registrarUsuario(@Body usuario: UsuarioRegistro): Response<RegistroRespuesta>

    @POST("login")
    suspend fun loginUsuario(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/cambiar-nombre")
    suspend fun cambiarNombreUsuario(@Body body: Map<String, String>): Response<RegistroRespuesta>

    @POST("api/cambiar-contrasena")
    fun cambiarContrasena(@Body datos: Map<String, String>): Call<Map<String, String>>


    /* ---------- Canciones ---------- */
    @POST("api/canciones/nueva")
    suspend fun registrarCancion(@Body cancion: CancionRequest): Response<RegistroRespuesta>

    @GET("api/canciones")
    suspend fun getCanciones(
        @Query("inicio") inicio: Int,
        @Query("cantidad") cantidad: Int = 100
    ): List<CancionRequest>


    /* ---------- Playlist personal ---------- */
    @POST("api/users/{userId}/playlist")
    suspend fun anadirCancionPlaylist(
        @Path("userId") userId: String,
        @Body req: Map<String, Long>
    ): Response<RegistroRespuesta>

    @GET("api/users/{userId}/playlist")
    suspend fun getPlaylist(@Path("userId") userId: String): List<CancionRequest>

    @GET("api/users/{userId}/playlist/info")
    suspend fun getPlaylistInfo(@Path("userId") userId: String): Response<PlaylistInfo>

    @PUT("api/users/{userId}/playlist")
    suspend fun renombrarPlaylist(
        @Path("userId") userId: String,
        @Body body: Map<String, String>
    ): Response<RegistroRespuesta>

    @PUT("api/users/{userId}/playlist/imagen")
    suspend fun actualizarImagenPlaylist(
        @Path("userId") userId: String,
        @Body body: Map<String, String>
    ): Response<PlaylistInfo>

    @DELETE("api/users/{userId}/playlist/cancion/{songId}")
    suspend fun EliminarCancionPlaylist(
        @Path("userId") userId: String,
        @Path("songId") songId: Long
    ): Response<RegistroRespuesta>
    @GET("perfil/usuario/{id_usuario}")
    suspend fun obtenerUsuarioPorId(@Path("id_usuario") idUsuario: String): Response<UsuarioDTO>

    @GET("perfil/favoritas/{idUsuario}")
    suspend fun obtenerCantidadFavoritas(@Path("idUsuario") idUsuario: String): Response<Int>

    @GET("api/users/{userId}/friends/cantidad-amigos")
    suspend fun obtenerCantidadAmigos(@Path("userId") userId: String): Response<Int>


    /* ---------- Amistades ---------- */
    @POST("api/users/{userId}/friends")
    suspend fun sendFriendRequest(
        @Path("userId") userId: String,
        @Body req: SendFriendRequest
    ): Response<RegistroRespuesta>

    @GET("api/users/{userId}/friends/requests")
    suspend fun getFriendRequests(@Path("userId") userId: String): List<AmigoDTO>

    @PUT("api/users/{userId}/friends/requests/{id}")
    suspend fun respondFriendRequest(
        @Path("userId") userId: String,
        @Path("id") requestId: Long,
        @Body req: RespondFriendRequest
    ): Response<RegistroRespuesta>

    @GET("api/users/{userId}/friends")
    suspend fun getFriends(@Path("userId") userId: String): List<UsuarioDTO>


    /* ---------- Otros ---------- */
    @GET("api/users/{userId}/playlist/imagen")
    suspend fun obtenerImagenPlaylist(@Path("userId") userId: String): Response<Map<String, String>>

    @FormUrlEncoded
    @POST("perfil/imagenUrl")
    suspend fun subirUrlImagen(
        @Field("id_usuario") idUsuario: String,
        @Field("urlImagen") urlImagen: String
    ): Response<Void>
}
