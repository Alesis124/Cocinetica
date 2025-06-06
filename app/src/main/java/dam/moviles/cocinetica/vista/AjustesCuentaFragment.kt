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
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentAjustesCuentaBinding
import dam.moviles.cocinetica.viewModel.AjustesCuentaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class AjustesCuentaFragment : Fragment() {

    private lateinit var binding: FragmentAjustesCuentaBinding
    private val args: AjustesCuentaFragmentArgs by navArgs()
    private lateinit var viewModel: AjustesCuentaViewModel

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && viewModel.imageUri.value != null) {
            viewModel.procesarImagen(requireContext(), viewModel.imageUri.value!!)
        } else {
            Toast.makeText(requireContext(), "Error al tomar la foto", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            viewModel.procesarImagen(requireContext(), uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(AjustesCuentaViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAjustesCuentaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observarViewModel()
        inicializarBotones()

        if (savedInstanceState == null) {
            viewModel.cargarDatosUsuario(FirebaseAuth.getInstance().currentUser?.email)
        } else {
            binding.nombreEdit.setText(savedInstanceState.getString("nombre", ""))
            binding.descripcionEdit.setText(savedInstanceState.getString("descripcion", ""))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("nombre", binding.nombreEdit.text.toString())
        outState.putString("descripcion", binding.descripcionEdit.text.toString())
    }

    private fun observarViewModel() {
        viewModel.imagenBase64.observe(viewLifecycleOwner) { base64String ->
            base64String?.let {
                try {
                    val decodedBytes = Base64.decode(it, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    if (bitmap != null) {
                        val circularBitmap = getCircularBitmap(bitmap)
                        binding.imagenUsuarioEdit.setImageBitmap(circularBitmap)
                    } else {
                        binding.imagenUsuarioEdit.setImageResource(R.drawable.cuent64)
                    }
                } catch (e: IllegalArgumentException) {
                    Log.e("AjustesCuentaFragment", "Invalid Base64 string for image: $it", e)
                    binding.imagenUsuarioEdit.setImageResource(R.drawable.cuent64)
                }
            } ?: run {
                binding.imagenUsuarioEdit.setImageResource(R.drawable.cuent64)
            }
        }

        viewModel.usuarioNombre.observe(viewLifecycleOwner) { nombre ->
            if (binding.nombreEdit.text.toString() != nombre) {
                binding.nombreEdit.setText(nombre)
            }
        }

        viewModel.usuarioDescripcion.observe(viewLifecycleOwner) { descripcion ->
            if (binding.descripcionEdit.text.toString() != descripcion) {
                binding.descripcionEdit.setText(descripcion)
            }
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearToastMessage()
            }
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { destinationId ->
                when (destinationId) {
                    R.id.action_ajustesCuentaFragment_to_cuentaFragment -> {
                        val action = AjustesCuentaFragmentDirections.actionAjustesCuentaFragmentToCuentaFragment(args.tab)
                        findNavController().navigate(action)
                    }
                    R.id.action_ajustesCuentaFragment_to_loginFragment -> {
                        findNavController().navigate(R.id.action_ajustesCuentaFragment_to_loginFragment)
                    }
                    R.id.action_ajustesCuentaFragment_to_noRecuerdoFragment -> {
                        findNavController().navigate(R.id.action_ajustesCuentaFragment_to_noRecuerdoFragment)
                    }
                }
            }
        }

        viewModel.glideImageUrl.observe(viewLifecycleOwner) { imageUrl ->
            imageUrl?.let {
                Glide.with(requireContext())
                    .load(it)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(binding.imagenUsuarioEdit)
                viewModel.clearGlideImageUrl()
            }
        }
    }

    private fun inicializarBotones() {
        binding.imagenUsuarioEdit.setOnClickListener {
            mostrarDialogoImagen()
        }

        binding.nombreEdit.doAfterTextChanged { editable ->
            viewModel.setNombreUsuario(editable?.toString() ?: "")
        }

        binding.descripcionEdit.doAfterTextChanged { editable ->
            viewModel.setDescripcionUsuario(editable?.toString() ?: "")
        }

        binding.btnHecho.setOnClickListener {
            viewModel.actualizarUsuario()
        }

        binding.EliminarCuentabtn.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.")
                .setPositiveButton("Sí") { _, _ ->
                    viewModel.eliminarCuenta()
                }
                .setNegativeButton("No", null)
                .show()
        }

        binding.btnVolver.setOnClickListener {
            viewModel.navigateTo(R.id.action_ajustesCuentaFragment_to_cuentaFragment)
        }

        binding.CerrarSesionbtn.setOnClickListener {
            viewModel.cerrarSesion()
        }

        binding.cambiarContraseAbtn.setOnClickListener {
            viewModel.navigateTo(R.id.action_ajustesCuentaFragment_to_noRecuerdoFragment)
        }
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

            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                imageFile
            )
            viewModel.setImageUri(uri)
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun abrirGaleria() {
        galleryLauncher.launch("image/*")
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
}