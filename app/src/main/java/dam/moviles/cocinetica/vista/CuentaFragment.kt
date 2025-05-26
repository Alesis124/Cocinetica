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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationBarView
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentCuentaBinding
import dam.moviles.cocinetica.modelo.Comentario
import dam.moviles.cocinetica.modelo.Valoracion
import dam.moviles.cocinetica.viewModel.CuentaViewModel

class CuentaFragment : Fragment() {

    private lateinit var binding: FragmentCuentaBinding
    private val cuentaViewModel: CuentaViewModel by viewModels()

    private var comentariosCargados: List<Comentario>? = null
    private var valoracionesCargadas: Map<Int, Valoracion>? = null


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

        cuentaViewModel.comentarios.observe(viewLifecycleOwner) { comentarios ->
            val comentariosMios = comentarios.filter { it.id_usuario == cuentaViewModel.usuario.value?.id_usuario }

            Log.d("CuentaFragment", "Comentarios del usuario: $comentariosMios")
            Log.d("CuentaFragment", "Valoraciones actuales: ${cuentaViewModel.valoraciones.value}")

            val adapter = ComentarioUsuarioAdapter(
                comentarios = comentariosMios,
                valoracionesMap = cuentaViewModel.valoraciones.value ?: emptyMap(),
                nombreUsuario = cuentaViewModel.usuario.value?.usuario ?: "Yo",
                onEliminarClick = { comentario ->
                    cuentaViewModel.eliminarComentario(comentario)
                },
                onIrClick = { idReceta ->
                    val action = CuentaFragmentDirections.actionCuentaFragmentToVerRecetaFragment3(idReceta)
                    findNavController().navigate(action)
                }
            )

            binding.misComentarios.layoutManager = LinearLayoutManager(requireContext())
            binding.misComentarios.adapter = adapter

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

        val adapter = ComentarioUsuarioAdapter(
            comentarios = comentariosMios,
            valoracionesMap = valoraciones,
            nombreUsuario = usuario.usuario,
            onEliminarClick = { comentario ->
                cuentaViewModel.eliminarComentario(comentario)
            },
            onIrClick = { idReceta ->
                val action = CuentaFragmentDirections.actionCuentaFragmentToVerRecetaFragment3(idReceta)
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
            // Navegación opcional
        }
    }
}
