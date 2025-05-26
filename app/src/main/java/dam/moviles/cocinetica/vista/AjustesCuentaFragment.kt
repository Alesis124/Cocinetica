package dam.moviles.cocinetica.vista

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentAjustesCuentaBinding
import dam.moviles.cocinetica.modelo.CocineticaRepository
import kotlinx.coroutines.launch

class AjustesCuentaFragment : Fragment() {

    lateinit var binding: FragmentAjustesCuentaBinding
    private val repository = CocineticaRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        inicializarBinding()
        cargarDatosUsuario()
        inicializarBotones()
        return binding.root
    }

    fun inicializarBinding(){
        binding= FragmentAjustesCuentaBinding.inflate(layoutInflater)
    }

    private fun cargarDatosUsuario() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val email = currentUser?.email ?: return

        lifecycleScope.launch {
            try {
                val usuario = repository.consultaUsuarioPorCorreo(email)
                binding.nombreEdit.setText(usuario.usuario)
                binding.descripcionEdit.setText(usuario.descripcion)
            } catch (e: Exception) {
                Log.e("AjustesCuenta", "Error al cargar datos: ${e.localizedMessage}")
            }
        }
    }

    fun inicializarBotones(){
        binding.btnHecho.setOnClickListener {
            val nombre = binding.nombreEdit.text.toString().trim()
            val descripcion = binding.descripcionEdit.text.toString().trim()

            val currentUser = FirebaseAuth.getInstance().currentUser
            val email = currentUser?.email ?: return@setOnClickListener

            lifecycleScope.launch {
                try {
                    val usuarios = repository.consultaTodosUsuarios()
                    val usuarioActual = usuarios.find { it.correo.equals(email, ignoreCase = true) }

                    if (usuarioActual != null) {
                        val actualizado = usuarioActual.copy(
                            usuario = nombre,
                            descripcion = descripcion
                        )
                        val response = repository.actualizarUsuario(actualizado)

                        if (response.isSuccessful) {
                            findNavController().navigate(R.id.action_ajustesCuentaFragment_to_cuentaFragment)
                        } else {
                            Log.e("AjustesCuenta", "Error al actualizar: ${response.errorBody()?.string()}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AjustesCuenta", "Error: ${e.localizedMessage}")
                }
            }
        }

        binding.EliminarCuentabtn.setOnClickListener {
            binding.EliminarCuentabtn.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Confirmar eliminación")
                    .setMessage("¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.")
                    .setPositiveButton("Sí") { _, _ ->
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val email = currentUser?.email ?: return@setPositiveButton

                        lifecycleScope.launch {
                            try {
                                val usuarios = repository.consultaTodosUsuarios()
                                val usuario = usuarios.find { it.correo.equals(email, ignoreCase = true) }

                                if (usuario != null) {
                                    val response = repository.eliminarUsuario(usuario.id_usuario)
                                    if (response.isSuccessful) {
                                        currentUser.delete().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                findNavController().navigate(R.id.action_ajustesCuentaFragment_to_loginFragment)
                                            } else {
                                                Log.e("EliminarCuenta", "Error al eliminar de FirebaseAuth: ${task.exception}")
                                            }
                                        }
                                    } else {
                                        Log.e("EliminarCuenta", "Error al eliminar de la API: ${response.errorBody()?.string()}")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("EliminarCuenta", "Error: ${e.localizedMessage}")
                            }
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            }

        }

        binding.btnVolver.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.CerrarSesionbtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.action_ajustesCuentaFragment_to_loginFragment)
        }
        binding.cambiarContraseAbtn.setOnClickListener {
            findNavController().navigate(R.id.action_ajustesCuentaFragment_to_noRecuerdoFragment)
        }

    }
}