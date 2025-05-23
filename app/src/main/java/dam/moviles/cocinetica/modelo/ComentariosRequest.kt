package dam.moviles.cocinetica.modelo

data class ComentarioRequest(
    val tabla: String = "Comentarios",
    val texto: String,
    val id_usuario: Int,
    val id_receta: Int
)