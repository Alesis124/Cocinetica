package dam.moviles.cocinetica.vista

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationBarView
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentBusquedaBinding
import dam.moviles.cocinetica.modelo.CocineticaRepository
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController


class BusquedaFragment : Fragment() {

    lateinit var binding: FragmentBusquedaBinding
    private val repository = CocineticaRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inicializarBinding(inflater, container)
        binding.bottomNav.selectedItemId = R.id.nav_search
        inicializarBotones()
        return binding.root
    }

    fun inicializarBinding(inflater: LayoutInflater, container: ViewGroup?) {
        binding = FragmentBusquedaBinding.inflate(inflater, container, false)
    }


    private fun inicializarBotones() {
        binding.fabAgregar.setOnClickListener {
            findNavController().navigate(R.id.action_busquedaFragment_to_creaRecetaFragment)
        }

        binding.bottomNav.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    findNavController().navigate(R.id.action_busquedaFragment_to_inicioFragment)
                    true
                }
                R.id.nav_search -> true
                R.id.nav_book -> {
                    findNavController().navigate(R.id.action_busquedaFragment_to_guardadosFragment)
                    true
                }
                R.id.nav_profile -> {
                    findNavController().navigate(R.id.action_busquedaFragment_to_cuentaFragment)
                    true
                }
                else -> false
            }
        })

        binding.btnBuscar.setOnClickListener {
            val textoBusqueda = binding.etBusqueda.text.toString().trim()
            if (textoBusqueda.isNotEmpty()) {
                buscarYNavegar(textoBusqueda)
            }
        }

        binding.etBusqueda.setOnEditorActionListener { _, _, _ ->
            val textoBusqueda = binding.etBusqueda.text.toString().trim()
            if (textoBusqueda.isNotEmpty()) {
                buscarYNavegar(textoBusqueda)
                true
            } else {
                false
            }
        }
    }

    private fun buscarYNavegar(texto: String) {
        lifecycleScope.launch {
            try {
                val recetas = repository.buscarRecetas(texto)
                if (recetas.isNotEmpty()) {
                    val idsRecetas = recetas.map { it.id_receta }.toIntArray()
                    val action = BusquedaFragmentDirections
                        .actionBusquedaFragmentToResultadoBusquedaFragment(idsRecetas)
                    findNavController().navigate(action)
                } else {
                    // Aquí podrías mostrar un Toast o mensaje que no hay resultados
                }
            } catch (e: Exception) {
                // Aquí podrías mostrar un mensaje de error
            }
        }
    }
}
