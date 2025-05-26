package dam.moviles.cocinetica.vista

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.navigation.NavigationBarView
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentResultadoBusquedaBinding
import dam.moviles.cocinetica.modelo.CocineticaRepository
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import dam.moviles.cocinetica.modelo.Receta
import kotlinx.coroutines.launch

class ResultadoBusquedaFragment : Fragment() {

    lateinit var binding: FragmentResultadoBusquedaBinding
    private val repository = CocineticaRepository()
    private val args: ResultadoBusquedaFragmentArgs by navArgs()

    private lateinit var recetaAdapter: RecetaAdapter
    private val recetasGuardadas = mutableSetOf<Int>()
    private var idUsuarioActual: Int? = null
    private var skipNavSelection = false

    // Variable para trackear si estamos en vista grid o lista
    private var enVistaGrid = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentResultadoBusquedaBinding.inflate(inflater, container, false)
        inicializarBotones()
        cargarDatosRecetas(args.idReceta.toList())
        return binding.root
    }

    private fun inicializarBotones() {
        binding.fabBusquedaAgregar.setOnClickListener {
            findNavController().navigate(R.id.action_resultadoBusquedaFragment_to_creaRecetaFragment)
        }

        binding.bottomNavBusqueda.setOnItemSelectedListener { item ->
            if (skipNavSelection) {
                skipNavSelection = false
                return@setOnItemSelectedListener true
            }

            val navController = findNavController()
            when (item.itemId) {
                R.id.nav_home -> {
                    if (navController.currentDestination?.id != R.id.inicioFragment)
                        navController.navigate(R.id.action_resultadoBusquedaFragment_to_inicioFragment)
                    true
                }
                R.id.nav_search -> {
                    if (navController.currentDestination?.id != R.id.busquedaFragment)
                        navController.navigate(R.id.action_resultadoBusquedaFragment_to_busquedaFragment)
                    true
                }
                R.id.nav_book -> {
                    if (navController.currentDestination?.id != R.id.guardadosFragment)
                        navController.navigate(R.id.action_resultadoBusquedaFragment_to_guardadosFragment)
                    true
                }
                R.id.nav_profile -> {
                    if (navController.currentDestination?.id != R.id.cuentaFragment)
                        navController.navigate(R.id.action_resultadoBusquedaFragment_to_cuentaFragment)
                    true
                }
                else -> false
            }
        }

        skipNavSelection = true
        binding.bottomNavBusqueda.selectedItemId = R.id.nav_search

        // Cambiar vista lista/grid con el botón
        binding.btnBusquedaGrid.setOnClickListener {
            enVistaGrid = !enVistaGrid
            actualizarLayoutSegunVista()
        }
    }

    private fun actualizarLayoutSegunVista() {
        if (enVistaGrid) {
            val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 6 else 3
            binding.recyclerViewBusqueda.layoutManager = GridLayoutManager(requireContext(), spanCount)
            binding.btnBusquedaGrid.setImageResource(R.drawable.menu) // Icono para grid, cámbialo si quieres otro
        } else {
            binding.recyclerViewBusqueda.layoutManager = LinearLayoutManager(requireContext())
            binding.btnBusquedaGrid.setImageResource(android.R.drawable.ic_dialog_dialer) // Icono para lista
        }

        if (::recetaAdapter.isInitialized) {
            recetaAdapter.cambiarVista(enVistaGrid)
        }
    }

    private fun cargarDatosRecetas(idsRecetas: List<Int>) {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: return

        lifecycleScope.launch {
            try {
                val usuario = repository.consultaUsuarioPorCorreo(email)
                idUsuarioActual = usuario.id_usuario

                val guardadas = repository.obtenerRecetasGuardadas(idUsuarioActual!!)
                recetasGuardadas.clear()
                recetasGuardadas.addAll(guardadas.map { it.id_receta })

                val recetasFiltradas = idsRecetas.map { id -> repository.consultaRecetaPorId(id) }

                recetaAdapter = RecetaAdapter(
                    recetasFiltradas,
                    enVistaGrid,
                    recetasGuardadas,
                    idUsuarioActual,
                    onGuardarClick = { receta, estaGuardada ->
                        onGuardarClick(receta, estaGuardada)
                        true
                    },
                    onVerClick = { receta ->
                        val action = ResultadoBusquedaFragmentDirections.actionResultadoBusquedaFragmentToVerRecetaFragment(receta.id_receta)
                        findNavController().navigate(action)
                    }
                )

                binding.recyclerViewBusqueda.adapter = recetaAdapter
                actualizarLayoutSegunVista()

            } catch (e: Exception) {
                Log.e("ResultadoBusqueda", "Error cargando recetas: ${e.message}", e)
            }
        }
    }

    private fun onGuardarClick(receta: Receta, estaGuardada: Boolean) {
        val idUsuario = idUsuarioActual ?: return
        lifecycleScope.launch {
            val resultado = if (estaGuardada) {
                repository.eliminarRecetaGuardada(idUsuario, receta.id_receta)
            } else {
                repository.agregarRecetaGuardada(idUsuario, receta.id_receta)
            }

            requireActivity().runOnUiThread {
                if (resultado) {
                    if (estaGuardada) {
                        recetasGuardadas.remove(receta.id_receta)
                    } else {
                        recetasGuardadas.add(receta.id_receta)
                    }
                    recetaAdapter.notifyItemChanged(recetaAdapter.recetas.indexOfFirst { it.id_receta == receta.id_receta })
                }
            }
        }
    }
}
