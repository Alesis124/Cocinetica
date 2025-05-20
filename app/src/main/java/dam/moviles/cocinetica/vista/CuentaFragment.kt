package dam.moviles.cocinetica.vista

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TabHost
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentCuentaBinding
import dam.moviles.cocinetica.modelo.CocineticaRepository
import kotlinx.coroutines.launch


class CuentaFragment : Fragment() {

    lateinit var binding: FragmentCuentaBinding
    private val repository = CocineticaRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        inicializarBinding()
        binding.bottomNav.selectedItemId = R.id.nav_profile
        configurarTabs()
        inicializarBotones()
        cargarDatosUsuario()
        return binding.root
    }

    fun inicializarBinding(){
        binding = FragmentCuentaBinding.inflate(layoutInflater)
    }

    private fun cargarDatosUsuario() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val email = currentUser?.email ?: return

        lifecycleScope.launch {
            try {
                val usuarios = repository.consultaTodosUsuarios()
                val usuario = usuarios.find { it.correo.equals(email, ignoreCase = true) }

                if (usuario != null) {
                    binding.txtNombre.text = usuario.usuario
                    binding.txtDescripciN.text = usuario.descripcion
                } else {
                    Log.e("CuentaFragment", "No se encontró el usuario con el correo $email")
                }
            } catch (e: Exception) {
                Log.e("CuentaFragment", "Error al cargar datos del usuario: ${e.localizedMessage}")
            }
        }
    }

    private fun configurarTabs() {
        val tabHost: TabHost = binding.navegadorRecetasCometarios
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
                    // refresh
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
                R.id.nav_profile -> {
                    //refesh
                    true
                }
                else -> false
            }
        })
        binding.btnEditarCuenta.setOnClickListener {
            findNavController().navigate(R.id.action_cuentaFragment_to_ajustesCuentaFragment)
        }
        binding.btnAyuda.setOnClickListener {
            //findNavController().navigate(R.id.action_cuentaFragment_to_ayudaFragment)
        }

    }

}