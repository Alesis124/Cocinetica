package dam.moviles.cocinetica.modelo

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class RecetaResponse(
    val mensaje: String?,
    val error: String?,
    val id_receta: Int?
) : Serializable
