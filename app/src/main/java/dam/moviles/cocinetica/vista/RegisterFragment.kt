package dam.moviles.cocinetica.vista

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentRegisterBinding
import dam.moviles.cocinetica.modelo.NavegadorError


class RegisterFragment : Fragment() {



    lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        inicializarBinding()
        inicializarBoton()
        return binding.root
    }

    fun inicializarBinding(){
        binding= FragmentRegisterBinding.inflate(layoutInflater)
    }

    fun inicializarBoton(){
        binding.btnCrearCuenta.setOnClickListener {

        }
    }


}