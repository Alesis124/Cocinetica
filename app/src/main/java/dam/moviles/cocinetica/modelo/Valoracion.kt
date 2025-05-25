package dam.moviles.cocinetica.modelo

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Valoracion(
    val id_valoracion: Int,
    val id_usuario: Int,
    val id_receta: Int,
    val valoracion: Int,
    val id_comentario: Int?
):Serializable
