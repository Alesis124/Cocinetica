package dam.moviles.cocinetica.vista

import ComentariosAdapter
import IngredienteAdapter
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
import dam.moviles.cocinetica.modelo.Comentario
import dam.moviles.cocinetica.modelo.ComentarioRequest
import kotlinx.coroutines.launch
import java.time.LocalDate

class VerRecetaFragment : Fragment() {

    private lateinit var binding: FragmentVerRecetaBinding
    private val args: VerRecetaFragmentArgs by navArgs()
    private val repository = CocineticaRepository()

    private var idUsuarioActual: Int? = null
    private var recetaGuardada: Boolean = false

    private lateinit var recyclerIngredientes: RecyclerView
    private lateinit var recyclerPasos: RecyclerView
    private lateinit var recyclerComentarios: RecyclerView

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

                // Ingredientes
                val contieneList = repository.obtenerContienePorReceta(idReceta)
                val todosIngredientes = repository.obtenerIngredientes()
                val ingredienteList = todosIngredientes.filter { ingrediente ->
                    contieneList.any { contiene -> contiene.id_ingrediente == ingrediente.id_ingrediente }
                }
                val unidadMedidaList = repository.obtenerUM()
                recyclerIngredientes = binding.reciclerIngrdientes
                recyclerIngredientes.layoutManager = LinearLayoutManager(requireContext())
                recyclerIngredientes.adapter = IngredienteAdapter(contieneList, ingredienteList, unidadMedidaList)

                // Pasos
                val pasosList = repository.obtenerPasosPorReceta(idReceta)
                recyclerPasos = binding.reciclerPasos
                recyclerPasos.layoutManager = LinearLayoutManager(requireContext())
                recyclerPasos.adapter = PasoAdapter(pasosList)

                // Comentarios
                val comentariosList = repository.obtenerComentariosPorReceta(idReceta)
                val valoracionesMap = repository.obtenerValoracionesComentarios(idReceta) // Map<Int, Valoracion?>
                val usuariosList = repository.obtenerUsuarios()
                val usuariosMap = usuariosList.associateBy({ it.id_usuario }, { it.usuario })

                recyclerComentarios = binding.recyclerComentarios
                recyclerComentarios.layoutManager = LinearLayoutManager(requireContext())
                recyclerComentarios.adapter = ComentariosAdapter(comentariosList, valoracionesMap, usuariosMap)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar la receta: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun mostrarDialogoComentario() {
        val editText = EditText(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle("Nuevo comentario")
            .setView(editText)
            .setPositiveButton("Enviar") { _, _ ->
                val texto = editText.text.toString()
                enviarComentario(texto)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun enviarComentario(texto: String) {
        val idUsuario = idUsuarioActual ?: return
        val idReceta = args.idReceta

        val comentarioRequest = ComentarioRequest(
            texto = texto,
            id_usuario = idUsuario,
            id_receta = idReceta
        )

        lifecycleScope.launch {
            try {
                val response = repository.insertarComentario(comentarioRequest)
                if (response.isSuccessful && response.body()?.error == null) {
                    Toast.makeText(requireContext(), "Comentario agregado", Toast.LENGTH_SHORT).show()
                    cargarDatos(idReceta)
                } else {
                    Toast.makeText(requireContext(), "Error al comentar: ${response.body()?.error ?: response.message()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
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
        binding.btnEnviarComentario.setOnClickListener {
            mostrarDialogoComentario()
        }

    }

}
