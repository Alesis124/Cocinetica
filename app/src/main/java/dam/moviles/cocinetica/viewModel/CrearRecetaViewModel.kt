package dam.moviles.cocinetica.viewModel

import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dam.moviles.cocinetica.modelo.CocineticaRepository
import dam.moviles.cocinetica.modelo.Ingrediente
import dam.moviles.cocinetica.modelo.IngredienteUI
import dam.moviles.cocinetica.modelo.PasoUI
import dam.moviles.cocinetica.modelo.UM
import dam.moviles.cocinetica.modelo.Usuario
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class CreaRecetaViewModel : ViewModel() {

    val repository = CocineticaRepository()

    val ingredientes = mutableListOf<IngredienteUI>()
    val pasos = mutableListOf<PasoUI>()

    var ingredientesDisponibles: List<Ingrediente> = emptyList()
    var unidadesDisponibles: List<UM> = emptyList()

    private val _usuario = MutableLiveData<Usuario?>()
    val usuario: LiveData<Usuario?> get() = _usuario


    private val _imagenBase64 = MutableLiveData<String?>()
    val imagenBase64: LiveData<String?> get() = _imagenBase64

    private val _imagenUri = MutableLiveData<Uri?>()
    val imagenUri: LiveData<Uri?> get() = _imagenUri

    private val _currentPhotoPath = MutableLiveData<String?>()
    val currentPhotoPath: LiveData<String?> get() = _currentPhotoPath

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
            } catch (e: Exception) {
                println("Error al cargar datos disponibles: ${e.message}")
            }
        }
    }

    fun cargarUsuarioDesdeFirebase() {
        val email = FirebaseAuth.getInstance().currentUser?.email
        if (email == null) {
            _usuario.value = null
            return
        }

        viewModelScope.launch {
            try {
                val usuarioBD = repository.consultaUsuarioPorCorreo(email)
                _usuario.value = usuarioBD
            } catch (e: Exception) {
                _usuario.value = null
            }
        }
    }

    fun setUsuario(usuario: Usuario) {
        _usuario.value = usuario
    }


    fun setImagenBase64(base64: String?) {
        _imagenBase64.value = base64
    }

    fun setImagenUri(uri: Uri?) {
        _imagenUri.value = uri
    }

    fun setCurrentPhotoPath(path: String?) {
        _currentPhotoPath.value = path
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}