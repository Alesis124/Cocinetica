package dam.moviles.cocinetica.modelo

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class ComentarioRequest(
    val tabla: String = "Comentarios",
    val texto: String,
    val id_usuario: Int,
    val id_receta: Int
): Serializable