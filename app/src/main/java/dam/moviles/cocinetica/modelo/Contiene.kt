package dam.moviles.cocinetica.modelo

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Contiene(
    val id_receta: Int,
    val id_ingrediente: Int,
    val id_um: Int,
    val cantidad: Double
): Serializable
