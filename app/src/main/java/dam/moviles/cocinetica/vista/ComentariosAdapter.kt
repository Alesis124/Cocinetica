import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.modelo.Comentario
import dam.moviles.cocinetica.modelo.Valoracion

class ComentariosAdapter(
    private val comentarios: List<Comentario>,
    private val valoracionesMap: Map<Int, Valoracion?>,
    private val usuariosMap: Map<Int, String?>
) : RecyclerView.Adapter<ComentariosAdapter.ComentarioViewHolder>() {

    inner class ComentarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUsuario: TextView = itemView.findViewById(R.id.tvUsuario)
        val tvValoracion: TextView = itemView.findViewById(R.id.tvValoracion)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        val tvComentario: TextView = itemView.findViewById(R.id.tvComentario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComentarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.comentarios_receta, parent, false)
        return ComentarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComentarioViewHolder, position: Int) {
        val comentario = comentarios[position]

        holder.tvComentario.text = comentario.texto
        holder.tvFecha.text = comentario.fecha

        // Nombre usuario
        val nombreUsuario = usuariosMap[comentario.id_usuario] ?: "Anónimo"
        holder.tvUsuario.text = nombreUsuario

        // Valoración en formato "⭐ 4.5"
        val valoracionInt = valoracionesMap[comentario.id_comentario]?.valoracion ?: 0
        val valoracionFloat = valoracionInt.toFloat()
        holder.tvValoracion.text = String.format("⭐ %.1f", valoracionFloat)
    }

    override fun getItemCount(): Int = comentarios.size
}
