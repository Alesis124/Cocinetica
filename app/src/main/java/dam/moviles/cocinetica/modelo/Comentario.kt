package dam.moviles.cocinetica.modelo

import java.util.Date

data class Comentario(
    val id_comentario: Int,
    val texto: String,
    val fecha: String,
    val id_usuario: Int,
    val id_receta: Int
)

