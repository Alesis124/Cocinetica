package dam.moviles.cocinetica.vista

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.modelo.Paso

class PasoAdapter(private val listaPasos: List<Paso>) : RecyclerView.Adapter<PasoAdapter.PasoViewHolder>() {

    inner class PasoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNumeroPaso: TextView = view.findViewById(R.id.tvNumeroPaso)
        val tvTextoPaso: TextView = view.findViewById(R.id.tvTextoPaso)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pasos_receta, parent, false)
        return PasoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PasoViewHolder, position: Int) {
        val paso = listaPasos[position]
        holder.tvNumeroPaso.text = "${position + 1}."
        holder.tvTextoPaso.text = paso.texto
    }

    override fun getItemCount(): Int = listaPasos.size
}
