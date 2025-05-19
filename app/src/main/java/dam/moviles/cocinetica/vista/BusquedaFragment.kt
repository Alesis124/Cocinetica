package dam.moviles.cocinetica.vista

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationBarView
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentBusquedaBinding


class BusquedaFragment : Fragment() {

    lateinit var binding: FragmentBusquedaBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        inicializarBinding()
        binding.bottomNav.selectedItemId = R.id.nav_search
        inicializarBotones()
        return binding.root
    }

    fun inicializarBinding(){
        binding= FragmentBusquedaBinding.inflate(layoutInflater)
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
                R.id.nav_search -> {
                    // refresh
                    true
                }
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

    }

}