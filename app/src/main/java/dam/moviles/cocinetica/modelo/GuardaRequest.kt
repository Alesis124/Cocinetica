package dam.moviles.cocinetica.modelo

data class GuardaRequest(
    val tabla: String = "Guarda",
    val id_usuario: Int,
    val id_receta: Int
)
