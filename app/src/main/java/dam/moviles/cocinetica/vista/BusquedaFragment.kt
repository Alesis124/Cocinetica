package dam.moviles.cocinetica.vista

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationBarView
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.databinding.FragmentBusquedaBinding
import dam.moviles.cocinetica.modelo.CocineticaRepository
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs


class BusquedaFragment : Fragment() {

    lateinit var binding: FragmentBusquedaBinding
    private val repository = CocineticaRepository()
    private val args: BusquedaFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inicializarBinding(inflater, container)
        binding.bottomNav.selectedItemId = R.id.nav_search
        inicializarBotones()
        mostrarHistorial()
        return binding.root
    }

    fun inicializarBinding(inflater: LayoutInflater, container: ViewGroup?) {
        binding = FragmentBusquedaBinding.inflate(inflater, container, false)
    }


    private fun inicializarBotones() {
        binding.btnBorrarHistorial.setOnClickListener {
            borrarHistorial()
        }
        binding.fabAgregar.setOnClickListener {
            findNavController().navigate(R.id.action_busquedaFragment_to_creaRecetaFragment)
        }

        binding.bottomNav.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    findNavController().navigate(R.id.action_busquedaFragment_to_inicioFragment)
                    true
                }
                R.id.nav_search -> true
                R.id.nav_book -> {
                    findNavController().navigate(R.id.action_busquedaFragment_to_guardadosFragment)
                    true
                }
                R.id.nav_profile -> {
                    val action = BusquedaFragmentDirections.actionBusquedaFragmentToCuentaFragment(args.tab)
                    findNavController().navigate(action)
                    true
                }
                else -> false
            }
        })

        binding.btnBuscar.setOnClickListener {
            val textoBusqueda = binding.etBusqueda.text.toString().trim()
            if (textoBusqueda.isNotEmpty()) {
                buscarYNavegar(textoBusqueda)
            }
        }

        binding.etBusqueda.setOnEditorActionListener { _, _, _ ->
            val textoBusqueda = binding.etBusqueda.text.toString().trim()
            if (textoBusqueda.isNotEmpty()) {
                buscarYNavegar(textoBusqueda)
                true
            } else {
                false
            }
        }
    }
    private fun obtenerHistorial(): MutableList<String> {
        val prefs = requireContext().getSharedPreferences("historial_busqueda", Context.MODE_PRIVATE)
        val historial = prefs.getStringSet("historial", emptySet())!!.toMutableList()
        historial.sortByDescending { prefs.getLong("tiempo_$it", 0L) }
        return historial
    }

    private fun guardarBusqueda(texto: String) {
        val prefs = requireContext().getSharedPreferences("historial_busqueda", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val historial = obtenerHistorial().toMutableSet()

        historial.add(texto)
        if (historial.size > 5) {
            val ordenadas = historial.sortedByDescending { prefs.getLong("tiempo_$it", 0L) }
            historial.clear()
            historial.addAll(ordenadas.take(5))
        }

        editor.putStringSet("historial", historial)
        editor.putLong("tiempo_$texto", System.currentTimeMillis())
        editor.apply()
    }

    private fun mostrarHistorial() {
        val historial = obtenerHistorial()
        val contenedor = binding.contenedorHistorial
        contenedor.removeAllViews()

        if (historial.isEmpty()) {
            val texto = TextView(requireContext()).apply {
                text = "Realiza tu primera búsqueda"
                textSize = 14f
                setPadding(16, 16, 16, 16)
                setTextColor(resources.getColor(android.R.color.darker_gray, null))
            }
            contenedor.addView(texto)
            return
        }

        for (item in historial) {
            val fila = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(24, 16, 24, 16)
                setBackgroundResource(R.drawable.bg_historial_chip)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 8, 0, 8)
                layoutParams = params
                elevation = 4f
            }

            val texto = TextView(requireContext()).apply {
                text = item
                textSize = 16f
                setTextColor(resources.getColor(android.R.color.black, null))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener {
                    binding.etBusqueda.setText(item)
                    buscarYNavegar(item)
                }
            }

            val botonEliminar = TextView(requireContext()).apply {
                text = "✕"
                textSize = 18f
                setPadding(16, 0, 0, 0)
                setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                setOnClickListener {
                    eliminarBusqueda(item)
                }
            }

            fila.addView(texto)
            fila.addView(botonEliminar)
            contenedor.addView(fila)
        }
    }


    private fun eliminarBusqueda(texto: String) {
        val prefs = requireContext().getSharedPreferences("historial_busqueda", Context.MODE_PRIVATE)
        val historial = obtenerHistorial().toMutableSet()
        historial.remove(texto)

        val editor = prefs.edit()
        editor.putStringSet("historial", historial)
        editor.remove("tiempo_$texto")
        editor.apply()

        mostrarHistorial()
    }


    private fun borrarHistorial() {
        val prefs = requireContext().getSharedPreferences("historial_busqueda", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val historial = obtenerHistorial()
        historial.forEach {
            editor.remove("tiempo_$it")
        }
        editor.remove("historial")
        editor.apply()
        mostrarHistorial()
    }




    private fun buscarYNavegar(texto: String) {
        lifecycleScope.launch {
            try {
                val recetas = repository.buscarRecetas(texto)
                if (recetas.isNotEmpty()) {
                    guardarBusqueda(texto)
                    mostrarHistorial()
                    val idsRecetas = recetas.map { it.id_receta }.toIntArray()
                    val action = BusquedaFragmentDirections
                        .actionBusquedaFragmentToResultadoBusquedaFragment(idsRecetas, args.tab)
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(requireContext(), "No se encontró ninguna receta con ese nombre", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("BusquedaFragment", "Error al buscar recetas: ${e.message}")
            }
        }
    }
}
