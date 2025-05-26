package dam.moviles.cocinetica.viewModel

import androidx.lifecycle.ViewModel

data class IngredienteUI(
    var cantidad: String = "",
    var unidad: String = "",
    var nombre: String = ""
)

data class PasoUI(
    var descripcion: String = ""
)

class CreaRecetaViewModel : ViewModel() {
    val ingredientes = mutableListOf<IngredienteUI>()
    val pasos = mutableListOf<PasoUI>()

    fun agregarIngrediente() {
        ingredientes.add(IngredienteUI())
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
}
