package dam.moviles.cocinetica.viewModel

import androidx.lifecycle.ViewModel
import dam.moviles.cocinetica.modelo.CocineticaRepository
import dam.moviles.cocinetica.modelo.UsuarioInsertar
import retrofit2.Response

class RegisterFragmentViewModel : ViewModel() {
    private val repository = CocineticaRepository()

    suspend fun insertarUsuario(usuario: UsuarioInsertar): Response<Unit> {
        return repository.insertarUsuario(usuario)
    }
}
