package dam.moviles.cocinetica.vista

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationBarView
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentCuentaBinding
import dam.moviles.cocinetica.modelo.CocineticaRepository
import dam.moviles.cocinetica.modelo.Comentario
import dam.moviles.cocinetica.modelo.Receta
import dam.moviles.cocinetica.modelo.Valoracion
import dam.moviles.cocinetica.viewModel.CuentaViewModel
import kotlinx.coroutines.launch
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth

class CuentaFragment : Fragment() {

    private lateinit var binding: FragmentCuentaBinding
    private val cuentaViewModel: CuentaViewModel by viewModels()
    private var recetasCargadas: List<Receta>? = null
    private var recetasGuardadasCargadas: Set<Int>? = null
    private val args: CuentaFragmentArgs by navArgs()


    private var comentariosCargados: List<Comentario>? = null
    private var valoracionesCargadas: Map<Int, Valoracion>? = null
    private lateinit var recetaAdapter: RecetaAdapter
    private val repository = CocineticaRepository()
    private var currentTabTag = "recetas"
    private var shouldReloadOnResume = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCuentaBinding.inflate(inflater, container, false)

        binding.bottomNav.selectedItemId = R.id.nav_profile
        configurarTabs()
        actualizoCurrentTabTag(args.tabSelect)
        inicializarBotones()
        observarViewModel()
        cuentaViewModel.cargarUsuarioYContenido()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        // Restaurar la pestaña seleccionada
        binding.navegadorRecetasCometarios.setCurrentTabByTag(currentTabTag)

        // Recargar datos si es necesario
        if (shouldReloadOnResume) {
            cuentaViewModel.usuario.value?.let { usuario ->
                cuentaViewModel.cargarUsuarioYContenido()
                cuentaViewModel.cargarMisRecetas(usuario.id_usuario)
            }
            shouldReloadOnResume = false
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

    private fun observarViewModel() {
        cuentaViewModel.usuario.observe(viewLifecycleOwner) { usuario ->
            binding.txtNombre.text = usuario.usuario
            binding.txtDescripciN.text = usuario.descripcion

            // Intentar primero con la imagen de la base de datos si existe
            usuario.imagen?.let { imagenBase64 ->
                try {
                    val decodedBytes = Base64.decode(imagenBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    if (bitmap != null) {
                        val circularBitmap = getCircularBitmap(bitmap)
                        binding.imagenCuenta.setImageBitmap(circularBitmap)
                        return@observe // Salir si la imagen de la BD se cargó correctamente
                    } else {
                        //nada
                    }
                } catch (e: Exception) {
                    Log.e("CuentaFragment", "Error al cargar imagen Base64", e)
                }
            }

            // Si no hay imagen en BD o falló, intentar con Google
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.photoUrl?.let { photoUrl ->
                Glide.with(requireContext())
                    .load(photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.cuent64)
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Añade esto para evitar caché
                    .skipMemoryCache(true) // Añade esto para evitar caché
                    .into(binding.imagenCuenta)
            } ?: run {
                binding.imagenCuenta.setImageResource(R.drawable.cuent64)
            }
        }

        cuentaViewModel.recetas.observe(viewLifecycleOwner) { recetas ->
            recetasCargadas = recetas
            inicializarOActualizarAdapter()
        }

        cuentaViewModel.recetasGuardadas.observe(viewLifecycleOwner) { guardadas ->
            recetasGuardadasCargadas = guardadas
            inicializarOActualizarAdapter()
        }


        cuentaViewModel.comentarios.observe(viewLifecycleOwner) { comentarios ->
            comentariosCargados = comentarios
            actualizarAdapterSiListo()
        }

        cuentaViewModel.valoraciones.observe(viewLifecycleOwner) { valoraciones ->
            valoracionesCargadas = valoraciones
            actualizarAdapterSiListo()
        }
    }

    private fun actualizarAdapterSiListo() {
        val usuario = cuentaViewModel.usuario.value ?: return
        val comentarios = comentariosCargados ?: return
        val valoraciones = valoracionesCargadas ?: return

        val comentariosMios = comentarios.filter { it.id_usuario == usuario.id_usuario }

        binding.txtNoComentarios.visibility = if (comentariosMios.isEmpty()) View.VISIBLE else View.GONE
        binding.misComentarios.visibility = if (comentariosMios.isEmpty()) View.GONE else View.VISIBLE

        val adapter = ComentarioUsuarioAdapter(
            comentarios = comentariosMios,
            valoracionesMap = valoraciones,
            nombreUsuario = usuario.usuario,
            onEliminarClick = { comentario -> cuentaViewModel.eliminarComentario(comentario) },
            onIrClick = { idReceta ->
                // Marcar que venimos de comentarios
                currentTabTag = "comentarios"
                shouldReloadOnResume = true

                val action = CuentaFragmentDirections.actionCuentaFragmentToVerRecetaFragment3(
                    idReceta,
                    origen = "cuenta" // Mantenemos el valor original
                )
                findNavController().navigate(action)
            }
        )

        binding.misComentarios.layoutManager = LinearLayoutManager(requireContext())
        binding.misComentarios.adapter = adapter
    }

    private fun configurarTabs() {
        val tabHost = binding.navegadorRecetasCometarios
        tabHost.setup()

        val tabSpec1 = tabHost.newTabSpec("recetas")
        tabSpec1.setContent(R.id.Recetas)
        tabSpec1.setIndicator("Recetas")
        tabHost.addTab(tabSpec1)

        val tabSpec2 = tabHost.newTabSpec("comentarios")
        tabSpec2.setContent(R.id.Comentarios)
        tabSpec2.setIndicator("Comentarios")
        tabHost.addTab(tabSpec2)

        tabHost.setOnTabChangedListener { tabId ->
            currentTabTag = tabId
        }

        tabHost.setCurrentTabByTag(currentTabTag)
    }

    fun actualizoCurrentTabTag(nuevoTag: String) {
        currentTabTag = nuevoTag
    }


    private fun inicializarBotones() {
        binding.fabAgregar.setOnClickListener {
            val action = CuentaFragmentDirections.actionCuentaFragmentToCreaRecetaFragment(currentTabTag, true)
            findNavController().navigate(action)
        }

        binding.bottomNav.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val action = CuentaFragmentDirections.actionCuentaFragmentToInicioFragment(currentTabTag)
                    findNavController().navigate(action)
                    true
                }
                R.id.nav_search -> {
                    val action = CuentaFragmentDirections.actionCuentaFragmentToBusquedaFragment(currentTabTag)
                    findNavController().navigate(action)
                    true
                }
                R.id.nav_book -> {
                    val action = CuentaFragmentDirections.actionCuentaFragmentToGuardadosFragment(currentTabTag)
                    findNavController().navigate(action)
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        })

        binding.btnEditarCuenta.setOnClickListener {
            val action = CuentaFragmentDirections.actionCuentaFragmentToAjustesCuentaFragment(currentTabTag)
            findNavController().navigate(action)
        }

        binding.btnAyuda.setOnClickListener {
            findNavController().navigate(R.id.action_cuentaFragment_to_ayudaFragment)
        }
    }



