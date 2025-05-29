package dam.moviles.cocinetica.vista

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dam.moviles.cocinetica.databinding.FragmentCreaRecetaBinding
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.modelo.Contiene
import dam.moviles.cocinetica.modelo.Receta
import dam.moviles.cocinetica.viewModel.CreaRecetaViewModel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CreaRecetaFragment : Fragment() {

    private lateinit var binding: FragmentCreaRecetaBinding
    private lateinit var viewModel: CreaRecetaViewModel
    private val unidades = listOf("g", "kg", "ml", "l", "cucharada", "taza", "pieza")
    private val args: CreaRecetaFragmentArgs by navArgs()

    // Registros para los resultados de actividad
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            viewModel.imagenUri.value?.let { uri ->
                cargarImagenDesdeUri(uri)
            }
        } else {
            Toast.makeText(requireContext(), "Error al tomar la foto", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.setImagenUri(it)
            cargarImagenDesdeUri(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreaRecetaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[CreaRecetaViewModel::class.java]

        // Configurar el click listener para el botón de imagen
        binding.imageButton.setOnClickListener {
            mostrarDialogoSeleccionImagen()
        }

        // Observar cambios en la imagen
        viewModel.imagenBase64.observe(viewLifecycleOwner) { base64 ->
            base64?.let {
                val decodedBytes = Base64.decode(it, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                binding.imageButton.setImageBitmap(bitmap)
            }
        }

        viewModel.cargarDatosDisponibles()
        viewModel.cargarUsuarioDesdeFirebase()

        viewModel.usuario.observe(viewLifecycleOwner) { usuario ->
            binding.buttonGuardar.isEnabled = (usuario != null)
            if (usuario == null) {
                Toast.makeText(requireContext(), "No hay usuario logueado", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonAgregarIngrediente.setOnClickListener {
            sincronizarDatosConViewModel()
            viewModel.agregarIngrediente()
            renderIngredientes()
        }

        binding.buttonAgregarPaso.setOnClickListener {
            sincronizarDatosConViewModel()
            viewModel.agregarPaso()
            renderPasos()
        }

        renderIngredientes()
        renderPasos()
        inicializarBotones()
    }

    private fun mostrarDialogoSeleccionImagen() {
        val opciones = arrayOf("Tomar foto", "Elegir de galería", "Cancelar")
        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar imagen de receta")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpenCamera()
                    1 -> abrirGaleria()
                    2 -> {} // Cancelar
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndOpenCamera() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.CAMERA)
        } else {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissions.all { ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED }) {
            abrirCamara()
        } else {
            requestPermissions(permissions, 1001)
        }
    }

    private fun abrirCamara() {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"
            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

            val photoPath = imageFile.absolutePath
            viewModel.setCurrentPhotoPath(photoPath)

            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                imageFile
            )

            viewModel.setImagenUri(uri)
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al abrir cámara: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun abrirGaleria() {
        galleryLauncher.launch("image/*")
    }

    private fun cargarImagenDesdeUri(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Redimensionar para optimización
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 800, 800, true)

            // Mostrar la imagen
            binding.imageButton.setImageBitmap(resizedBitmap)

            // Convertir a Base64 y guardar en ViewModel
            viewModel.setImagenBase64(viewModel.bitmapToBase64(resizedBitmap))

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al cargar imagen", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            abrirCamara()
        } else {
            Toast.makeText(requireContext(), "Permisos necesarios denegados", Toast.LENGTH_SHORT).show()
        }
    }

    private fun inicializarBotones() {
        binding.buttonVolver.setOnClickListener {
            if(args.tab == "nada" && !args.cuenta) {
                requireActivity().onBackPressed()
            } else {
                val action = CreaRecetaFragmentDirections.actionCreaRecetaFragmentToCuentaFragment(args.tab)
                findNavController().navigate(action)
            }
        }

        binding.buttonGuardar.setOnClickListener {
            sincronizarDatosConViewModel()

            lifecycleScope.launch {
                val nombre = binding.editTextNombre.text.toString().trim()
                val duracionStr = binding.editTextDuracion.text.toString()
                val duracion = duracionStr.toIntOrNull() ?: 0
                val idUsuario = viewModel.usuario.value?.id_usuario ?: return@launch

                val receta = Receta(
                    id_receta = 0,
                    nombre = nombre,
                    duracion = duracion,
                    valoracion = "0",
                    imagen = viewModel.imagenBase64.value ?: "",
                    id_usuario = idUsuario
                )

                try {
                    val ingredientesList = viewModel.ingredientes.map { ing ->
                        val idIng = viewModel.repository.obtenerOInsertarIngrediente(ing.nombre)
                        val idUM = viewModel.obtenerIdUMPorNombre(ing.unidad)
                        val cantidad = ing.cantidad.toDoubleOrNull()

                        if (idUM == null || cantidad == null) {
                            throw IllegalArgumentException("Unidad o cantidad inválida para ingrediente '${ing.nombre}'")
                        }

                        Contiene(0, idIng, idUM, cantidad)
                    }

                    val pasosList = viewModel.pasos.mapNotNull { it.descripcion.takeIf { it.isNotBlank() } }

                    if (nombre.isBlank() || ingredientesList.isEmpty() || pasosList.isEmpty()) {
                        Toast.makeText(requireContext(), "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val idReceta = viewModel.repository.insertarRecetaCompleta(receta, ingredientesList, pasosList)
                    val recetaInsertada = viewModel.repository.consultaRecetaPorId(idReceta ?: 0)

                    if (recetaInsertada != null) {
                        Toast.makeText(requireContext(), "Receta guardada con éxito", Toast.LENGTH_SHORT).show()
                        val action = CreaRecetaFragmentDirections.actionCreaRecetaFragmentToCompletadoFragment(recetaInsertada.id_receta)
                        findNavController().navigate(action)
                    } else {
                        Toast.makeText(requireContext(), "Error al confirmar receta guardada", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_creaRecetaFragment_to_errorFragment)
                    }

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun sincronizarDatosConViewModel() {
        // Sincronizar ingredientes
        for (i in 0 until binding.layoutIngredientes.childCount) {
            val ingredienteView = binding.layoutIngredientes.getChildAt(i)
            val etCantidad = ingredienteView.findViewById<EditText>(R.id.editTextCantidad)
            val etNombre = ingredienteView.findViewById<EditText>(R.id.editTextNombreIngrediente)
            val spinnerUnidad = ingredienteView.findViewById<Spinner>(R.id.spinnerUnidad)

            if (i < viewModel.ingredientes.size) {
                viewModel.ingredientes[i].cantidad = etCantidad.text.toString()
                viewModel.ingredientes[i].nombre = etNombre.text.toString()
                viewModel.ingredientes[i].unidad = spinnerUnidad.selectedItem.toString()
            }
        }

        // Sincronizar pasos
        for (i in 0 until binding.layoutPasos.childCount) {
            val pasoView = binding.layoutPasos.getChildAt(i)
            val etDescripcion = pasoView.findViewById<EditText>(R.id.etDescripcionPaso)

            if (i < viewModel.pasos.size) {
                viewModel.pasos[i].descripcion = etDescripcion.text.toString()
            }
        }
    }

    private fun renderIngredientes() {
        binding.layoutIngredientes.removeAllViews()
        viewModel.ingredientes.forEachIndexed { index, ingrediente ->
            val ingredienteView = layoutInflater.inflate(R.layout.item_ingrediente, binding.layoutIngredientes, false)

            val tvNumero = ingredienteView.findViewById<TextView>(R.id.textViewOrdenIngrediente)
            val etCantidad = ingredienteView.findViewById<EditText>(R.id.editTextCantidad)
            val etNombre = ingredienteView.findViewById<EditText>(R.id.editTextNombreIngrediente)
            val spinnerUnidad = ingredienteView.findViewById<Spinner>(R.id.spinnerUnidad)
            val btnEliminar = ingredienteView.findViewById<ImageButton>(R.id.buttonEliminarIngrediente)

            tvNumero.text = "${index + 1}."
            etCantidad.setText(ingrediente.cantidad)
            etNombre.setText(ingrediente.nombre)

            etCantidad.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.ingredientes[index].cantidad = etCantidad.text.toString()
                }
            }

            etNombre.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.ingredientes[index].nombre = etNombre.text.toString()
                }
            }

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, unidades)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerUnidad.adapter = adapter

            val pos = unidades.indexOf(ingrediente.unidad)
            spinnerUnidad.setSelection(if (pos != -1) pos else 0)

            spinnerUnidad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                    viewModel.ingredientes[index].unidad = unidades[pos]
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            btnEliminar.setOnClickListener {
                ocultarTecladoYQuitarFoco()
                sincronizarDatosConViewModel()
                viewModel.eliminarIngrediente(index)
                renderIngredientes()
            }

            binding.layoutIngredientes.addView(ingredienteView)
        }
    }

    private fun renderPasos() {
        binding.layoutPasos.removeAllViews()
        viewModel.pasos.forEachIndexed { index, paso ->
            val pasoView = layoutInflater.inflate(R.layout.item_paso, binding.layoutPasos, false)

            val tvNumeroPaso = pasoView.findViewById<TextView>(R.id.tvNumeroPaso)
            val etDescripcion = pasoView.findViewById<EditText>(R.id.etDescripcionPaso)
            val btnEliminar = pasoView.findViewById<ImageButton>(R.id.btnEliminarPaso)

            tvNumeroPaso.text = "${index + 1}."
            etDescripcion.setText(paso.descripcion)

            etDescripcion.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.pasos[index].descripcion = etDescripcion.text.toString()
                }
            }

            btnEliminar.setOnClickListener {
                ocultarTecladoYQuitarFoco()
                sincronizarDatosConViewModel()
                viewModel.eliminarPaso(index)
                renderPasos()
            }

            binding.layoutPasos.addView(pasoView)
        }
    }

    private fun ocultarTecladoYQuitarFoco() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        val view = requireActivity().currentFocus
        view?.clearFocus()
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}