package com.franciscolinares.ubb.invitado.RecyclerViewInvitado

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.franciscolinares.ubb.R
import com.squareup.picasso.Picasso

class JugadorAdapter(
    private val jugadores: MutableList<Jugador>,
    private val onItemClick: (Jugador) -> Unit,
    private val onDeleteClick: (Jugador) -> Unit
) : RecyclerView.Adapter<JugadorAdapter.JugadorViewHolder>() {

    class JugadorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val logo: ImageView = itemView.findViewById(R.id.logoJugadorInvitado)
        val nombre: TextView = itemView.findViewById(R.id.txtNombreJInvitado)
        val categoria: TextView = itemView.findViewById(R.id.txtCategoriaJInvitado)
        val pj: TextView = itemView.findViewById(R.id.txtItemPJJugador)
        val pts: TextView = itemView.findViewById(R.id.txtItemPTSJugador)
        val btnEliminar: ImageView = itemView.findViewById(R.id.btnEliminarJInvitado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JugadorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.jugador_invitado_item, parent, false)
        return JugadorViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: JugadorViewHolder, position: Int) {
        val jugador = jugadores[position]
        holder.nombre.text = jugador.nombre
        holder.categoria.text = jugador.categoria + " " + jugador.sexo
        holder.pj.text = "PJ: " + jugador.pj
        holder.pts.text = "PTS: " + jugador.pts

        if (jugador.logoResId != "") {
            Picasso.get()
                .load(jugador.logoResId)
                .error(R.drawable.jugador_de_baloncesto)
                .into(holder.logo)
        }

        holder.itemView.setOnClickListener {
            onItemClick(jugador)
        }

        holder.btnEliminar.setOnClickListener {
            onDeleteClick(jugador)
        }
    }

    override fun getItemCount(): Int = jugadores.size
}