    private fun inicializarOActualizarAdapter() {
        val recetas = recetasCargadas ?: return
        val guardadas = recetasGuardadasCargadas ?: return
        val usuario = cuentaViewModel.usuario.value ?: return

        if (!::recetaAdapter.isInitialized) {
            recetaAdapter = RecetaAdapter(
                recetas = recetas.toMutableList(),
                enVistaGrid = false,
                recetasGuardadas = guardadas.toMutableSet(),
                idUsuario = usuario.id_usuario,
                nombreAutor = usuario.usuario,
                onGuardarClick = { receta, estabaGuardada ->
                    lifecycleScope.launch {
                        try {
                            val resultado = if (estabaGuardada) {
                                repository.eliminarRecetaGuardada(usuario.id_usuario, receta.id_receta)
                            } else {
                                repository.agregarRecetaGuardada(usuario.id_usuario, receta.id_receta)
                            }

                            if (resultado) {
                                val nuevasGuardadas = recetaAdapter.recetasGuardadas
                                if (estabaGuardada) {
                                    nuevasGuardadas.remove(receta.id_receta)
                                } else {
                                    nuevasGuardadas.add(receta.id_receta)
                                }

                                cuentaViewModel._recetasGuardadas.postValue(nuevasGuardadas.toSet())
                                recetaAdapter.notifyItemChanged(
                                    recetaAdapter.recetas.indexOfFirst { it.id_receta == receta.id_receta }
                                )
                            } else {
                                Toast.makeText(requireContext(), "Error al guardar/cancelar receta", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Error inesperado: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    true
                },
                onVerClick = { receta ->
                    lifecycleScope.launch {
                        try {
                            // Marcar que venimos de recetas
                            currentTabTag = "recetas"
                            shouldReloadOnResume = true

                            val recetaActualizada = repository.consultaRecetaPorId(receta.id_receta)
                            val action = CuentaFragmentDirections.actionCuentaFragmentToVerRecetaFragment3(
                                recetaActualizada.id_receta,
                                origen = "cuenta"
                            )
                            findNavController().navigate(action)
                        } catch (e: Exception) {
                            // Manejo de errores existente
                        }
                    }
                },
                onEliminarClick = { idReceta ->
                    confirmarYEliminarReceta(idReceta)
                }
            )

            binding.misRecetas.layoutManager = LinearLayoutManager(requireContext())
            binding.misRecetas.adapter = recetaAdapter
        } else {
            recetaAdapter.actualizarRecetas(recetas)
        }

        // Mostrar u ocultar mensaje de no recetas
        if (recetas.isEmpty()) {
            binding.txtNoRecetas.visibility = View.VISIBLE
            binding.misRecetas.visibility = View.GONE
        } else {
            binding.txtNoRecetas.visibility = View.GONE
            binding.misRecetas.visibility = View.VISIBLE
        }
    }

    private fun confirmarYEliminarReceta(idReceta: Int) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmar eliminación")
        builder.setMessage("¿Estás seguro de que quieres eliminar esta receta?")

        builder.setPositiveButton("Sí") { dialog, _ ->
            dialog.dismiss()
            eliminarReceta(idReceta)
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun eliminarReceta(idReceta: Int) {
        lifecycleScope.launch {
            try {
                val resultado = repository.eliminarReceta(idReceta)
                if (resultado) {
                    val index = recetaAdapter.recetas.indexOfFirst { it.id_receta == idReceta }
                    if (index != -1) {
                        recetaAdapter.eliminarRecetaPorIndex(index)
                        Toast.makeText(requireContext(), "Receta eliminada", Toast.LENGTH_SHORT).show()

                        // Actualizar visibilidad del texto cuando no hay recetas
                        if (recetaAdapter.recetas.isEmpty()) {
                            binding.txtNoRecetas.visibility = View.VISIBLE
                            binding.misRecetas.visibility = View.GONE
                        } else {
                            binding.txtNoRecetas.visibility = View.GONE
                            binding.misRecetas.visibility = View.VISIBLE
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "No se pudo eliminar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
