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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationBarView
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentGuardadosBinding
import dam.moviles.cocinetica.modelo.CocineticaRepository
import dam.moviles.cocinetica.modelo.Receta
import dam.moviles.cocinetica.viewModel.GuardadosFragmentViewModel
import kotlinx.coroutines.launch

class GuardadosFragment : Fragment() {

    private lateinit var binding: FragmentGuardadosBinding
    private lateinit var viewModel: GuardadosFragmentViewModel
    private lateinit var recetaAdapter: RecetaAdapter
    private val repository = CocineticaRepository()
    private var idUsuarioActual: Int? = null
    private val args: GuardadosFragmentArgs by navArgs()

    // Lista mutable de recetas para mostrar en el adapter
    private val recetas = mutableListOf<Receta>()

    // Conjunto de ids de recetas guardadas
    private val recetasGuardadas = mutableSetOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGuardadosBinding.inflate(inflater, container, false)
        inicializarViewModel()
        inicializarBotones()
        return binding.root
    }

    private fun inicializarViewModel() {
        viewModel = ViewModelProvider(this)[GuardadosFragmentViewModel::class.java]

        viewModel.enVistaGrid.observe(viewLifecycleOwner) { enGrid ->
            if (!::recetaAdapter.isInitialized) return@observe

            if (enGrid) {
                binding.btnGrid.setImageResource(R.drawable.menu)
                val orientation = resources.configuration.orientation
                val spanCount = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 6 else 3
                binding.recyclerViewGuardados.layoutManager = GridLayoutManager(requireContext(), spanCount)
            } else {
                binding.btnGrid.setImageResource(android.R.drawable.ic_dialog_dialer)
                binding.recyclerViewGuardados.layoutManager = LinearLayoutManager(requireContext())
            }
            recetaAdapter.cambiarVista(enGrid)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarDatosGuardados()
    }

    private fun cargarDatosGuardados() {
        val email = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: return

        lifecycleScope.launch {
            try {
                val usuario = repository.consultaUsuarioPorCorreo(email)
                idUsuarioActual = usuario.id_usuario

                val guardadas = repository.obtenerRecetasGuardadas(idUsuarioActual!!)
                recetasGuardadas.clear()
                recetasGuardadas.addAll(guardadas.map { it.id_receta })

                recetas.clear()
                recetas.addAll(guardadas)

                recetaAdapter = RecetaAdapter(
                    recetas,
                    viewModel.enVistaGrid.value ?: false,
                    recetasGuardadas,
                    idUsuarioActual,
                    usuario.usuario,
                    onGuardarClick = { receta, estaGuardada ->
                        onGuardarClick(receta, estaGuardada)
                        true
                    },
                    onVerClick = { receta ->
                        lifecycleScope.launch {
                            try {
                                val recetaActualizada = repository.consultaRecetaPorId(receta.id_receta)

                                // Navegación desde GuardadosFragment, no InicioFragment
                                val action = GuardadosFragmentDirections
                                    .actionGuardadosFragmentToVerRecetaFragment(recetaActualizada.id_receta, "guardados")
                                findNavController().navigate(action)

                            } catch (e: Exception) {
                                Log.e("GuardadosFragment", "Receta no encontrada o eliminada: ${e.message}")
                                val index = recetas.indexOfFirst { it.id_receta == receta.id_receta }
                                if (index != -1) {
                                    recetas.removeAt(index)
                                    recetaAdapter.notifyItemRemoved(index)
                                    Toast.makeText(requireContext(), "La receta ya no está disponible", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    onEliminarClick = { idReceta ->
                        confirmarYEliminarReceta(idReceta)
                    }

                )

                binding.recyclerViewGuardados.adapter = recetaAdapter
                actualizarLayoutSegunVista()

            } catch (e: Exception) {
                Log.e("GuardadosFragment", "Error al cargar guardados: ${e.message}")
                Toast.makeText(requireContext(), "Error al cargar recetas guardadas", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun actualizarLayoutSegunVista() {
        val enGrid = viewModel.enVistaGrid.value ?: false
        val orientation = resources.configuration.orientation
        val spanCount = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 6 else 3

        if (enGrid) {
            binding.btnGrid.setImageResource(R.drawable.menu)
            binding.recyclerViewGuardados.layoutManager = GridLayoutManager(requireContext(), spanCount)
        } else {
            binding.btnGrid.setImageResource(android.R.drawable.ic_dialog_dialer)
            binding.recyclerViewGuardados.layoutManager = LinearLayoutManager(requireContext())
        }

        if (::recetaAdapter.isInitialized) {
            recetaAdapter.cambiarVista(enGrid)
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
                        // Quitar receta de guardadas y también del listado
                        recetasGuardadas.remove(receta.id_receta)

                        val index = recetas.indexOfFirst { it.id_receta == receta.id_receta }
                        if (index != -1) {
                            recetas.removeAt(index)
                            recetaAdapter.notifyItemRemoved(index)
                        }
                    } else {
                        recetasGuardadas.add(receta.id_receta)
                        // Si quieres añadir receta al listado, hazlo aquí
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar guardados", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun inicializarBotones() {
        binding.fabAgregar.setOnClickListener {
            findNavController().navigate(R.id.action_guardadosFragment_to_creaRecetaFragment)
        }

        binding.btnGrid.setOnClickListener {
            viewModel.toggleVista()
        }

        // Marcar que estamos en "guardados"
        binding.bottomNav.selectedItemId = R.id.nav_book

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    findNavController().navigate(R.id.action_guardadosFragment_to_inicioFragment)
                    true
                }
                R.id.nav_search -> {
                    findNavController().navigate(R.id.action_guardadosFragment_to_busquedaFragment)
                    true
                }
                R.id.nav_book -> true // Ya estás en guardados, no hace nada
                R.id.nav_profile -> {
                    val action = GuardadosFragmentDirections.actionGuardadosFragmentToCuentaFragment(args.tab)
                    findNavController().navigate(action)
                    true
                }
                else -> false
            }
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