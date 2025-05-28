package dam.moviles.cocinetica.vista

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import dam.moviles.cocinetica.R
import dam.moviles.cocinetica.modelo.CocineticaRepository
import dam.moviles.cocinetica.modelo.Receta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecetaAdapter(
    var recetas: MutableList<Receta>,
    private var enVistaGrid: Boolean,
    val recetasGuardadas: MutableSet<Int>, // Cambiado a val para poder actualizarlo
    private val idUsuario: Int?,
    private val nombreAutor: String,
    val onGuardarClick: suspend (Receta, Boolean) -> Boolean,
    val onVerClick: (Receta) -> Unit
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

    override fun getItemCount(): Int = recetas.size

    inner class RecetaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(receta: Receta) {
            val btnGuardar = itemView.findViewById<com.google.android.material.button.MaterialButton>(R.id.txtGuardar)
            val btnVer = itemView.findViewById<Button>(R.id.txtVer)
            val txtTitulo = itemView.findViewById<TextView>(R.id.txtTitulo)
            val txtAutor = itemView.findViewById<TextView>(R.id.txtAutor)
            val ratingBar = itemView.findViewById<RatingBar>(R.id.ratingBar)
            val guardado = recetasGuardadas.contains(receta.id_receta)
            val email = FirebaseAuth.getInstance().currentUser?.email ?: return

            actualizarBotonGuardar(btnGuardar, guardado)

            // Configuración inicial
            txtAutor.text = "Autor: $nombreAutor"
            txtTitulo.text = receta.nombre
            ratingBar.rating = receta.valoracion.toFloat()

            // Estado inicial del botón guardar (icono + texto)
            actualizarBotonGuardar(btnGuardar, guardado)

            itemView.setOnClickListener {
                onVerClick(receta)
            }

            CoroutineScope(Dispatchers.Main).launch {
                cargarUsuario()
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
                        actualizarBotonGuardar(btnGuardar, ahoraEstaGuardada)
                    }
                }
            }




            btnVer?.setOnClickListener {
                onVerClick(receta)
            }
        }

        private fun actualizarBotonGuardar(btn: MaterialButton, estaGuardada: Boolean) {
            btn.icon = ContextCompat.getDrawable(
                btn.context,
                if (estaGuardada) R.drawable.guardar48llevo else R.drawable.guardar48vacio
            )
        }
    }
}