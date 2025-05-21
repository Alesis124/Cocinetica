package dam.moviles.cocinetica.vista

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.modelo.Receta

class RecetaAdapter(
    var recetas: List<Receta>,
    private var enVistaGrid: Boolean
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
            itemView.findViewById<TextView>(R.id.txtTitulo).text = receta.nombre
            itemView.findViewById<TextView>(R.id.txtAutor).text = "Autor: ${receta.usuario}"
            itemView.findViewById<RatingBar>(R.id.ratingBar).rating = receta.valoracion.toFloat()
        }
    }

}




