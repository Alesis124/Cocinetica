package dam.moviles.cocinetica.vista

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentRespuestaRegisterBinding

class Respuesta_registerFragment : Fragment() {
    private var _binding: FragmentRespuestaRegisterBinding? = null
    val binding: FragmentRespuestaRegisterBinding
        get() = checkNotNull(_binding)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        inicializarBinding(inflater, container)
        inicializarBoton()
        inicializarMensajeError()
        return binding.root
    }

    fun inicializarBinding(inflater: LayoutInflater, container: ViewGroup?) {
        _binding = FragmentRespuestaRegisterBinding.inflate(inflater, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun inicializarBoton(){
        binding.btnAtras.setOnClickListener {
            findNavController().navigate(R.id.action_respuesta_registerFragment_to_contraseniaFragment)
        }

    }

    fun inicializarMensajeError() {
        val mensajeError = Respuesta_registerFragmentArgs.fromBundle(requireArguments()).mensajeError
        binding.txtMensajeError.text = mensajeError
    }




}