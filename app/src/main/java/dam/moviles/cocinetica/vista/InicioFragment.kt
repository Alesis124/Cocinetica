package dam.moviles.cocinetica.vista

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentInicioBinding
import dam.moviles.cocinetica.modelo.CocineticaRepository
import dam.moviles.cocinetica.modelo.Receta
import dam.moviles.cocinetica.viewModel.InicioFragmenViewModel
import dam.moviles.cocinetica.viewModel.InicioFragmenViewModelFactory
import kotlinx.coroutines.launch

class InicioFragment : Fragment() {

    private lateinit var binding: FragmentInicioBinding
    private lateinit var viewModel: InicioFragmenViewModel
    private lateinit var recetaAdapter: RecetaAdapter

    private val recetasGuardadas = mutableSetOf<Int>()
    private var idUsuarioActual: Int? = null
    private val repository = CocineticaRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inicializarViewModel()
        inicializarBotones()
        cargarDatosCompletos()
    }

    override fun onResume() {
        super.onResume()
        cargarDatosCompletos()
    }

    private fun inicializarViewModel() {
        val factory = InicioFragmenViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[InicioFragmenViewModel::class.java]

        viewModel.enVistaGrid.observe(viewLifecycleOwner) { enGrid ->
            if (!::recetaAdapter.isInitialized) return@observe

            if (enGrid) {
                binding.btnGrid.setImageResource(R.drawable.menu)
                val orientation = resources.configuration.orientation
                val spanCount = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 6 else 3
                binding.recyclerViewRecetas.layoutManager = GridLayoutManager(requireContext(), spanCount)
            } else {
                binding.btnGrid.setImageResource(android.R.drawable.ic_dialog_dialer)
                binding.recyclerViewRecetas.layoutManager = LinearLayoutManager(requireContext())
            }
            recetaAdapter.cambiarVista(enGrid)
        }

    }

    private fun actualizarLayoutSegunVista() {
        val enGrid = viewModel.enVistaGrid.value ?: false
        val orientation = resources.configuration.orientation
        val spanCount = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 6 else 3

        if (enGrid) {
            binding.btnGrid.setImageResource(R.drawable.menu)
            binding.recyclerViewRecetas.layoutManager = GridLayoutManager(requireContext(), spanCount)
        } else {
            binding.btnGrid.setImageResource(android.R.drawable.ic_dialog_dialer)
            binding.recyclerViewRecetas.layoutManager = LinearLayoutManager(requireContext())
        }

        if (::recetaAdapter.isInitialized) {
            recetaAdapter.cambiarVista(enGrid)
        }
    }

    private fun cargarDatosCompletos() {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: return

        lifecycleScope.launch {
            try {
                val usuario = repository.consultaUsuarioPorCorreo(email)
                idUsuarioActual = usuario.id_usuario

                val guardadas = repository.obtenerRecetasGuardadas(idUsuarioActual!!)
                recetasGuardadas.clear()
                recetasGuardadas.addAll(guardadas.map { it.id_receta })

                val recetas = repository.consultaTodasRecetas()

                recetaAdapter = RecetaAdapter(
                    recetas.toMutableList(),
                    viewModel.enVistaGrid.value ?: false,
                    recetasGuardadas,
                    idUsuarioActual,
                    usuario.usuario,  // <--- PASAR NOMBRE DEL AUTOR AQUÍ
                    onGuardarClick = { receta, estaGuardada ->
                        onGuardarClick(receta, estaGuardada)
                        true
                    },
                    onVerClick = { receta ->
                        lifecycleScope.launch {
                            try {
                                val recetaActualizada = repository.consultaRecetaPorId(receta.id_receta)
                                val action = InicioFragmentDirections.actionInicioFragmentToVerRecetaFragment(recetaActualizada.id_receta, "inicio")
                                findNavController().navigate(action)
                            } catch (e: Exception) {
                                // Elimina del RecyclerView si ya no existe
                                val index = recetaAdapter.recetas.indexOfFirst { it.id_receta == receta.id_receta }
                                if (index != -1) {
                                    recetaAdapter.recetas.removeAt(index)
                                    recetaAdapter.notifyItemRemoved(index)
                                    Toast.makeText(requireContext(), "La receta ya no está disponible y se ha eliminado de la lista", Toast.LENGTH_SHORT).show()
                                }
                                Log.e("InicioFragment", "Receta eliminada remotamente: ${e.message}")
                            }
                        }
                    }


                )

                binding.recyclerViewRecetas.adapter = recetaAdapter
                actualizarLayoutSegunVista()

            } catch (e: Exception) {
                Log.e("InicioFragment", "Error al cargar datos: ${e.message}")
                Toast.makeText(requireContext(), "Error al cargar recetas", Toast.LENGTH_LONG).show()
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
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar guardados", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    private fun inicializarBotones() {
        binding.fabAgregar.setOnClickListener {
            findNavController().navigate(R.id.action_inicioFragment_to_creaRecetaFragment)
        }

        binding.btnGrid.setOnClickListener {
            viewModel.toggleVista()
        }

        binding.bottomNav.selectedItemId = R.id.nav_home
        binding.bottomNav.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_search -> {
                    findNavController().navigate(R.id.action_inicioFragment_to_busquedaFragment)
                    true
                }
                R.id.nav_book -> {
                    findNavController().navigate(R.id.action_inicioFragment_to_guardadosFragment)
                    true
                }
                R.id.nav_profile -> {
                    findNavController().navigate(R.id.action_inicioFragment_to_cuentaFragment)
                    true
                }
                else -> false
            }
        })
    }
}
