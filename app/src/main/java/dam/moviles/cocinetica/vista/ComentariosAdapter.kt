import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.modelo.Comentario
import dam.moviles.cocinetica.modelo.Valoracion
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ComentariosAdapter(
    private val comentarios: List<Comentario>,
    private val valoracionesMap: Map<Int, Valoracion?>,
    private val usuariosMap: Map<Int, String?>,
    private val idUsuarioActual: Int,
    private val onEliminarClick: (Comentario) -> Unit // callback para eliminar
) : RecyclerView.Adapter<ComentariosAdapter.ComentarioViewHolder>() {

    inner class ComentarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUsuario: TextView = itemView.findViewById(R.id.tvUsuario)
        val tvValoracion: TextView = itemView.findViewById(R.id.tvValoracion)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        val tvComentario: TextView = itemView.findViewById(R.id.tvComentario)
        val btnEliminar: Button = itemView.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComentarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.comentarios_receta, parent, false)
        return ComentarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComentarioViewHolder, position: Int) {
        val comentario = comentarios[position]

        holder.tvComentario.text = comentario.texto
        holder.tvFecha.text = formatearTiempoRelativo(comentario.fecha)

        val nombreUsuario = usuariosMap[comentario.id_usuario] ?: "Anónimo"
        holder.tvUsuario.text = nombreUsuario

        val valoracionInt = valoracionesMap[comentario.id_comentario]?.valoracion ?: 0
        val valoracionFloat = valoracionInt.toFloat()
        holder.tvValoracion.text = String.format("⭐ %.1f", valoracionFloat)

        if (comentario.id_usuario == idUsuarioActual) {
            holder.btnEliminar.visibility = View.VISIBLE
            holder.btnEliminar.setOnClickListener {
                onEliminarClick(comentario)
            }
        } else {
            holder.btnEliminar.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = comentarios.size

    private fun formatearTiempoRelativo(fechaStr: String): String {
        return try {
            // Formato para "2025-04-28 08:56:51"
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val fecha = LocalDateTime.parse(fechaStr, formatter)
            val ahora = LocalDateTime.now()
            val duracion = Duration.between(fecha, ahora)

            val minutos = duracion.toMinutes()
            val horas = duracion.toHours()
            val dias = duracion.toDays()

            when {
                minutos < 1 -> "ahora"
                minutos < 60 -> "hace $minutos ${if (minutos == 1L) "minuto" else "minutos"}"
                horas < 24 -> "hace $horas ${if (horas == 1L) "hora" else "horas"}"
                dias == 1L -> "ayer"
                dias in 2..6 -> "hace $dias días"
                dias in 7..13 -> "hace 1 semana"
                dias in 14..20 -> "hace 2 semanas"
                dias in 21..27 -> "hace 3 semanas"
                dias in 28..59 -> "hace 1 mes"
                dias < 365 -> "hace ${dias / 30} meses"
                else -> "hace ${dias / 365} ${if (dias / 365 == 1L) "año" else "años"}"
            }
        } catch (e: Exception) {
            // Si no se puede parsear la fecha, devolver el string original
            fechaStr
        }
    }
}
