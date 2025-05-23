package dam.moviles.cocinetica.vista

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationBarView
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentCuentaBinding
import dam.moviles.cocinetica.viewModel.CuentaViewModel

class CuentaFragment : Fragment() {

    private lateinit var binding: FragmentCuentaBinding
    private val cuentaViewModel: CuentaViewModel by viewModels()

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

    private fun observarViewModel() {
        cuentaViewModel.usuario.observe(viewLifecycleOwner, Observer { usuario ->
            binding.txtNombre.text = usuario.usuario
            binding.txtDescripciN.text = usuario.descripcion
        })

        cuentaViewModel.recetas.observe(viewLifecycleOwner, Observer { recetas ->
            // Actualiza UI relacionada a recetas (por ejemplo, un RecyclerView dentro de la pestaña Recetas)
        })

        cuentaViewModel.comentarios.observe(viewLifecycleOwner, Observer { comentarios ->
            // Actualiza UI relacionada a comentarios
        })
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
            // Navegación opcional
        }
    }
}
