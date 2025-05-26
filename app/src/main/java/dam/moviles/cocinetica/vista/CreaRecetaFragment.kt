package dam.moviles.cocinetica.vista

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dam.moviles.cocinetica.databinding.FragmentCreaRecetaBinding
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.viewModel.CreaRecetaViewModel

class CreaRecetaFragment : Fragment() {

    private lateinit var binding: FragmentCreaRecetaBinding
    private lateinit var viewModel: CreaRecetaViewModel
    private val unidades = listOf("g", "kg", "ml", "l", "cucharada", "taza", "pieza")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreaRecetaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[CreaRecetaViewModel::class.java]

        binding.buttonAgregarIngrediente.setOnClickListener {
            sincronizarDatosConViewModel()
            viewModel.agregarIngrediente()
            renderIngredientes()
        }

        binding.buttonAgregarPaso.setOnClickListener {
            sincronizarDatosConViewModel()
            viewModel.agregarPaso()
            renderPasos()
        }

        renderIngredientes()
        renderPasos()
        inicializarBotones()
    }

    private fun sincronizarDatosConViewModel() {
        // Ingredientes
        for (i in 0 until binding.layoutIngredientes.childCount) {
            val ingredienteView = binding.layoutIngredientes.getChildAt(i)
            val etCantidad = ingredienteView.findViewById<EditText>(R.id.editTextCantidad)
            val etNombre = ingredienteView.findViewById<EditText>(R.id.editTextNombreIngrediente)
            val spinnerUnidad = ingredienteView.findViewById<Spinner>(R.id.spinnerUnidad)

            if (i < viewModel.ingredientes.size) {
                viewModel.ingredientes[i].cantidad = etCantidad.text.toString()
                viewModel.ingredientes[i].nombre = etNombre.text.toString()
                viewModel.ingredientes[i].unidad = spinnerUnidad.selectedItem.toString()
            }
        }

        // Pasos
        for (i in 0 until binding.layoutPasos.childCount) {
            val pasoView = binding.layoutPasos.getChildAt(i)
            val etDescripcion = pasoView.findViewById<EditText>(R.id.etDescripcionPaso)

            if (i < viewModel.pasos.size) {
                viewModel.pasos[i].descripcion = etDescripcion.text.toString()
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        sincronizarDatosConViewModel() // Guarda los textos reales en el ViewModel
    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        // Vuelve a renderizar los datos después de restaurar el estado
        renderIngredientes()
        renderPasos()
    }


    private fun renderIngredientes() {
        binding.layoutIngredientes.removeAllViews()
        viewModel.ingredientes.forEachIndexed { index, ingrediente ->
            val ingredienteView = layoutInflater.inflate(R.layout.item_ingrediente, binding.layoutIngredientes, false)

            val tvNumero = ingredienteView.findViewById<TextView>(R.id.textViewOrdenIngrediente)
            val etCantidad = ingredienteView.findViewById<EditText>(R.id.editTextCantidad)
            val etNombre = ingredienteView.findViewById<EditText>(R.id.editTextNombreIngrediente)
            val spinnerUnidad = ingredienteView.findViewById<Spinner>(R.id.spinnerUnidad)
            val btnEliminar = ingredienteView.findViewById<ImageButton>(R.id.buttonEliminarIngrediente)

            tvNumero.text = "${index + 1}."

            // Quitar listeners antes de setText para evitar que se dispare
            etCantidad.setText(ingrediente.cantidad, TextView.BufferType.EDITABLE)
            etNombre.setText(ingrediente.nombre, TextView.BufferType.EDITABLE)

            // Solo registrar cambios del usuario
            etCantidad.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.ingredientes[index].cantidad = etCantidad.text.toString()
                }
            }

            etNombre.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.ingredientes[index].nombre = etNombre.text.toString()
                }
            }

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, unidades)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerUnidad.adapter = adapter

            val pos = unidades.indexOf(ingrediente.unidad)
            spinnerUnidad.setSelection(if (pos != -1) pos else 0)

            spinnerUnidad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                    viewModel.ingredientes[index].unidad = unidades[pos]
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            btnEliminar.setOnClickListener {
                ocultarTecladoYQuitarFoco()
                sincronizarDatosConViewModel()
                viewModel.eliminarIngrediente(index)
                renderIngredientes()
            }

            binding.layoutIngredientes.addView(ingredienteView)
        }
    }

    fun inicializarBotones(){
        binding.buttonVolver.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.buttonGuardar.setOnClickListener {
            //Guardara en la base de datos
        }
    }

    private fun ocultarTecladoYQuitarFoco() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        val view = requireActivity().currentFocus
        view?.clearFocus()
        if (view != null) {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun renderPasos() {
        binding.layoutPasos.removeAllViews()
        viewModel.pasos.forEachIndexed { index, paso ->
            val pasoView = layoutInflater.inflate(R.layout.item_paso, binding.layoutPasos, false)

            val tvNumeroPaso = pasoView.findViewById<TextView>(R.id.tvNumeroPaso)
            val etDescripcion = pasoView.findViewById<EditText>(R.id.etDescripcionPaso)
            val btnEliminar = pasoView.findViewById<ImageButton>(R.id.btnEliminarPaso)

            tvNumeroPaso.text = "${index + 1}."
            etDescripcion.setText(paso.descripcion, TextView.BufferType.EDITABLE)

            etDescripcion.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.pasos[index].descripcion = etDescripcion.text.toString()
                }
            }

            btnEliminar.setOnClickListener {
                ocultarTecladoYQuitarFoco()
                sincronizarDatosConViewModel()
                viewModel.eliminarPaso(index)
                renderPasos()
            }

            binding.layoutPasos.addView(pasoView)
        }
    }

}
