package dam.moviles.cocinetica.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GuardadosFragmentViewModel : ViewModel() {

    private val _enVistaGrid = MutableLiveData(false)
    val enVistaGrid: LiveData<Boolean> = _enVistaGrid

    fun toggleVista() {
        _enVistaGrid.value = !(_enVistaGrid.value ?: false)
    }
}
