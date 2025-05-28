package dam.moviles.cocinetica.vista

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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

class CuentaFragment : Fragment() {

    private lateinit var binding: FragmentCuentaBinding
    private val cuentaViewModel: CuentaViewModel by viewModels()
    private var recetasCargadas: List<Receta>? = null
    private var recetasGuardadasCargadas: Set<Int>? = null


    private var comentariosCargados: List<Comentario>? = null
    private var valoracionesCargadas: Map<Int, Valoracion>? = null
    private lateinit var recetaAdapter: RecetaAdapter
    private val repository = CocineticaRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCuentaBinding.inflate(inflater, container, false)

        binding.bottomNav.selectedItemId = R.id.nav_profile
        configurarTabs()
        inicializarBotones()
        observarViewModel()
        cuentaViewModel.cargarUsuarioYContenido()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        observarViewModel()
    }

    private fun observarViewModel() {
        cuentaViewModel.usuario.observe(viewLifecycleOwner) { usuario ->
            binding.txtNombre.text = usuario.usuario
            binding.txtDescripciN.text = usuario.descripcion

            cuentaViewModel.cargarMisRecetas(usuario.id_usuario)

            // Ya no creamos el adapter aquí directamente
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
                val action = CuentaFragmentDirections.actionCuentaFragmentToVerRecetaFragment3(idReceta, origen = "cuenta")
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
    }

    private fun inicializarBotones() {
        binding.fabAgregar.setOnClickListener {
            findNavController().navigate(R.id.action_cuentaFragment_to_creaRecetaFragment)
        }

        binding.bottomNav.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    findNavController().navigate(R.id.action_cuentaFragment_to_inicioFragment)
                    true
                }
                R.id.nav_search -> {
                    findNavController().navigate(R.id.action_cuentaFragment_to_busquedaFragment)
                    true
                }
                R.id.nav_book -> {
                    findNavController().navigate(R.id.action_cuentaFragment_to_guardadosFragment)
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        })

        binding.btnEditarCuenta.setOnClickListener {
            findNavController().navigate(R.id.action_cuentaFragment_to_ajustesCuentaFragment)
        }

        binding.btnAyuda.setOnClickListener {
            // Acción de ayuda opcional
        }
    }

    private fun crearAdaptador(guardadas: Set<Int>) {
        val usuario = cuentaViewModel.usuario.value ?: return

        recetaAdapter = RecetaAdapter(
            recetas = mutableListOf(),
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
                        val recetaActualizada = repository.consultaRecetaPorId(receta.id_receta)
                        val action = CuentaFragmentDirections.actionCuentaFragmentToVerRecetaFragment3(
                            recetaActualizada.id_receta,
                            origen = "cuenta"
                        )
                        findNavController().navigate(action)
                    } catch (e: Exception) {
                        val index = recetaAdapter.recetas.indexOfFirst { it.id_receta == receta.id_receta }
                        if (index != -1) {
                            recetaAdapter.eliminarRecetaPorIndex(index)
                            Toast.makeText(requireContext(), "La receta ya no está disponible", Toast.LENGTH_SHORT).show()
                        }
                        Log.e("CuentaFragment", "Error al ver receta: ${e.message}")
                    }
                }
            }
        )

        binding.misRecetas.layoutManager = LinearLayoutManager(requireContext())
        binding.misRecetas.adapter = recetaAdapter
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
                            val recetaActualizada = repository.consultaRecetaPorId(receta.id_receta)
                            val action = CuentaFragmentDirections.actionCuentaFragmentToVerRecetaFragment3(
                                recetaActualizada.id_receta,
                                origen = "cuenta"
                            )
                            findNavController().navigate(action)
                        } catch (e: Exception) {
                            val index = recetaAdapter.recetas.indexOfFirst { it.id_receta == receta.id_receta }
                            if (index != -1) {
                                recetaAdapter.eliminarRecetaPorIndex(index)
                                Toast.makeText(requireContext(), "La receta ya no está disponible", Toast.LENGTH_SHORT).show()
                            }
                            Log.e("CuentaFragment", "Error al ver receta: ${e.message}")
                        }
                    }
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

}
