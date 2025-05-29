package dam.moviles.cocinetica.viewModel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.modelo.CocineticaRepository
import dam.moviles.cocinetica.modelo.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class AjustesCuentaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CocineticaRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _imagenBase64 = MutableLiveData<String?>()
    val imagenBase64: LiveData<String?> = _imagenBase64

    private val _usuarioNombre = MutableLiveData<String>()
    val usuarioNombre: LiveData<String> = _usuarioNombre

    private val _usuarioDescripcion = MutableLiveData<String>()
    val usuarioDescripcion: LiveData<String> = _usuarioDescripcion

    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?> = _imageUri

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    private val _navigationEvent = MutableLiveData<Event<Int>>()
    val navigationEvent: LiveData<Event<Int>> = _navigationEvent

    private val _glideImageUrl = MutableLiveData<String?>()
    val glideImageUrl: LiveData<String?> = _glideImageUrl

    fun setImageUri(uri: Uri?) {
        _imageUri.value = uri
    }

    fun setNombreUsuario(nombre: String) {
        _usuarioNombre.value = nombre
    }

    fun setDescripcionUsuario(descripcion: String) {
        _usuarioDescripcion.value = descripcion
    }

    fun procesarImagen(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: run {
                    withContext(Dispatchers.Main) {
                        _toastMessage.value = "No se pudo abrir la imagen"
                    }
                    return@launch
                }

                inputStream.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 128, 128, true)
                    val circularBitmap = getCircularBitmap(resizedBitmap)

                    val byteArray = ByteArrayOutputStream().apply {
                        circularBitmap.compress(Bitmap.CompressFormat.JPEG, 80, this)
                    }.toByteArray()

                    _imagenBase64.postValue(Base64.encodeToString(byteArray, Base64.NO_WRAP))
                }
            } catch (e: Exception) {
                Log.e("AjustesCuentaViewModel", "Error al procesar imagen: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _toastMessage.value = "Error al procesar imagen"
                }
            }
        }
    }

    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(bitmap.width / 2f, bitmap.height / 2f, bitmap.width / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    fun cargarDatosUsuario(email: String?) {
        email ?: return

        viewModelScope.launch {
            try {
                val usuario = repository.consultaUsuarioPorCorreo(email)
                _usuarioNombre.postValue(usuario.usuario)
                _usuarioDescripcion.postValue(usuario.descripcion)

                if (usuario.imagen != null) {
                    _imagenBase64.postValue(usuario.imagen)
                } else {
                    auth.currentUser?.photoUrl?.let { url ->
                        _glideImageUrl.postValue(url.toString())
                        loadImageFromUrlAndConvertToBase64(getApplication(), url.toString())
                    }
                }
            } catch (e: Exception) {
                Log.e("AjustesCuentaViewModel", "Error al cargar datos: ${e.localizedMessage}")
                _toastMessage.postValue("Error al cargar datos del usuario")
                _imagenBase64.postValue(null)
                _usuarioNombre.postValue("")
                _usuarioDescripcion.postValue("")
            }
        }
    }

    private fun loadImageFromUrlAndConvertToBase64(context: Context, imageUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bitmap = Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .submit(128, 128)
                    .get()

                val byteArray = ByteArrayOutputStream().apply {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, this)
                }.toByteArray()

                _imagenBase64.postValue(Base64.encodeToString(byteArray, Base64.NO_WRAP))
            } catch (e: Exception) {
                Log.e("AjustesCuentaViewModel", "Error al convertir imagen de URL a Base64", e)
                withContext(Dispatchers.Main) {
                    _toastMessage.value = "Error al procesar imagen de perfil"
                }
            }
        }
    }

    fun actualizarUsuario() {
        val nombre = _usuarioNombre.value ?: ""
        val descripcion = _usuarioDescripcion.value ?: ""
        val email = auth.currentUser?.email ?: run {
            _toastMessage.value = "Usuario no autenticado"
            return
        }

        viewModelScope.launch {
            try {
                val usuarios = repository.consultaTodosUsuarios()
                val usuarioActual = usuarios.find { it.correo.equals(email, ignoreCase = true) }

                if (usuarioActual != null) {
                    val actualizado = usuarioActual.copy(
                        usuario = nombre,
                        descripcion = descripcion,
                        imagen = _imagenBase64.value ?: usuarioActual.imagen
                    )

                    val request = Usuario(
                        id_usuario = actualizado.id_usuario,
                        correo = actualizado.correo,
                        usuario = actualizado.usuario,
                        descripcion = actualizado.descripcion,
                        imagen = actualizado.imagen
                    )

                    val response = repository.actualizarUsuario(request)

                    if (response.isSuccessful) {
                        _toastMessage.postValue("Perfil actualizado con éxito")
                        _navigationEvent.postValue(Event(R.id.action_ajustesCuentaFragment_to_cuentaFragment))
                    } else {
                        Log.e("AjustesCuentaViewModel", "Error al actualizar: ${response.errorBody()?.string()}")
                        _toastMessage.postValue("Error al actualizar perfil")
                    }
                }
            } catch (e: Exception) {
                Log.e("AjustesCuentaViewModel", "Error al actualizar usuario: ${e.localizedMessage}")
                _toastMessage.postValue("Error: ${e.message}")
            }
        }
    }

    fun eliminarCuenta() {
        val currentUser = auth.currentUser ?: run {
            _toastMessage.value = "Usuario no autenticado"
            return
        }
        val email = currentUser.email ?: return

        viewModelScope.launch {
            try {
                val usuarios = repository.consultaTodosUsuarios()
                val usuario = usuarios.find { it.correo.equals(email, ignoreCase = true) }

                if (usuario != null) {
                    val response = repository.eliminarUsuario(usuario.id_usuario)
                    if (response.isSuccessful) {
                        currentUser.delete().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                _toastMessage.postValue("Cuenta eliminada con éxito")
                                _navigationEvent.postValue(Event(R.id.action_ajustesCuentaFragment_to_loginFragment))
                            } else {
                                Log.e("AjustarCuentaViewModel", "Error al eliminar de FirebaseAuth: ${task.exception}")
                                _toastMessage.postValue("Error al eliminar cuenta de autenticación")
                            }
                        }
                    } else {
                        Log.e("AjustarCuentaViewModel", "Error al eliminar de la API: ${response.errorBody()?.string()}")
                        _toastMessage.postValue("Error al eliminar perfil de la base de datos")
                    }
                }
            } catch (e: Exception) {
                Log.e("AjustesCuentaViewModel", "Error al eliminar cuenta: ${e.localizedMessage}")
                _toastMessage.postValue("Error: ${e.message}")
            }
        }
    }

    fun cerrarSesion() {
        auth.signOut()
        _navigationEvent.value = Event(R.id.action_ajustesCuentaFragment_to_loginFragment)
    }

    fun navigateTo(destinationId: Int) {
        _navigationEvent.value = Event(destinationId)
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    fun clearGlideImageUrl() {
        _glideImageUrl.value = null
    }
}

class Event<out T>(private val content: T) {
    var hasBeenHandled = false
        private set

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}