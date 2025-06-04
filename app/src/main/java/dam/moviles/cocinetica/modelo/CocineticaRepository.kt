package dam.moviles.cocinetica.modelo

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Response
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
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
            .baseUrl("https://api.alesismedia.es/API/crud/")
            .client(client)
            .addConverterFactory(moshiConverterFactory)
            .build()
            .create(CocineticaApi::class.java)
    }

    suspend fun consultaTodosUsuarios(): List<Usuario> = cocineticaApi.consultaTodosUsuarios()

    suspend fun consultaUsuario(id_usuario: Int): Usuario =
        cocineticaApi.consultaUsuario("Usuarios", id_usuario)

    suspend fun consultarUsuarios(query: String?) =
        if (query == null) consultaTodosUsuarios()
        else consultaTodosUsuarios().filter { it.correo.contains(query, ignoreCase = true) }

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
        return cocineticaApi.obtenerRecetasUsuario(idUsuario = idUsuario)
    }

    suspend fun obtenerComentariosUsuario(idUsuario: Int): List<Comentario> {
        return cocineticaApi.leerComentarios().filter { it.id_usuario == idUsuario }
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

    suspend fun obtenerIngredientes(): List<Ingrediente> = cocineticaApi.getIngredientes()
    suspend fun obtenerUM(): List<UM> = cocineticaApi.getUMs()
    suspend fun obtenerContienePorReceta(idReceta: Int): List<Contiene> = cocineticaApi.getContienePorReceta("contiene", idReceta)
    suspend fun obtenerPasosPorReceta(idReceta: Int): List<Paso> = cocineticaApi.getPasosPorReceta("pasos", idReceta)
    suspend fun leerValoraciones(): List<Valoracion> = cocineticaApi.leerValoraciones()
    suspend fun obtenerComentariosPorReceta(idReceta: Int): List<Comentario> = cocineticaApi.leerComentarios("comentarios", idReceta)

    suspend fun obtenerValoracionesComentarios(idReceta: Int): Map<Int, Int> {
        val comentarios = obtenerComentariosPorReceta(idReceta)
        val valoraciones = leerValoraciones()

        return comentarios.mapNotNull { comentario ->
            valoraciones.find {
                it.id_usuario == comentario.id_usuario && it.id_receta == comentario.id_receta
            }?.let { comentario.id_comentario to it.valoracion }
        }.toMap()
    }



    suspend fun obtenerUsuarios(): List<Usuario> = cocineticaApi.consultaTodosUsuarios()

    suspend fun insertarComentario(comentarioRequest: ComentarioRequest): Response<GenericResponse> {
        return cocineticaApi.insertarComentario(comentarioRequest)
    }

    suspend fun eliminarComentario(idComentario: Int): Response<RespuestaApi> {
        val json = JSONObject()
        json.put("tabla", "Comentarios")
        json.put("id_comentario", idComentario)

        val body = json.toString().toRequestBody("application/json".toMediaType())
        return cocineticaApi.eliminarComentario(body)
    }

    suspend fun insertarOActualizarValoracionExistente(
        idUsuario: Int,
        idReceta: Int,
        valoracionInt: Int
    ): Boolean {
        val todasValoraciones = leerValoraciones()
        val existente = todasValoraciones.find { it.id_usuario == idUsuario && it.id_receta == idReceta }

        return if (existente != null) {

            val json = JSONObject().apply {
                put("tabla", "Valoraciones")
                put("id_valoracion", existente.id_valoracion)
                put("id_usuario", idUsuario)
                put("id_receta", idReceta)
                put("valoracion", valoracionInt)
            }

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val response = cocineticaApi.actualizarValoracion(body)
            response.isSuccessful
        } else {

            val json = JSONObject().apply {
                put("tabla", "Valoraciones")
                put("id_usuario", idUsuario)
                put("id_receta", idReceta)
                put("valoracion", valoracionInt)
            }

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val response = cocineticaApi.insertarGenerico(body)
            response.isSuccessful
        }
    }
    suspend fun actualizarMediaValoracionDeReceta(idReceta: Int): Boolean {
        return try {
            val valoraciones = cocineticaApi.leerValoraciones("Valoraciones")
                .filter { it.id_receta == idReceta }

            if (valoraciones.isNotEmpty()) {
                val media = valoraciones.map { it.valoracion }.average()


                val recetaOriginal = cocineticaApi.consultaReceta("Recetas", idReceta)


                println("DEBUG - Imagen original: ${recetaOriginal.imagen?.take(10)}...")

                val json = JSONObject().apply {
                    put("tabla", "Recetas")
                    put("id_receta", idReceta)
                    put("valoracion", media)

                }

                val body = json.toString().toRequestBody("application/json".toMediaType())
                val response = cocineticaApi.actualizarValoracion(body)

                response.isSuccessful && response.body()?.error == null
            } else {
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun buscarRecetas(texto: String): List<Receta> {
        return cocineticaApi.buscarRecetas("recetas", texto)
    }

    suspend fun insertarRecetaCompleta(
        recetaBase: Receta,
        ingredientes: List<Contiene>,
        pasos: List<String>
    ): Int? {
        val recetaJson = JSONObject().apply {
            put("tabla", "Recetas")
            put("nombre", recetaBase.nombre)
            put("duracion", recetaBase.duracion)
            put("valoracion", recetaBase.valoracion)
            put("imagen", recetaBase.imagen)
            put("id_usuario", recetaBase.id_usuario)
        }
        val body = recetaJson.toString().toRequestBody("application/json".toMediaType())

        val response = cocineticaApi.insertarGenerico(body)

        if (!response.isSuccessful) return null

        val RecetaResponse = response.body() ?: return null



        val idReceta = RecetaResponse.id_receta ?: return null


        for (contiene in ingredientes) {
            val ingJson = JSONObject().apply {
                put("tabla", "Contiene")
                put("id_receta", idReceta)
                put("id_ingrediente", contiene.id_ingrediente)
                put("id_um", contiene.id_um)
                put("cantidad", contiene.cantidad)
            }
            val ingBody = ingJson.toString().toRequestBody("application/json".toMediaType())
            cocineticaApi.insertarGenerico(ingBody)
        }


        for (textoPaso in pasos) {
            val pasoJson = JSONObject().apply {
                put("tabla", "Pasos")
                put("id_receta", idReceta)
                put("texto", textoPaso)
            }
            val pasoBody = pasoJson.toString().toRequestBody("application/json".toMediaType())
            cocineticaApi.insertarGenerico(pasoBody)
        }

        return idReceta
    }


    suspend fun obtenerOInsertarIngrediente(nombre: String): Int {
        val nombreLimpio = nombre.trim()


        val ingredientes = cocineticaApi.getIngredientes()
        val existente = ingredientes.find { it.nombre.equals(nombreLimpio, ignoreCase = true) }


        if (existente != null) return existente.id_ingrediente


        val json = JSONObject().apply {
            put("tabla", "Ingredientes")
            put("nombre", nombreLimpio)
        }
        val body = json.toString().toRequestBody("application/json".toMediaType())
        val response = cocineticaApi.insertarGenerico(body)

        if (!response.isSuccessful) throw Exception("Error insertando ingrediente: $nombreLimpio")


        val actualizados = cocineticaApi.getIngredientes()
        return actualizados.find { it.nombre.equals(nombreLimpio, ignoreCase = true) }?.id_ingrediente
            ?: throw Exception("Ingrediente insertado pero no encontrado: $nombreLimpio")
    }

    suspend fun eliminarReceta(idReceta: Int): Boolean {
        val json = JSONObject().apply {
            put("tabla", "Recetas")
            put("id_receta", idReceta)
        }
        val body = json.toString().toRequestBody("application/json".toMediaType())
        val response = cocineticaApi.eliminarReceta(body)
        return response.isSuccessful && response.body()?.mensaje == "Borrado correctamente"
    }








}
