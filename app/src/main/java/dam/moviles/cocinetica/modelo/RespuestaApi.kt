package dam.moviles.cocinetica.modelo

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class RespuestaApi(
    val error: String? = null,
    val mensaje: String? = null
):Serializable
