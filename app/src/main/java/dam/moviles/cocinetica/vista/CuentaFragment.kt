package dam.moviles.cocinetica.vista

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationBarView
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentCuentaBinding
import dam.moviles.cocinetica.modelo.CocineticaRepository
import dam.moviles.cocinetica.modelo.Comentario
import dam.moviles.cocinetica.modelo.Valoracion
import dam.moviles.cocinetica.viewModel.CuentaViewModel
import kotlinx.coroutines.launch

class CuentaFragment : Fragment() {

    private lateinit var binding: FragmentCuentaBinding
    private val cuentaViewModel: CuentaViewModel by viewModels()

    private var comentariosCargados: List<Comentario>? = null
    private var valoracionesCargadas: Map<Int, Valoracion>? = null
    private lateinit var recetaAdapter: RecetaAdapter
    private val repository = CocineticaRepository()


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

    override fun onResume() {
        super.onResume()
        observarViewModel()
    }

    private fun observarViewModel() {
        cuentaViewModel.usuario.observe(viewLifecycleOwner) { usuario ->
            binding.txtNombre.text = usuario.usuario
            binding.txtDescripciN.text = usuario.descripcion

            cuentaViewModel.cargarMisRecetas(usuario.id_usuario)

            recetaAdapter = RecetaAdapter(
                recetas = mutableListOf(),
                enVistaGrid = false,
                recetasGuardadas = mutableSetOf(),
                idUsuario = usuario.id_usuario,
                nombreAutor = usuario.usuario,
                onGuardarClick = { receta, estabaGuardada -> true },
                onVerClick = { receta ->
                    lifecycleScope.launch {
                        try {
                            val recetaActualizada = repository.consultaRecetaPorId(receta.id_receta)
                            val action = InicioFragmentDirections.actionInicioFragmentToVerRecetaFragment(recetaActualizada.id_receta, origen = "cuenta")
                            findNavController().navigate(action)
                        } catch (e: Exception) {
                            // Elimina del RecyclerView si ya no existe
                            val index = recetaAdapter.recetas.indexOfFirst { it.id_receta == receta.id_receta }
                            if (index != -1) {
                                recetaAdapter.recetas.removeAt(index)
                                recetaAdapter.notifyItemRemoved(index)
                                Toast.makeText(requireContext(), "La receta ya no está disponible y se ha eliminado de la lista", Toast.LENGTH_SHORT).show()
                                binding.txtNoRecetas.visibility = View.VISIBLE
                                binding.misRecetas.visibility = View.GONE
                            }
                            Log.e("InicioFragment", "Receta eliminada remotamente: ${e.message}")
                        }
                    }
                }
            )

            binding.misRecetas.layoutManager = LinearLayoutManager(requireContext())
            binding.misRecetas.adapter = recetaAdapter
        }

        cuentaViewModel.recetas.observe(viewLifecycleOwner) { recetas ->
            if (::recetaAdapter.isInitialized) {
                recetaAdapter.actualizarRecetas(recetas)

                // Mostrar u ocultar mensaje de no recetas
                if (recetas.isEmpty()) {
                    binding.txtNoRecetas.visibility = View.VISIBLE
                    binding.misRecetas.visibility = View.GONE
                } else {
                    binding.txtNoRecetas.visibility = View.GONE
                    binding.misRecetas.visibility = View.VISIBLE
                }
            }
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

        // Mostrar u ocultar mensaje de no comentarios
        if (comentariosMios.isEmpty()) {
            binding.txtNoComentarios.visibility = View.VISIBLE
            binding.misComentarios.visibility = View.GONE
        } else {
            binding.txtNoComentarios.visibility = View.GONE
            binding.misComentarios.visibility = View.VISIBLE
        }

        val adapter = ComentarioUsuarioAdapter(
            comentarios = comentariosMios,
            valoracionesMap = valoraciones,
            nombreUsuario = usuario.usuario,
            onEliminarClick = { comentario ->
                cuentaViewModel.eliminarComentario(comentario)
            },
            onIrClick = { idReceta ->
                val action = CuentaFragmentDirections.actionCuentaFragmentToVerRecetaFragment3(idReceta, origen = "cuenta")
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
