package dam.moviles.cocinetica.vista

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationBarView
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentInicioBinding


class InicioFragment : Fragment() {

    lateinit var binding: FragmentInicioBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        inicializarBinding()
        binding.bottomNav.selectedItemId = R.id.nav_home
        inicializarBotones()
        return binding.root
    }

    fun inicializarBinding(){
        binding= FragmentInicioBinding.inflate(layoutInflater)
    }

    private fun inicializarBotones() {
        binding.fabAgregar.setOnClickListener {
            findNavController().navigate(R.id.action_inicioFragment_to_creaRecetaFragment)
        }
        binding.btnGrid.setOnClickListener {
            // Cambia la forma de ver el RecyclerView
        }
        binding.bottomNav.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // refresh
                    true
                }
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