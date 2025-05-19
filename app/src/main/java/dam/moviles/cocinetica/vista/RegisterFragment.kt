package dam.moviles.cocinetica.vista

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentRegisterBinding


class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val RC_SIGN_IN = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inicializarBinding()
        auth = FirebaseAuth.getInstance()
        inicializarBoton()
        configurarGoogleSignIn()
        inicializarTextoIniciarSesion()
        return binding.root
    }

    private fun inicializarBinding(){
        binding = FragmentRegisterBinding.inflate(layoutInflater)
    }

    private fun configurarGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))  // Aquí debe ir tu web client ID
            .requestEmail()
            .build()


        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }


    // Para manejar el resultado de Google Sign-In
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(context, "Google sign-in falló: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Autenticación con Google exitosa", Toast.LENGTH_SHORT).show()
                    // Navega a la pantalla principal o carga
                    findNavController().navigate(R.id.action_registerFragment_to_cargaFragment)
                } else {
                    Toast.makeText(context, "Error autenticando con Google: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun inicializarBoton(){
        binding.btnCrearCuenta.setOnClickListener {
            registrarUsuario()
        }
    }

    private fun inicializarTextoIniciarSesion() {
        binding.volverInicioSesion.setOnClickListener {
            findNavController().navigate(R.id.action_respuesta_registerFragment_to_contraseniaFragment)
        }
    }

    private fun registrarUsuario() {
        val nombre = binding.nombretxt.text.toString().trim()
        val email = binding.correotxtRegister.text.toString().trim()
        val password = binding.contrseAtxtRegister.text.toString().trim()

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // Opcional: actualizar el perfil con el nombre
                    // val profileUpdates = userProfileChangeRequest { displayName = nombre }
                    // user?.updateProfile(profileUpdates)

                    user?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
                        if (verifyTask.isSuccessful) {
                            Toast.makeText(context, "Correo de verificación enviado", Toast.LENGTH_SHORT).show()
                            // Navegar a fragment de carga para verificar email
                            findNavController().navigate(R.id.action_registerFragment_to_cargaFragment)
                        } else {
                            Toast.makeText(context, "Error enviando correo: ${verifyTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }


}
