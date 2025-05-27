package dam.moviles.cocinetica.viewModel

import androidx.lifecycle.ViewModel

class CompletadoViewModel : ViewModel() {
    var idReceta: Int = 0
    var origen: String = "completado"

    fun setArgs(idReceta: Int) {
        this.idReceta = idReceta
    }
}