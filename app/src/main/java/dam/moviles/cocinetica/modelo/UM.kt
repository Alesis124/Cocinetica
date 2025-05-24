package dam.moviles.cocinetica.modelo

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class UM(
    val id_um: Int,
    val medida: String
):Serializable
