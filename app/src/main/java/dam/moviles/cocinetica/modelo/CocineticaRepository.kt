package dam.moviles.cocinetica.modelo

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.moshi.MoshiConverterFactory

class CocineticaRepository {
    val cocineticaApi: CocineticaApi

    init {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val moshiConverterFactory = MoshiConverterFactory.create(moshi).asLenient()

        cocineticaApi = Retrofit.Builder()
            .baseUrl("http://83.60.12.188:81/API/crud/")
            .client(client)
            .addConverterFactory(moshiConverterFactory)
            .build()
            .create(CocineticaApi::class.java)
    }


    suspend fun consultaTodosUsuarios(): List<Usuario> = cocineticaApi.consultaTodosUsuarios()

    suspend fun consultaUsuario(id_usuario: Int): Usuario =
        cocineticaApi.consultaUsuario("Usuarios", id_usuario)

    suspend fun consultarUsuarios(query: String?) =
        if (query == null) {
            consultaTodosUsuarios()
        } else {
            consultaTodosUsuarios()
                .filter { usuario -> usuario.correo.contains(query, ignoreCase = true) }
                .toList()
        }

    suspend fun insertarUsuario(usuario: UsuarioInsertar): Response<Unit> {
        return cocineticaApi.insertarUsuario(usuario)
    }

    suspend fun actualizarUsuario(usuario: Usuario): Response<MensajeRespuesta> {
        val request = UsuarioActualizarRequest(
            id_usuario = usuario.id_usuario,
            correo = usuario.correo,
            usuario = usuario.usuario,
            descripcion = usuario.descripcion,
            imagen = usuario.imagen ?: ""
        )
        return cocineticaApi.actualizarUsuario(request)
    }

    suspend fun eliminarUsuario(idUsuario: Int): Response<MensajeRespuesta> {
        val request = UsuarioBorrarRequest(id_usuario = idUsuario)
        return cocineticaApi.eliminarUsuario(request)
    }


    suspend fun consultaTodasRecetas(): List<Receta> = cocineticaApi.consultaTodasRecetas()

    suspend fun consultaUsuarioPorCorreo(correo: String): Usuario {
        return cocineticaApi.consultaUsuarioPorCorreo(correo = correo)
    }

    suspend fun agregarRecetaGuardada(idUsuario: Int, idReceta: Int): Boolean {
        val request = GuardaRequest(id_usuario = idUsuario, id_receta = idReceta)
        val response = cocineticaApi.insertarGuarda(request)
        return response.isSuccessful && response.body()?.mensaje == "Insertado correctamente"
    }

    suspend fun eliminarRecetaGuardada(idUsuario: Int, idReceta: Int): Boolean {
        val request = GuardaRequest(id_usuario = idUsuario, id_receta = idReceta)
        val response = cocineticaApi.eliminarGuarda(request)
        return response.isSuccessful && response.body()?.mensaje == "Borrado correctamente"
    }


    suspend fun obtenerRecetasGuardadas(idUsuario: Int): List<Receta> {
        return cocineticaApi.obtenerRecetasGuardadas(idUsuario = idUsuario)
    }



    suspend fun obtenerRecetasUsuario(idUsuario: Int): List<Receta> {
        return cocineticaApi.leerRecetas()
            .filter { it.id_usuario == idUsuario }
    }

    suspend fun obtenerComentariosUsuario(idUsuario: Int): List<Comentario> {
        return cocineticaApi.leerComentarios()
            .filter { it.id_usuario == idUsuario }
    }


    suspend fun consultaRecetaPorId(idReceta: Int): Receta {
        return cocineticaApi.consultaReceta(id_receta = idReceta)
    }

    suspend fun consultaIngredientes(idReceta: Int): List<Ingrediente> {
        return cocineticaApi.consultaIngredientes(id_receta = idReceta)
    }
    suspend fun consultaPasos(idReceta: Int): List<Paso> {
        return cocineticaApi.consultaPasos(id_receta = idReceta)
    }

    // Obtener lista completa de ingredientes
    suspend fun obtenerIngredientes(): List<Ingrediente> {
        return cocineticaApi.getIngredientes()
    }

    // Obtener lista de unidades de medida
    suspend fun obtenerUM(): List<UM> {
        return cocineticaApi.getUMs()
    }

    // Obtener pasos de una receta
    suspend fun obtenerContienePorReceta(idReceta: Int): List<Contiene> {
        return cocineticaApi.getContienePorReceta(idReceta = idReceta)
    }

    suspend fun obtenerPasosPorReceta(idReceta: Int): List<Paso> {
        return cocineticaApi.getPasosPorReceta(idReceta = idReceta)
    }

    suspend fun leerValoraciones(): List<Valoracion> {
        return cocineticaApi.leerValoraciones()
    }

    suspend fun obtenerComentariosPorReceta(idReceta: Int): List<Comentario> {
        return cocineticaApi.leerComentarios(idReceta)
    }


    suspend fun obtenerValoracionesComentarios(idReceta: Int): Map<Int, Valoracion?> {
        // Obtenemos todas las valoraciones y las filtramos por receta
        val valoraciones = cocineticaApi.leerValoraciones()
            .filter { it.id_receta == idReceta }

        // Map<id_comentario, Valoracion>
        return valoraciones.associateBy { it.id_comentario }
    }

    suspend fun obtenerUsuarios(): List<Usuario> {
        return cocineticaApi.consultaTodosUsuarios()
    }




}
