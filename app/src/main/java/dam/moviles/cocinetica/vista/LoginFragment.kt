package dam.moviles.cocinetica.vista

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentLoginBinding
import dam.moviles.cocinetica.modelo.CocineticaRepository
import dam.moviles.cocinetica.modelo.UsuarioInsertar
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val PREFS_NAME = "prefs_usuario"
    private val KEY_RECORDAR = "recordar_usuario"
    private val RC_SIGN_IN = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inicializarBinding()
        auth = FirebaseAuth.getInstance()
        configurarGoogleSignIn()
        inicializarBotones()
        return binding.root
    }

    private fun inicializarBinding(){
        binding = FragmentLoginBinding.inflate(layoutInflater)
    }

    private fun configurarGoogleSignIn() {
        // Configura Google Sign-In sin token (idToken puede ser null si no usas backend)
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

    private fun inicializarBotones(){
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, 0)
        val recordar = prefs.getBoolean(KEY_RECORDAR, false)

        // Si está recordado y el usuario ya está logueado, saltar
        if (recordar && auth.currentUser != null) {
            findNavController().navigate(R.id.action_loginFragment_to_cargaFragment)
            return
        }

        binding.NoRecuerdo.setOnClickListener{
            findNavController().navigate(R.id.action_loginFragment_to_noRecuerdoFragment)
        }

        binding.btnRegistrar.setOnClickListener{
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.btnLogin.setOnClickListener{
            iniciarSesionFirebase()
        }
    }

    private fun iniciarSesionFirebase() {
        val email = binding.txtCorreo.text.toString().trim()
        val password = binding.txtClave.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Por favor ingresa correo y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Guardar estado de recordar
                    val prefs = requireActivity().getSharedPreferences(PREFS_NAME, 0)
                    val editor = prefs.edit()
                    editor.putBoolean(KEY_RECORDAR, binding.chkRecordar.isChecked)
                    editor.apply()

                    Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_cargaFragment)
                } else {
                    Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

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
                    val firebaseUser = auth.currentUser

                    val correo = firebaseUser?.email ?: ""
                    val usuario = firebaseUser?.displayName ?: ""
                    val descripcion = "Hola soy un usuario nuevo"
                    val imagen = firebaseUser?.photoUrl?.toString() ?: ""

                    val nuevoUsuario = UsuarioInsertar(
                        tabla = "Usuarios",
                        correo = correo,
                        usuario = usuario,
                        descripcion = descripcion,
                        imagen = imagen
                    )

                    val prefs = requireActivity().getSharedPreferences(PREFS_NAME, 0)
                    val editor = prefs.edit()
                    editor.putBoolean(KEY_RECORDAR, true)
                    editor.apply()

                    lifecycleScope.launch {
                        try {
                            val repo = CocineticaRepository()
                            val usuarios = repo.consultaTodosUsuarios()
                            val existe = usuarios.any { it.correo.equals(correo, ignoreCase = true) }

                            if (!existe) {
                                val response = repo.insertarUsuario(nuevoUsuario)
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Usuario creado en la base de datos", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Error al crear usuario en backend", Toast.LENGTH_LONG).show()
                                }
                            }

                            findNavController().navigate(R.id.action_loginFragment_to_cargaFragment)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error accediendo al backend: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }

                } else {
                    Toast.makeText(context, "Error autenticando con Google: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }



}
