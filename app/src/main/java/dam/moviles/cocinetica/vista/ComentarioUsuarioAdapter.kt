package dam.moviles.cocinetica.vista

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
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ComentarioUsuarioAdapter(
    private val comentarios: List<Comentario>,
    private val valoracionesMap: Map<Int, Valoracion>,
    private val nombreUsuario: String,
    private val onEliminarClick: (Comentario) -> Unit,
    private val onIrClick: (Int) -> Unit
) : RecyclerView.Adapter<ComentarioUsuarioAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUsuario: TextView = itemView.findViewById(R.id.tvUsuario)
        val tvValoracion: TextView = itemView.findViewById(R.id.tvValoracion)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        val tvComentario: TextView = itemView.findViewById(R.id.tvComentario)
        val btnEliminar: Button = itemView.findViewById(R.id.btnEliminar)
        val btnIr: Button = itemView.findViewById(R.id.btnIr)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.comentarios_usuarios, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comentario = comentarios[position]

        holder.tvUsuario.text = nombreUsuario
        holder.tvComentario.text = comentario.texto
        holder.tvFecha.text = formatearTiempoRelativo(comentario.fecha)

        val valoracion = valoracionesMap[comentario.id_comentario]
        val notaTexto = valoracion?.valoracion ?: "Sin valorar"
        holder.tvValoracion.text = if (valoracion != null) "⭐ $notaTexto/5" else "⭐ Sin valorar"

        holder.btnEliminar.setOnClickListener {
            onEliminarClick(comentario)
        }

        holder.btnIr.setOnClickListener {
            onIrClick(comentario.id_receta)
        }
    }

    override fun getItemCount(): Int = comentarios.size

    private fun formatearTiempoRelativo(fechaStr: String): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val localDateTimeUtc = LocalDateTime.parse(fechaStr, formatter)
            val fechaUtc = localDateTimeUtc.atZone(ZoneId.of("UTC"))
            val fechaLocal = fechaUtc.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()

            val ahora = LocalDateTime.now()
            val duracion = Duration.between(fechaLocal, ahora)

            when {
                duracion.toMinutes() < 1 -> "ahora"
                duracion.toMinutes() < 60 -> "hace ${duracion.toMinutes()} min"
                duracion.toHours() < 24 -> "hace ${duracion.toHours()} h"
                duracion.toDays() == 1L -> "ayer"
                duracion.toDays() < 7 -> "hace ${duracion.toDays()} días"
                duracion.toDays() < 30 -> "hace ${duracion.toDays() / 7} sem"
                duracion.toDays() < 365 -> "hace ${duracion.toDays() / 30} mes"
                else -> "hace ${duracion.toDays() / 365} años"
            }
        } catch (e: Exception) {
            fechaStr
        }
    }
}
