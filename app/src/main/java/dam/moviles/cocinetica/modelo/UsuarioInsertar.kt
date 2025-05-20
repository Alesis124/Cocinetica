package dam.moviles.cocinetica.modelo

data class UsuarioInsertar(
    val tabla: String = "Usuarios",
    val correo: String,
    val usuario: String,
    val descripcion: String,
    val imagen: String
)
