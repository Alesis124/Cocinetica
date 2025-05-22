package dam.moviles.cocinetica.vista

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.modelo.Receta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecetaAdapter(
    var recetas: List<Receta>,
    private var enVistaGrid: Boolean,
    private val recetasGuardadas: MutableSet<Int>,
    private val idUsuario: Int?,
    val onGuardarClick: suspend (Receta, Boolean) -> Boolean,
    val onVerClick: (Receta) -> Unit
) : RecyclerView.Adapter<RecetaAdapter.RecetaViewHolder>() {

    fun actualizarRecetas(nuevasRecetas: List<Receta>) {
        this.recetas = nuevasRecetas
        notifyDataSetChanged()
    }

    fun cambiarVista(grid: Boolean) {
        enVistaGrid = grid
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (enVistaGrid) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
        val layoutId = if (viewType == 1) R.layout.item_receta_grid else R.layout.item_receta_lista
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return RecetaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
        holder.bind(recetas[position])
    }

    override fun getItemCount(): Int = recetas.size

    inner class RecetaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(receta: Receta) {
            val btnGuardar = itemView.findViewById<Button>(R.id.txtGuardar)
            val btnVer = itemView.findViewById<Button>(R.id.txtVer)
            val txtTitulo = itemView.findViewById<TextView>(R.id.txtTitulo)
            val txtAutor = itemView.findViewById<TextView>(R.id.txtAutor)
            val ratingBar = itemView.findViewById<RatingBar>(R.id.ratingBar)
            val guardado = recetasGuardadas.contains(receta.id_receta)

            btnGuardar.text = when {
                guardado && enVistaGrid -> "Quitar"
                guardado -> "Quitar de guardados"
                else -> "Guardar"
            }


            btnGuardar.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    val estabaGuardada = recetasGuardadas.contains(receta.id_receta)
                    val exito = onGuardarClick(receta, estabaGuardada)
                    if (exito) {
                        if (estabaGuardada) {
                            recetasGuardadas.remove(receta.id_receta)
                        } else {
                            recetasGuardadas.add(receta.id_receta)
                        }
                        val ahoraEstaGuardada = recetasGuardadas.contains(receta.id_receta)
                        btnGuardar.text = when {
                            ahoraEstaGuardada && enVistaGrid -> "Quitar"
                            ahoraEstaGuardada -> "Quitar de guardados"
                            else -> "Guardar"
                        }
                    }
                }
            }



            btnVer?.setOnClickListener {
                onVerClick(receta)
            }

            txtTitulo.text = receta.nombre
            txtAutor.text = "Autor: ${receta.usuario}"
            ratingBar.rating = receta.valoracion.toFloat()
        }
    }
}
