package dam.moviles.cocinetica.modelo

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Receta(
    val id_receta: Int,
    val nombre: String,
    val duracion: Int,
    val valoracion: String,
    val imagen: String,
    val id_usuario: Int,
    val usuario: String
): Serializable
