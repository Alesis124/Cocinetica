package dam.moviles.cocinetica.modelo

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Paso(
    val id_paso: Int,
    val id_receta: Int,
    val texto: String
): Serializable