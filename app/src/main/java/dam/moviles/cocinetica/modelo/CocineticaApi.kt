package dam.moviles.cocinetica.modelo

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface CocineticaApi {

    @GET("leer.php")
    suspend fun consultaTodosUsuarios(@Query("tabla") tabla: String = "Usuarios"): List<Usuario>

    @GET("leer.php")
    suspend fun consultaUsuario(
        @Query("tabla") tabla: String = "Usuarios",
        @Query("id_usuario") id_usuario: Int
    ): Usuario

    @POST("insertar.php")
    suspend fun insertarUsuario(@Body usuario: UsuarioInsertar): Response<Unit>

    @POST("actualizar.php")
    suspend fun actualizarUsuario(@Body usuario: UsuarioActualizarRequest): Response<MensajeRespuesta>

    @POST("borrar.php")
    suspend fun eliminarUsuario(@Body request: UsuarioBorrarRequest): Response<MensajeRespuesta>

    @GET("leer.php")
    suspend fun consultaTodasRecetas(@Query("tabla") tabla: String = "Recetas"): List<Receta>

    @GET("leer.php")
    suspend fun consultaUsuarioPorCorreo(
        @Query("tabla") tabla: String = "Usuarios",
        @Query("correo") correo: String
    ): Usuario

    @POST("insertar.php")
    suspend fun insertarGuarda(@Body guardaRequest: GuardaRequest): Response<GenericResponse>

    @POST("borrar.php")
    suspend fun eliminarGuarda(@Body guardaRequest: GuardaRequest): Response<GenericResponse>

    @GET("leer.php")
    suspend fun obtenerRecetasGuardadas(
        @Query("tabla") tabla: String = "Guarda",
        @Query("id_usuario") idUsuario: Int
    ): List<Receta>

    @GET("leer.php")
    suspend fun leerRecetas(@Query("tabla") tabla: String = "recetas"): List<Receta>

    @GET("leer.php")
    suspend fun leerComentarios(@Query("tabla") tabla: String = "comentarios"): List<Comentario>

    @GET("leer.php")
    suspend fun consultaReceta(
        @Query("tabla") tabla: String = "Recetas",
        @Query("id_receta") id_receta: Int
    ): Receta

    @GET("leer.php")
    suspend fun consultaIngredientes(
        @Query("tabla") tabla: String = "Ingredientes",
        @Query("id_receta") id_receta: Int
    ): List<Ingrediente>

    @GET("leer.php")
    suspend fun consultaPasos(
        @Query("tabla") tabla: String = "Pasos",
        @Query("id_receta") id_receta: Int
    ): List<Paso>

    @GET("leer.php")
    suspend fun getContienePorReceta(
        @Query("tabla") tabla: String = "contiene",
        @Query("id_receta") idReceta: Int
    ): List<Contiene>

    @GET("leer.php")
    suspend fun getPasosPorReceta(
        @Query("tabla") tabla: String = "pasos",
        @Query("id_receta") idReceta: Int
    ): List<Paso>

    @GET("leer.php")
    suspend fun getIngredientes(@Query("tabla") tabla: String = "ingredientes"): List<Ingrediente>

    @GET("leer.php")
    suspend fun getUMs(@Query("tabla") tabla: String = "um"): List<UM>

    @GET("leer.php")
    suspend fun leerValoraciones(@Query("tabla") tabla: String = "Valoraciones"): List<Valoracion>

    @GET("leer.php")
    suspend fun leerComentarios(@Query("tabla") tabla: String = "comentarios", @Query("id_receta") idReceta: Int): List<Comentario>

    @GET("leer.php")
    suspend fun obtenerComentariosPorReceta(
        @Query("tabla") tabla: String = "Comentarios",
        @Query("id_receta") idReceta: Int
    ): List<Comentario>

    @POST("insertar.php")
    suspend fun insertarComentario(@Body comentarioRequest: ComentarioRequest): Response<GenericResponse>

    @POST("borrar.php")
    suspend fun eliminarComentario(@Body cuerpo: RequestBody): Response<RespuestaApi>

    @POST("actualizar.php")
    suspend fun actualizarValoracion(@Body body: RequestBody): Response<GenericResponse>

    @POST("insertar.php")
    suspend fun insertarGenerico(@Body body: RequestBody): Response<GenericResponse>
}
