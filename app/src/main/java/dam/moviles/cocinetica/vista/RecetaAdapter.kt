package dam.moviles.cocinetica.vista

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.modelo.CocineticaRepository
import dam.moviles.cocinetica.modelo.Receta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class RecetaAdapter(
    var recetas: MutableList<Receta>,
    private var enVistaGrid: Boolean,
    val recetasGuardadas: MutableSet<Int>, // Cambiado a val para poder actualizarlo
    private val idUsuario: Int?,
    private val nombreAutor: String,
    val onGuardarClick: suspend (Receta, Boolean) -> Boolean,
    val onVerClick: (Receta) -> Unit,
    val onEliminarClick: (Int) -> Unit
) : RecyclerView.Adapter<RecetaAdapter.RecetaViewHolder>() {

    private val repository = CocineticaRepository()
    var idUsuarioActual = 0

    fun actualizarRecetas(nuevasRecetas: List<Receta>) {
        this.recetas = nuevasRecetas.toMutableList()
        notifyDataSetChanged()
    }

    fun eliminarRecetaPorIndex(index: Int) {
        if (index >= 0 && index < recetas.size) {
            recetas.removeAt(index)
            notifyItemRemoved(index)
        }
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

    suspend fun cargarUsuario(){
        val email = FirebaseAuth.getInstance().currentUser?.email ?: return
        val usuario = repository.consultaUsuarioPorCorreo(email)
        idUsuarioActual = usuario.id_usuario
    }

    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getItemCount(): Int = recetas.size

    inner class RecetaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(receta: Receta) {
            val btnGuardar = itemView.findViewById<MaterialButton>(R.id.txtGuardar)
            val btnVer = itemView.findViewById<MaterialButton>(R.id.txtVer)
            val txtTitulo = itemView.findViewById<TextView>(R.id.txtTitulo)
            val txtAutor = itemView.findViewById<TextView>(R.id.txtAutor)
            val ratingBar = itemView.findViewById<RatingBar>(R.id.ratingBar)
            val guardado = recetasGuardadas.contains(receta.id_receta)
            val email = FirebaseAuth.getInstance().currentUser?.email ?: return
            val btnBorrar = itemView.findViewById<MaterialButton>(R.id.btnEliminar)
            val imagenReceta = itemView.findViewById<ImageView>(R.id.imgReceta)

            actualizarBotonGuardar(btnGuardar, guardado)

            // Configuración inicial
            txtAutor.text = "Autor: $nombreAutor"
            txtTitulo.text = receta.nombre
            ratingBar.rating = receta.valoracion.toFloat()
            if (!receta.imagen.isNullOrEmpty()) {
                val bitmap = base64ToBitmap(receta.imagen)
                bitmap?.let {
                    imagenReceta.setImageBitmap(it)
                } ?: run {
                    imagenReceta.setImageResource(R.drawable.sinimagenplato) // Imagen por defecto si hay error
                }
            } else {
                imagenReceta.setImageResource(R.drawable.sinimagenplato) // Imagen por defecto si no hay Base64
            }

            // Estado inicial del botón guardar (icono + texto)
            actualizarBotonGuardar(btnGuardar, guardado)

            // Reemplaza el código actual del btnVer por esto:
            btnVer.doOnLayout {
                val textWidth = btnVer.paint.measureText(btnVer.text.toString()).toInt()
                btnVer.minWidth = textWidth + btnVer.paddingLeft + btnVer.paddingRight
                btnVer.minimumWidth = textWidth + btnVer.paddingLeft + btnVer.paddingRight
            }

            itemView.setOnClickListener {
                onVerClick(receta)
            }

            // Mostrar el botón de borrar solo si la receta es del usuario actual
            if (receta.id_usuario == idUsuario) {
                btnBorrar.visibility = View.VISIBLE
                btnBorrar.setOnClickListener {
                    onEliminarClick(receta.id_receta)
                }
                itemView.post { itemView.requestLayout() }
            } else {
                btnBorrar.visibility = View.GONE
            }

            /*
            if (receta.id_usuario == idUsuarioActual) {
                btnBorrar.visibility = View.VISIBLE
                btnBorrar.setOnClickListener {
                    onEliminarClick(receta.id_receta)
                }
            } else {
                btnBorrar.visibility = View.GONE
            }
*/

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
                        actualizarBotonGuardar(btnGuardar, ahoraEstaGuardada)
                    }
                }
            }




            btnVer?.setOnClickListener {
                onVerClick(receta)
            }

            txtTitulo.text = receta.nombre
            txtAutor.text = "Autor: ${receta.usuario ?: nombreAutor}"
            ratingBar.rating = receta.valoracion.toFloat()
        }

        private fun actualizarBotonGuardar(btn: MaterialButton, estaGuardada: Boolean) {
            btn.icon = ContextCompat.getDrawable(
                btn.context,
                if (estaGuardada) R.drawable.guardar48llevo else R.drawable.guardar48vacio
            )
        }
    }
    // Añade este método en tu RecetaAdapter
    fun actualizarRecetasGuardadas(nuevasGuardadas: Set<Int>) {
        recetasGuardadas.clear()
        recetasGuardadas.addAll(nuevasGuardadas)
        notifyDataSetChanged() // O notifyItemRangeChanged(0, itemCount)
    }

}