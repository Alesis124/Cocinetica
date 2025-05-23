package dam.moviles.cocinetica.modelo

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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
    suspend fun insertarGuarda(
        @Body guardaRequest: GuardaRequest
    ): Response<GenericResponse>

    @POST("borrar.php")
    suspend fun eliminarGuarda(@Body guardaRequest: GuardaRequest): Response<GenericResponse>

    @GET("leer.php")
    suspend fun obtenerRecetasGuardadas(
        @Query("tabla") tabla: String = "Guarda",
        @Query("id_usuario") idUsuario: Int
    ): List<Receta>

    @GET("Leer.php")
    suspend fun leerRecetas(
        @Query("tabla") tabla: String = "recetas"
    ): List<Receta>

    @GET("Leer.php")
    suspend fun leerComentarios(
        @Query("tabla") tabla: String = "comentarios"
    ): List<Comentario>

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
    suspend fun getIngredientes(
        @Query("tabla") tabla: String = "ingredientes"
    ): List<Ingrediente>

    @GET("leer.php")
    suspend fun getUMs(
        @Query("tabla") tabla: String = "um"
    ): List<UM>




}