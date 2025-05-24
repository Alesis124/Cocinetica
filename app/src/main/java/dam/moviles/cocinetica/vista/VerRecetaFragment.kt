package dam.moviles.cocinetica.vista

import ComentariosAdapter
import IngredienteAdapter
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentVerRecetaBinding
import dam.moviles.cocinetica.modelo.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class VerRecetaFragment : Fragment() {

    private lateinit var binding: FragmentVerRecetaBinding
    private val args: VerRecetaFragmentArgs by navArgs()
    private val repository = CocineticaRepository()

    private var idUsuarioActual: Int = -1
    private var recetaGuardada: Boolean = false
    private var idReceta: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVerRecetaBinding.inflate(inflater, container, false)
        idReceta = args.idReceta

        habilitarInteraccion(false)
        cargarDatos()
        configurarBotones()
        return binding.root
    }

    private fun cargarDatos() {
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

                val recetasGuardadas = repository.obtenerRecetasGuardadas(idUsuarioActual)
                recetaGuardada = recetasGuardadas.any { it.id_receta == idReceta }
                actualizarTextoBoton()

                // Ingredientes
                val contieneList = repository.obtenerContienePorReceta(idReceta)
                val todosIngredientes = repository.obtenerIngredientes()
                val ingredienteList = todosIngredientes.filter { ingrediente ->
                    contieneList.any { contiene -> contiene.id_ingrediente == ingrediente.id_ingrediente }
                }
                val unidadMedidaList = repository.obtenerUM()
                binding.reciclerIngrdientes.layoutManager = LinearLayoutManager(requireContext())
                binding.reciclerIngrdientes.adapter = IngredienteAdapter(contieneList, ingredienteList, unidadMedidaList)

                // Pasos
                val pasosList = repository.obtenerPasosPorReceta(idReceta)
                binding.reciclerPasos.layoutManager = LinearLayoutManager(requireContext())
                binding.reciclerPasos.adapter = PasoAdapter(pasosList)

                // Comentarios
                cargarComentarios()

                habilitarInteraccion(true)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar la receta: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun cargarComentarios() {
        lifecycleScope.launch {
            try {
                val comentariosList = repository.obtenerComentariosPorReceta(idReceta)
                val valoracionesMap = repository.obtenerValoracionesComentarios(idReceta)
                val usuariosList = repository.obtenerUsuarios()
                val usuariosMap = usuariosList.associateBy({ it.id_usuario }, { it.usuario })

                binding.recyclerComentarios.layoutManager = LinearLayoutManager(requireContext())
                binding.recyclerComentarios.adapter = ComentariosAdapter(
                    comentariosList,
                    valoracionesMap,
                    usuariosMap,
                    idUsuarioActual,
                    onEliminarClick = { comentario ->
                        mostrarDialogoConfirmacionEliminar(comentario)
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar comentarios", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoConfirmacionEliminar(comentario: Comentario) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar comentario")
            .setMessage("¿Estás seguro de que deseas eliminar este comentario?")
            .setPositiveButton("Sí") { _, _ -> eliminarComentario(comentario.id_comentario) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarComentario(idComentario: Int) {
        lifecycleScope.launch {
            try {
                val response = repository.eliminarComentario(idComentario)
                if (response.isSuccessful && response.body()?.error == null) {
                    Toast.makeText(requireContext(), "Comentario eliminado", Toast.LENGTH_SHORT).show()
                    cargarDatos()
                } else {
                    Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun configurarBotones() {
        binding.btnVolver.setOnClickListener {
            findNavController().navigate(R.id.action_verRecetaFragment_to_inicioFragment)
        }

        binding.btnGuardar.setOnClickListener {
            lifecycleScope.launch {
                val exito = if (recetaGuardada) {
                    repository.eliminarRecetaGuardada(idUsuarioActual, idReceta)
                } else {
                    repository.agregarRecetaGuardada(idUsuarioActual, idReceta)
                }

                if (exito) {
                    recetaGuardada = !recetaGuardada
                    actualizarTextoBoton()
                } else {
                    Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnEnviarComentario.setOnClickListener {
            val texto = binding.editTextComentario.text.toString().trim()
            val nota = binding.ratingBarUsuario.rating.toInt()

            if (texto.isEmpty()) {
                Toast.makeText(requireContext(), "Escribe un comentario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val comentarioRequest = ComentarioRequest(
                texto = texto,
                id_usuario = idUsuarioActual,
                id_receta = idReceta
            )


            lifecycleScope.launch {
                try {
                    val response = repository.insertarComentario(comentarioRequest)
                    if (response.isSuccessful) {
                        val comentarios = repository.obtenerComentariosPorReceta(idReceta)
                        val nuevoComentario = comentarios.lastOrNull {
                            it.id_usuario == idUsuarioActual && it.texto == texto
                        }

                        if (nota > 0 && nuevoComentario != null) {
                            val exito = insertarOActualizarValoracionExistente(
                                idUsuarioActual,
                                idReceta,
                                nota,
                                nuevoComentario.id_comentario
                            )
                            if (exito) {
                                Toast.makeText(context, "Comentario y valoración enviados", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Comentario enviado, error al valorar", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Comentario enviado", Toast.LENGTH_SHORT).show()
                        }

                        binding.editTextComentario.setText("")
                        binding.ratingBarUsuario.rating = 0f
                        cargarDatos()
                    } else {
                        Toast.makeText(context, "Error al comentar", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun actualizarTextoBoton() {
        binding.btnGuardar.text = if (recetaGuardada) "Quitar de guardados" else "Guardar"
    }

    private fun habilitarInteraccion(habilitar: Boolean) {
        binding.btnGuardar.isEnabled = habilitar
        binding.btnVolver.isEnabled = habilitar
        binding.btnValorar.isEnabled = habilitar
        binding.ratingBarUsuario.isEnabled = habilitar
        binding.editTextComentario.isEnabled = habilitar
        binding.btnEnviarComentario.isEnabled = habilitar
    }

    private suspend fun insertarOActualizarValoracionExistente(
        idUsuario: Int,
        idReceta: Int,
        valoracionInt: Int,
        idComentario: Int?
    ): Boolean {
        val todasValoraciones = repository.leerValoraciones()
        val existente = todasValoraciones.find { it.id_usuario == idUsuario && it.id_receta == idReceta }

        return if (existente != null) {
            val json = JSONObject().apply {
                put("tabla", "Valoraciones")
                put("id_valoracion", existente.id_valoracion)
                put("valoracion", valoracionInt)
                put("id_comentario", idComentario)
            }

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val response = repository.cocineticaApi.actualizarValoracion(body)
            response.isSuccessful
        } else {
            val json = JSONObject().apply {
                put("tabla", "Valoraciones")
                put("id_usuario", idUsuario)
                put("id_receta", idReceta)
                put("valoracion", valoracionInt)
                if (idComentario != null) put("id_comentario", idComentario)
            }

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val response = repository.cocineticaApi.insertarGenerico(body)
            response.isSuccessful
        }
    }
}
