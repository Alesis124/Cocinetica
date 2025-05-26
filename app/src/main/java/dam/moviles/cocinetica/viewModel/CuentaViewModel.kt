package dam.moviles.cocinetica.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import dam.moviles.cocinetica.modelo.CocineticaRepository
import dam.moviles.cocinetica.modelo.Usuario
import dam.moviles.cocinetica.modelo.Receta
import dam.moviles.cocinetica.modelo.Comentario
import dam.moviles.cocinetica.modelo.Valoracion
import kotlinx.coroutines.launch

class CuentaViewModel : ViewModel() {

    private val repository = CocineticaRepository()

    private val _usuario = MutableLiveData<Usuario>()
    val usuario: LiveData<Usuario> = _usuario

    private val _recetas = MutableLiveData<List<Receta>>()
    val recetas: LiveData<List<Receta>> = _recetas

    private val _comentarios = MutableLiveData<List<Comentario>>()
    val comentarios: LiveData<List<Comentario>> = _comentarios

    // Mapa id_comentario -> valoracion
    private val _valoraciones = MutableLiveData<Map<Int, Valoracion>>()
    val valoraciones: LiveData<Map<Int, Valoracion>> = _valoraciones


    fun cargarUsuarioYContenido() {
        val email = FirebaseAuth.getInstance().currentUser?.email
        if (email == null) {
            Log.e("CuentaViewModel", "No hay usuario logueado en Firebase")
            return
        }

        viewModelScope.launch {
            try {
                val usuario = repository.consultaUsuarioPorCorreo(email)
                if (usuario == null) {
                    Log.e("CuentaViewModel", "No se encontró usuario en la API para email: $email")
                    return@launch
                }

                Log.d("CuentaViewModel", "Usuario cargado: $usuario")

                _usuario.value = usuario

                val recetasUsuario = repository.obtenerRecetasUsuario(usuario.id_usuario)
                _recetas.value = recetasUsuario

                val comentariosUsuario = repository.obtenerComentariosUsuario(usuario.id_usuario)
                _comentarios.value = comentariosUsuario

                val todasValoraciones = repository.leerValoraciones()
                val valoracionesMap = comentariosUsuario.mapNotNull { comentario ->
                    val valoracion = todasValoraciones.find {
                        it.id_usuario == comentario.id_usuario && it.id_receta == comentario.id_receta
                    }
                    if (valoracion != null) comentario.id_comentario to valoracion else null
                }.toMap()

                _valoraciones.value = valoracionesMap

            } catch (e: Exception) {
                Log.e("CuentaViewModel", "Error cargando usuario y contenido", e)
            }
        }
    }


    fun eliminarComentario(comentario: Comentario) {
        viewModelScope.launch {
            try {
                repository.eliminarComentario(comentario.id_comentario)
                val idUsuario = _usuario.value?.id_usuario
                if (idUsuario != null) {
                    val comentariosUsuario = repository.obtenerComentariosUsuario(idUsuario)
                    _comentarios.value = comentariosUsuario

                    val todasValoraciones = repository.leerValoraciones()
                    val valoracionesMap = comentariosUsuario.mapNotNull { comentario ->
                        val valoracion = todasValoraciones.find {
                            it.id_usuario == comentario.id_usuario && it.id_receta == comentario.id_receta
                        }
                        if (valoracion != null) comentario.id_comentario to valoracion else null
                    }.toMap()
                    _valoraciones.value = valoracionesMap
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
