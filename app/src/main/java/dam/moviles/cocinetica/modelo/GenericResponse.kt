package dam.moviles.cocinetica.modelo

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class GenericResponse(
    val mensaje: String?,
    val error: String?,
    val id_comentario: Int? = null
) : Serializable