package dam.moviles.cocinetica.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import dam.moviles.cocinetica.modelo.CocineticaRepository
import dam.moviles.cocinetica.modelo.Usuario
import dam.moviles.cocinetica.modelo.Receta
import dam.moviles.cocinetica.modelo.Comentario
import kotlinx.coroutines.launch

class CuentaViewModel : ViewModel() {

    private val repository = CocineticaRepository()

    private val _usuario = MutableLiveData<Usuario>()
    val usuario: LiveData<Usuario> = _usuario

    private val _recetas = MutableLiveData<List<Receta>>()
    val recetas: LiveData<List<Receta>> = _recetas

    private val _comentarios = MutableLiveData<List<Comentario>>()
    val comentarios: LiveData<List<Comentario>> = _comentarios

    fun cargarUsuarioYContenido() {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: return

        viewModelScope.launch {
            try {
                val usuario = repository.consultaUsuarioPorCorreo(email)
                _usuario.value = usuario

                val recetasUsuario = repository.obtenerRecetasUsuario(usuario.id_usuario)
                _recetas.value = recetasUsuario

                val comentariosUsuario = repository.obtenerComentariosUsuario(usuario.id_usuario)
                _comentarios.value = comentariosUsuario

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }




}
