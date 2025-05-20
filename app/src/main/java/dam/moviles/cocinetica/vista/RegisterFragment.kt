package dam.moviles.cocinetica.vista

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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
import dam.moviles.cocinetica.modelo.CocineticaRepository
import dam.moviles.cocinetica.modelo.UsuarioInsertar
import dam.moviles.cocinetica.viewModel.RegisterFragmentViewModel
import kotlinx.coroutines.launch


class RegisterFragment : Fragment() {


    lateinit var binding: FragmentRegisterBinding
    lateinit var viewModel: RegisterFragmentViewModel

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val RC_SIGN_IN = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inicializarBinding()
        inicializarViewModel()
        auth = FirebaseAuth.getInstance()
        inicializarBoton()
        configurarGoogleSignIn()
        inicializarTextoIniciarSesion()
        return binding.root
    }

    private fun inicializarBinding(){
        binding = FragmentRegisterBinding.inflate(layoutInflater)
    }

    private fun inicializarViewModel(){
        viewModel = ViewModelProvider(this).get(RegisterFragmentViewModel::class.java)
    }

    private fun configurarGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))  // tu web client ID
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
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
                // Opcional: Podrías navegar a login o dejar que el usuario reintente
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

                            findNavController().navigate(R.id.action_registerFragment_to_cargaFragment)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error accediendo al backend: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }

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
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun registrarUsuario() {
        val nombre = binding.usuariotxt.text.toString().trim()
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
                    user?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
                        if (verifyTask.isSuccessful) {
                            Toast.makeText(context, "Correo de verificación enviado", Toast.LENGTH_SHORT).show()

                            // 🔽 Insertar en la base de datos externa vía API
                            val usuarioInsertar = UsuarioInsertar(
                                correo = email,
                                usuario = nombre,
                                descripcion = "Nuevo usuario registrado", // Puedes personalizar
                                imagen = "" // Imagen vacía por defecto
                            )

                            lifecycleScope.launch {
                                try {
                                    val response = viewModel.insertarUsuario(usuarioInsertar)
                                    if (response.isSuccessful) {
                                        Log.d("Registro", "Usuario insertado en la base de datos correctamente")
                                    } else {
                                        Log.e("Registro", "Fallo al insertar en BD: ${response.code()}")
                                    }
                                } catch (e: Exception) {
                                    Log.e("Registro", "Error al insertar en BD", e)
                                }

                                // Navegar después de la inserción
                                findNavController().navigate(R.id.action_registerFragment_to_cargaFragment)
                            }

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