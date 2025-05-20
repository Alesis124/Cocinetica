package dam.moviles.cocinetica.modelo

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Usuario(
    val id_usuario: Int,
    val correo: String,
    val usuario: String,
    val descripcion: String,
    val imagen: String
):Serializable
