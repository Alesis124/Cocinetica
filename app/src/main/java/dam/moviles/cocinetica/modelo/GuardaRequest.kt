package dam.moviles.cocinetica.modelo

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class GuardaRequest(
    val tabla: String = "Guarda",
    val id_usuario: Int,
    val id_receta: Int
):Serializable
