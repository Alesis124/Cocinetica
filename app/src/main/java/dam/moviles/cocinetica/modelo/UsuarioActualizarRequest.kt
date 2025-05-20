package dam.moviles.cocinetica.modelo

data class UsuarioActualizarRequest(
    val tabla: String = "Usuarios",
    val id_usuario: Int,
    val correo: String,
    val usuario: String,
    val descripcion: String,
    val imagen: String = ""
)