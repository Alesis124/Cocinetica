package dam.moviles.cocinetica.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dam.moviles.cocinetica.modelo.CocineticaRepository

class InicioFragmenViewModelFactory(private val repository: CocineticaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InicioFragmenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InicioFragmenViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
