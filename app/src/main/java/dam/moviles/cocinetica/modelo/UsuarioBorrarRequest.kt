package dam.moviles.cocinetica.modelo

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class UsuarioBorrarRequest(
    val tabla: String = "Usuarios",
    val id_usuario: Int
): Serializable

