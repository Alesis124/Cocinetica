package dam.moviles.cocinetica.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dam.moviles.cocinetica.modelo.CocineticaRepository
import dam.moviles.cocinetica.modelo.Ingrediente
import dam.moviles.cocinetica.modelo.UM
import dam.moviles.cocinetica.modelo.Usuario
import kotlinx.coroutines.launch

data class IngredienteUI(
    var cantidad: String = "",
    var unidad: String = "",
    var nombre: String = ""
)

data class PasoUI(
    var descripcion: String = ""
)

class CreaRecetaViewModel : ViewModel() {

    val repository = CocineticaRepository()

    val ingredientes = mutableListOf<IngredienteUI>()
    val pasos = mutableListOf<PasoUI>()

    var ingredientesDisponibles: List<Ingrediente> = emptyList()
    var unidadesDisponibles: List<UM> = emptyList()

    private val _usuario = MutableLiveData<Usuario?>()
    val usuario: LiveData<Usuario?> get() = _usuario

    fun agregarIngrediente() {
        ingredientes.add(IngredienteUI())
        println("DEBUG: Ingredientes ahora: ${ingredientes.size}")
    }


    fun eliminarIngrediente(index: Int) {
        if (index in ingredientes.indices) ingredientes.removeAt(index)
    }

    fun agregarPaso() {
        pasos.add(PasoUI())
    }

    fun eliminarPaso(index: Int) {
        if (index in pasos.indices) pasos.removeAt(index)
    }

    fun obtenerIdIngredientePorNombre(nombre: String): Int? {
        return ingredientesDisponibles.find { it.nombre.equals(nombre.trim(), ignoreCase = true) }?.id_ingrediente
    }

    fun obtenerIdUMPorNombre(nombre: String): Int? {
        return unidadesDisponibles.find { it.medida.equals(nombre.trim(), ignoreCase = true) }?.id_um
    }

    fun cargarDatosDisponibles() {
        viewModelScope.launch {
            try {
                ingredientesDisponibles = repository.obtenerIngredientes()
                unidadesDisponibles = repository.obtenerUM()
                println("DEBUG ingredientesDisponibles cargados: $ingredientesDisponibles")
                println("DEBUG unidadesDisponibles cargados: $unidadesDisponibles")
            } catch (e: Exception) {
                println("Error al cargar datos disponibles: ${e.message}")
            }
        }
    }


    fun cargarUsuarioDesdeFirebase() {
        val email = FirebaseAuth.getInstance().currentUser?.email
        if (email == null) {
            // No hay usuario logueado
            _usuario.value = null
            return
        }

        viewModelScope.launch {
            try {
                val usuarioBD = repository.consultaUsuarioPorCorreo(email)
                _usuario.value = usuarioBD
            } catch (e: Exception) {
                // Manejar error de consulta
                _usuario.value = null
            }
        }
    }

    fun setUsuario(usuario: Usuario) {
        _usuario.value = usuario
    }

}
