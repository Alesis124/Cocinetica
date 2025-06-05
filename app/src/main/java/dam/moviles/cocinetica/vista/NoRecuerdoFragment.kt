package dam.moviles.cocinetica.vista

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentNoRecuerdoBinding


class NoRecuerdoFragment : Fragment() {

    private var _binding: FragmentNoRecuerdoBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoRecuerdoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        inicializarBotones()

        // Restaurar el texto del email si hubo rotación
        savedInstanceState?.let {
            binding.emailEditText.setText(it.getString("email", ""))
        }
    }

    private fun inicializarBotones() {
        binding.buttonVolver.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.changePasswordButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(context, "Introduce tu correo electrónico", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Correo de recuperación enviado", Toast.LENGTH_LONG).show()
                        findNavController().navigate(R.id.action_noRecuerdoFragment_to_loginFragment)
                    } else {
                        Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("email", binding.emailEditText.text.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}