package dam.moviles.cocinetica.modelo

data class Valoracion(
    val id_valoracion: Int,
    val id_usuario: Int,
    val id_receta: Int,
    val valoracion: Int,
    val id_comentario: Int,
    val fecha: String
)
