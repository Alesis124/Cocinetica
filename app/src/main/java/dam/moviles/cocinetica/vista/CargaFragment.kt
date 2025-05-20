package dam.moviles.cocinetica.vista

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentCargaBinding

class CargaFragment : Fragment() {

    lateinit var binding: FragmentCargaBinding
    private lateinit var auth: FirebaseAuth

    private val handler = Handler(Looper.getMainLooper())
    private val checkDelay: Long = 3000 // 3 segundos

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        inicializarBinding()
        auth = FirebaseAuth.getInstance()
        comprobarVerificacionEmail()
        return binding.root
    }

    fun inicializarBinding(){
        binding= FragmentCargaBinding.inflate(layoutInflater)
    }

    private fun comprobarVerificacionEmail() {
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(context, "No hay usuario logueado", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_cargaFragment_to_loginFragment)
            return
        }

        user.reload().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Tras reload, si user sigue siendo null o no existe
                val refreshedUser = auth.currentUser
                if (refreshedUser == null) {
                    Toast.makeText(context, "Usuario no encontrado, redirigiendo a login", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_cargaFragment_to_loginFragment)
                    return@addOnCompleteListener
                }

                if (refreshedUser.isEmailVerified) {
                    Toast.makeText(context, "Email verificado, bienvenido!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_cargaFragment_to_inicioFragment)
                } else {
                    Toast.makeText(context, "Aún no has verificado tu correo, esperando...", Toast.LENGTH_SHORT).show()
                    handler.postDelayed({ comprobarVerificacionEmail() }, checkDelay)
                }
            } else {
                Toast.makeText(context, "Error comprobando usuario: ${task.exception?.message}. Volviendo a login.", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_cargaFragment_to_loginFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)  // Evita leaks cuando se destruye la vista
    }

}