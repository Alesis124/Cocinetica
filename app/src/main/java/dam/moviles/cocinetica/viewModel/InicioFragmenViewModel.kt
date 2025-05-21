package dam.moviles.cocinetica.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InicioFragmenViewModel : ViewModel() {

    // LiveData para saber si estamos en vista grid (true) o lista (false)
    private val _enVistaGrid = MutableLiveData(false)
    val enVistaGrid: LiveData<Boolean> = _enVistaGrid

    // Cambiar el estado (toggle)
    fun toggleVista() {
        _enVistaGrid.value = !(_enVistaGrid.value ?: false)
    }

    // Opcional: método para fijar directamente
    fun setVistaGrid(valor: Boolean) {
        _enVistaGrid.value = valor
    }
}
