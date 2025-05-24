package dam.moviles.cocinetica.modelo

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Ingrediente(
    val id_ingrediente: Int,
    val nombre: String
):Serializable
