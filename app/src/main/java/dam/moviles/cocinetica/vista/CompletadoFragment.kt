package dam.moviles.cocinetica.vista

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentCompletadoBinding
import dam.moviles.cocinetica.viewModel.CompletadoViewModel

class CompletadoFragment : Fragment() {

    private var _binding: FragmentCompletadoBinding? = null
    private val binding get() = _binding!!
    private val args: CompletadoFragmentArgs by navArgs()
    private val viewModel: CompletadoViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompletadoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar ViewModel con los argumentos
        viewModel.setArgs(args.idReceta)

        inicializarBotones()
    }

    private fun inicializarBotones() {
        binding.bntVerRecetaMia.setOnClickListener {
            val action = CompletadoFragmentDirections
                .actionCompletadoFragmentToVerRecetaFragment(
                    viewModel.idReceta,
                    viewModel.origen
                )
            findNavController().navigate(action)
        }

        binding.backInicio.setOnClickListener {
            findNavController().navigate(R.id.action_completadoFragment_to_inicioFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}