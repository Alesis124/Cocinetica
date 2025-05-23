import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.modelo.Contiene
import dam.moviles.cocinetica.modelo.Ingrediente
import dam.moviles.cocinetica.modelo.UM

class IngredienteAdapter(
    private val contieneList: List<Contiene>,
    private val ingredienteList: List<Ingrediente>,
    private val unidadMedidaList: List<UM>
) : RecyclerView.Adapter<IngredienteAdapter.IngredienteViewHolder>() {

    inner class IngredienteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNumero: TextView = view.findViewById(R.id.tvNumero)
        val tvCantidad: TextView = view.findViewById(R.id.tvCantidad)
        val tvUnidad: TextView = view.findViewById(R.id.tvUnidad)
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredienteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ingredientes_receta, parent, false)
        return IngredienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredienteViewHolder, position: Int) {
        val contiene = contieneList[position]

        // Buscar ingrediente según id_ingrediente de Contiene
        val ingrediente = ingredienteList.find { it.id_ingrediente == contiene.id_ingrediente }
        // Buscar unidad de medida según id_um de Contiene
        val unidad = unidadMedidaList.find { it.id_um == contiene.id_um }

        holder.tvNumero.text = "${position + 1}."
        holder.tvCantidad.text = contiene.cantidad.toString()
        holder.tvUnidad.text = unidad?.medida ?: "unidad"
        holder.tvNombre.text = ingrediente?.nombre ?: "Ingrediente desconocido"
    }


    override fun getItemCount() = contieneList.size
}
