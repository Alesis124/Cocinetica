package dam.moviles.cocinetica.vista

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentAjustesCuentaBinding
import dam.moviles.cocinetica.modelo.CocineticaRepository
import dam.moviles.cocinetica.modelo.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date

class AjustesCuentaFragment : Fragment() {

    private lateinit var binding: FragmentAjustesCuentaBinding
    private val repository = CocineticaRepository()
    private val args: AjustesCuentaFragmentArgs by navArgs()

    private var imageUri: Uri? = null
    private var imagenBase64: String? = null
    private var currentPhotoPath: String? = null

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && imageUri != null) {
            procesarImagen(imageUri!!)
        } else {
            Toast.makeText(requireContext(), "Error al tomar la foto", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            procesarImagen(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        inicializarBinding()
        cargarDatosUsuario()
        inicializarBotones()

        return binding.root
    }

    private fun inicializarBinding() {
        binding = FragmentAjustesCuentaBinding.inflate(layoutInflater)
    }

    private fun mostrarDialogoImagen() {
        val opciones = arrayOf("Cámara", "Galería")
        AlertDialog.Builder(requireContext())
            .setTitle("Cambiar foto de perfil")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpenCamera()
                    1 -> abrirGaleria()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun abrirCamara() {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val imageFileName = "JPEG_${timeStamp}.jpg"
            val imageFile = File(requireContext().filesDir, imageFileName)

            imageUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                imageFile
            )

            // Limpiar URI anterior para evitar problemas
            imageUri?.let { uri ->
                requireContext().contentResolver.delete(uri, null, null)
            }

            cameraLauncher.launch(imageUri)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun abrirGaleria() {
        galleryLauncher.launch("image/*")
    }

    private fun procesarImagen(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: run {
                Toast.makeText(requireContext(), "No se pudo abrir la imagen", Toast.LENGTH_LONG).show()
                return
            }

            inputStream.use { stream ->
                // Redimensionar y hacer circular
                val bitmap = BitmapFactory.decodeStream(stream)
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 128, 128, true)
                val circularBitmap = getCircularBitmap(resizedBitmap)

                // Mostrar la imagen
                binding.imagenUsuarioEdit.setImageBitmap(circularBitmap)

                // Convertir a Base64
                val byteArray = ByteArrayOutputStream().apply {
                    circularBitmap.compress(Bitmap.CompressFormat.JPEG, 80, this)
                }.toByteArray()

                imagenBase64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al procesar imagen", Toast.LENGTH_LONG).show()
        }
    }


    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(bitmap.width / 2f, bitmap.height / 2f, bitmap.width / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    private fun checkCameraPermissionAndOpenCamera() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.CAMERA)
        } else {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissions.all {
                ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
            }) {
            abrirCamara()
        } else {
            requestPermissions(permissions, 1001)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                abrirCamara()
            } else {
                Toast.makeText(requireContext(), "Permisos necesarios denegados", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarDatosUsuario() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val email = currentUser?.email ?: return
        lifecycleScope.launch {
            try {
                val usuario = repository.consultaUsuarioPorCorreo(email)
                binding.nombreEdit.setText(usuario.usuario)
                binding.descripcionEdit.setText(usuario.descripcion)
            }catch (e: Exception){

            }
        }




        // Primero verifica si el usuario tiene foto de Google (URL)
        currentUser.photoUrl?.let { photoUrl ->
            // Si hay foto de Google, cargarla desde la URL
            lifecycleScope.launch {
                try {
                    loadImageFromUrl(photoUrl.toString())
                } catch (e: Exception) {
                    Log.e("AjustesCuenta", "Error al cargar imagen de Google: ${e.message}")
                    binding.imagenUsuarioEdit.setImageResource(R.drawable.cuent64)
                }
            }
        } ?: run {
            // Si no hay foto de Google, intentar cargar la imagen Base64 de tu base de datos
            lifecycleScope.launch {
                try {
                    val usuario = repository.consultaUsuarioPorCorreo(email)

                    usuario.imagen?.let { imagenBase64 ->
                        try {
                            val decodedBytes = Base64.decode(imagenBase64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            if (bitmap != null) {
                                val circularBitmap = getCircularBitmap(bitmap)
                                binding.imagenUsuarioEdit.setImageBitmap(circularBitmap)
                            } else {
                                binding.imagenUsuarioEdit.setImageResource(R.drawable.cuent64)
                            }
                        } catch (e: Exception) {
                            binding.imagenUsuarioEdit.setImageResource(R.drawable.cuent64)
                        }
                    } ?: run {
                        binding.imagenUsuarioEdit.setImageResource(R.drawable.cuent64)
                    }
                } catch (e: Exception) {
                    Log.e("AjustesCuenta", "Error al cargar datos: ${e.localizedMessage}")
                    binding.imagenUsuarioEdit.setImageResource(R.drawable.cuent64)
                }
            }
        }
    }

    private fun loadImageFromUrl(imageUrl: String) {
        Glide.with(requireContext())
            .load(imageUrl)
            .circleCrop()
            .into(binding.imagenUsuarioEdit)

        // Convertir a Base64 en un hilo en segundo plano
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val bitmap = Glide.with(requireContext())
                    .asBitmap()
                    .load(imageUrl)
                    .submit(128, 128)
                    .get() // Esto ahora es seguro porque estamos en Dispatchers.IO

                val byteArray = ByteArrayOutputStream().apply {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, this)
                }.toByteArray()

                imagenBase64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            } catch (e: Exception) {
                Log.e("AjustesCuenta", "Error al convertir imagen de Google a Base64", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error al procesar imagen", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }


    private fun inicializarBotones() {
        binding.imagenUsuarioEdit.setOnClickListener {
            mostrarDialogoImagen()
        }

        binding.btnHecho.setOnClickListener {
            val nombre = binding.nombreEdit.text.toString().trim()
            val descripcion = binding.descripcionEdit.text.toString().trim()

            val currentUser = FirebaseAuth.getInstance().currentUser
            val email = currentUser?.email ?: return@setOnClickListener

            lifecycleScope.launch {
                try {
                    val usuarios = repository.consultaTodosUsuarios()
                    val usuarioActual = usuarios.find { it.correo.equals(email, ignoreCase = true) }

                    if (usuarioActual != null) {
                        val actualizado = usuarioActual.copy(
                            usuario = nombre,
                            descripcion = descripcion,
                            imagen = imagenBase64 ?: usuarioActual.imagen
                        )

                        // Crear request para la API
                        val request = Usuario(
                            id_usuario = actualizado.id_usuario,
                            correo = actualizado.correo,
                            usuario = actualizado.usuario,
                            descripcion = actualizado.descripcion,
                            imagen = actualizado.imagen
                        )

                        val response = repository.actualizarUsuario(request)

                        if (response.isSuccessful) {
                            val action = AjustesCuentaFragmentDirections.actionAjustesCuentaFragmentToCuentaFragment(args.tab)
                            findNavController().navigate(action)
                        } else {
                            Log.e("AjustesCuenta", "Error al actualizar: ${response.errorBody()?.string()}")
                            Toast.makeText(requireContext(), "Error al actualizar perfil", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AjustesCuenta", "Error: ${e.localizedMessage}")
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.EliminarCuentabtn.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.")
                .setPositiveButton("Sí") { _, _ ->
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val email = currentUser?.email ?: return@setPositiveButton

                    lifecycleScope.launch {
                        try {
                            val usuarios = repository.consultaTodosUsuarios()
                            val usuario = usuarios.find { it.correo.equals(email, ignoreCase = true) }

                            if (usuario != null) {
                                val response = repository.eliminarUsuario(usuario.id_usuario)
                                if (response.isSuccessful) {
                                    currentUser.delete().addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            findNavController().navigate(R.id.action_ajustesCuentaFragment_to_loginFragment)
                                        } else {
                                            Log.e("EliminarCuenta", "Error al eliminar de FirebaseAuth: ${task.exception}")
                                        }
                                    }
                                } else {
                                    Log.e("EliminarCuenta", "Error al eliminar de la API: ${response.errorBody()?.string()}")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("EliminarCuenta", "Error: ${e.localizedMessage}")
                        }
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }

        binding.btnVolver.setOnClickListener {
            val action = AjustesCuentaFragmentDirections.actionAjustesCuentaFragmentToCuentaFragment(args.tab)
            findNavController().navigate(action)
        }

        binding.CerrarSesionbtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.action_ajustesCuentaFragment_to_loginFragment)
        }

        binding.cambiarContraseAbtn.setOnClickListener {
            findNavController().navigate(R.id.action_ajustesCuentaFragment_to_noRecuerdoFragment)
        }
    }
}
