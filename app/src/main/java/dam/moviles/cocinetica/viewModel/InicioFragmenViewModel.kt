package dam.moviles.cocinetica.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dam.moviles.cocinetica.modelo.CocineticaRepository
import kotlinx.coroutines.launch

class InicioFragmenViewModel(private val repo: CocineticaRepository) : ViewModel() {

    private val _enVistaGrid = MutableLiveData(false)
    val enVistaGrid: LiveData<Boolean> = _enVistaGrid

    fun toggleVista() {
        _enVistaGrid.value = !(_enVistaGrid.value ?: false)
    }

    fun setVistaGrid(valor: Boolean) {
        _enVistaGrid.value = valor
    }

    fun guardarReceta(idUsuario: Int, idReceta: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val exito = repo.agregarRecetaGuardada(idUsuario, idReceta)
            onResult(exito)
        }
    }
}
