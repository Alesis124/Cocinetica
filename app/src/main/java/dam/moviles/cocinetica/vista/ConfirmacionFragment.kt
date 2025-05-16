package dam.moviles.cocinetica.vista

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentConfirmacionBinding


class ConfirmacionFragment : Fragment() {

    lateinit var binding: FragmentConfirmacionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        inicializarBinding()
        return binding.root
    }

    fun inicializarBinding(){
        binding= FragmentConfirmacionBinding.inflate(layoutInflater)
    }


}