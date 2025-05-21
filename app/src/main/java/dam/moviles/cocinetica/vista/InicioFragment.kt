package dam.moviles.cocinetica.vista

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationBarView
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentInicioBinding
import dam.moviles.cocinetica.modelo.CocineticaRepository
import dam.moviles.cocinetica.viewModel.InicioFragmenViewModel
import dam.moviles.cocinetica.viewModel.RegisterFragmentViewModel
import kotlinx.coroutines.launch

class InicioFragment : Fragment() {

    private lateinit var binding: FragmentInicioBinding
    lateinit var viewModel: InicioFragmenViewModel
    private lateinit var recetaAdapter: RecetaAdapter

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
        inicializarRecyclerView()
        inicializarBotones()
        testTodosUsuarios()
    }

    private fun inicializarViewModel() {
        viewModel = ViewModelProvider(this).get(InicioFragmenViewModel::class.java)

        viewModel.enVistaGrid.observe(viewLifecycleOwner) { enGrid ->
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

    private fun inicializarRecyclerView() {
        recetaAdapter = RecetaAdapter(emptyList(), false) // empieza en lista por defecto
        binding.recyclerViewRecetas.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewRecetas.adapter = recetaAdapter

        lifecycleScope.launch {
            try {
                val recetas = CocineticaRepository().consultaTodasRecetas()
                recetaAdapter.actualizarRecetas(recetas)
            } catch (e: Exception) {
                Log.e("API", "Error cargando recetas: ${e.message}")
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

    private fun testTodosUsuarios() {
        lifecycleScope.launch {
            try {
                CocineticaRepository()
                    .consultaTodosUsuarios()
                    .forEach { usuario -> Log.d("Usuario", usuario.toString()) }
            } catch (e: Exception) {
                Log.d("Error", e.toString())
            }
        }
    }
}
