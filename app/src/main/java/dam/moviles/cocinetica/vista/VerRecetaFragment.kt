package dam.moviles.cocinetica.vista

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentVerRecetaBinding
import dam.moviles.cocinetica.modelo.CocineticaRepository
import kotlinx.coroutines.launch

class VerRecetaFragment : Fragment() {

    private lateinit var binding: FragmentVerRecetaBinding
    private val args: VerRecetaFragmentArgs by navArgs()
    private val repository = CocineticaRepository()

    private var idUsuarioActual: Int? = null
    private var recetaGuardada: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        inicializarBinding()
        inicializarBotones()
        cargarDatos(args.idReceta)
        return binding.root
    }

    fun inicializarBinding(){
        binding= FragmentVerRecetaBinding.inflate(layoutInflater)
    }


    private fun cargarDatos(idReceta: Int) {
        lifecycleScope.launch {
            try {
                // Obtener usuario actual (email y id)
                val email = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: return@launch
                val usuario = repository.consultaUsuarioPorCorreo(email)
                idUsuarioActual = usuario.id_usuario

                // Cargar receta
                val receta = repository.consultaRecetaPorId(idReceta)

                // Mostrar datos en UI
                binding.txtNombreReceta.text = receta.nombre
                binding.descripcionReceta.text = "Hecho por ${receta.usuario}"
                binding.tiempotxt.text = "${receta.duracion} minutos"
                binding.ratingBar.rating = receta.valoracion.toFloat()

                // Consultar si está guardada
                val recetasGuardadas = repository.obtenerRecetasGuardadas(idUsuarioActual!!)
                recetaGuardada = recetasGuardadas.any { it.id_receta == idReceta }

                actualizarTextoBoton()

                // Si usas Glide para imagen:
                // Glide.with(this@VerRecetaFragment).load(receta.imagen).into(binding.imagenReceta)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar la receta", Toast.LENGTH_SHORT).show()
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
    }



}