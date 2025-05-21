package dam.moviles.cocinetica.modelo

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class CocineticaRepository {
    val cocineticaApi: CocineticaApi

    init {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory()) // 👈 Soporte para clases Kotlin
            .build()

        cocineticaApi = Retrofit.Builder()
            .baseUrl("http://83.60.12.188:81/API/crud/")
            .addConverterFactory(MoshiConverterFactory.create(moshi)) // 👈 Usa el Moshi con soporte
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




}
