package dam.moviles.cocinetica.modelo

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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




}