package dam.moviles.cocinetica.modelo

data class Receta(
    val id_receta: Int,
    val nombre: String,
    val duracion: Int,
    val valoracion: String,
    val imagen: String,
    val id_usuario: Int,
    val usuario: String
)
