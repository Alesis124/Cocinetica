package dam.moviles.cocinetica.modelo

data class UsuarioBorrarRequest(
    val tabla: String = "Usuarios",
    val id_usuario: Int
)

