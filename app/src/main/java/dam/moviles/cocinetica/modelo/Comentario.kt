package dam.moviles.cocinetica.modelo

import com.squareup.moshi.JsonClass
import java.io.Serializable
import java.util.Date

@JsonClass(generateAdapter = true)
data class Comentario(
    val id_comentario: Int,
    val texto: String,
    val fecha: String,
    val id_usuario: Int,
    val id_receta: Int
): Serializable

