package dam.moviles.cocinetica.vista

import IngredienteAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentVerRecetaBinding
import dam.moviles.cocinetica.modelo.CocineticaRepository
import dam.moviles.cocinetica.modelo.Contiene
import dam.moviles.cocinetica.modelo.Ingrediente
import dam.moviles.cocinetica.modelo.UM
import dam.moviles.cocinetica.modelo.Paso
import kotlinx.coroutines.launch

class VerRecetaFragment : Fragment() {

    private lateinit var binding: FragmentVerRecetaBinding
    private val args: VerRecetaFragmentArgs by navArgs()
    private val repository = CocineticaRepository()

    private var idUsuarioActual: Int? = null
    private var recetaGuardada: Boolean = false

    private lateinit var recyclerIngredientes: RecyclerView
    private lateinit var recyclerPasos: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        inicializarBinding()
        inicializarBotones()
        cargarDatos(args.idReceta)
        return binding.root
    }

    private fun inicializarBinding() {
        binding = FragmentVerRecetaBinding.inflate(layoutInflater)
    }

    private fun cargarDatos(idReceta: Int) {
        lifecycleScope.launch {
            try {
                val email = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: return@launch
                val usuario = repository.consultaUsuarioPorCorreo(email)
                idUsuarioActual = usuario.id_usuario

                val receta = repository.consultaRecetaPorId(idReceta)
                binding.txtNombreReceta.text = receta.nombre
                binding.descripcionReceta.text = "Hecho por ${receta.usuario}"
                binding.tiempotxt.text = "${receta.duracion} minutos"
                binding.ratingBar.rating = receta.valoracion.toFloat()

                val recetasGuardadas = repository.obtenerRecetasGuardadas(idUsuarioActual!!)
                recetaGuardada = recetasGuardadas.any { it.id_receta == idReceta }
                actualizarTextoBoton()

                // Obtener contiene (ingredientes usados en la receta)
                val contieneList = repository.obtenerContienePorReceta(idReceta)

                // Obtener todos los ingredientes para filtrar solo los que usa la receta
                val todosIngredientes = repository.obtenerIngredientes()

                // Filtrar ingredientes solo usados en esta receta
                val ingredienteList = todosIngredientes.filter { ingrediente ->
                    contieneList.any { contiene -> contiene.id_ingrediente == ingrediente.id_ingrediente }
                }

                // Obtener unidades de medida
                val unidadMedidaList = repository.obtenerUM()

                // Configurar RecyclerView ingredientes
                recyclerIngredientes = binding.reciclerIngrdientes  // Revisa que el ID esté bien en el XML
                recyclerIngredientes.layoutManager = LinearLayoutManager(requireContext())
                recyclerIngredientes.adapter = IngredienteAdapter(contieneList, ingredienteList, unidadMedidaList)

                // Obtener pasos filtrados por receta
                val pasosList = repository.obtenerPasosPorReceta(idReceta)

                // Configurar RecyclerView pasos
                recyclerPasos = binding.reciclerPasos  // Revisa que el ID esté bien en el XML
                recyclerPasos.layoutManager = LinearLayoutManager(requireContext())
                recyclerPasos.adapter = PasoAdapter(pasosList)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar la receta: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    fun combinarIngredientes(
        contiene: List<Contiene>,
        ingredientes: List<Ingrediente>,
        unidades: List<UM>
    ): List<String> {
        val mapaIngredientes = ingredientes.associateBy { it.id_ingrediente }
        val mapaUM = unidades.associateBy { it.id_um }

        return contiene.mapNotNull { c ->
            val nombreIng = mapaIngredientes[c.id_ingrediente]?.nombre
            val unidad = mapaUM[c.id_um]?.medida
            if (nombreIng != null && unidad != null) {
                "${c.cantidad} $unidad de $nombreIng"
            } else null
        }
    }

    private fun actualizarTextoBoton() {
        binding.btnGuardar.text = if (recetaGuardada) "Quitar de guardados" else "Guardar"
    }

    private fun inicializarBotones() {
        binding.btnVolver.setOnClickListener {
            findNavController().navigate(R.id.action_verRecetaFragment_to_inicioFragment)
        }
        binding.btnGuardar.setOnClickListener {
            lifecycleScope.launch {
                val idUsuario = idUsuarioActual ?: return@launch
                val idReceta = args.idReceta
                val exito = if (recetaGuardada) {
                    repository.eliminarRecetaGuardada(idUsuario, idReceta)
                } else {
                    repository.agregarRecetaGuardada(idUsuario, idReceta)
                }

                if (exito) {
                    recetaGuardada = !recetaGuardada
                    actualizarTextoBoton()
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar guardados", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
