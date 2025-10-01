package com.franciscolinares.ubb.invitado.RecyclerViewInvitado

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.franciscolinares.ubb.R
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso

class EquipoAdapter(
    private val equipos: MutableList<Equipo>,
    private val onItemClick: (Equipo) -> Unit,
    private val onDeleteClick: (Equipo) -> Unit
) : RecyclerView.Adapter<EquipoAdapter.EquipoViewHolder>() {

    class EquipoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val logo: ImageView = itemView.findViewById(R.id.logoEquipoInvitado)
        val nombre: TextView = itemView.findViewById(R.id.txtNombreEInvitado)
        val categoria: TextView = itemView.findViewById(R.id.txtCategoriaEInvitado)
        val btnEliminar: ImageView = itemView.findViewById(R.id.btnEliminarEInvitado)
        val pj: TextView = itemView.findViewById(R.id.txtItemPJEquipo)
        val pg: TextView = itemView.findViewById(R.id.txtItemPGEquipo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.equipo_invitado_item, parent, false)
        return EquipoViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: EquipoViewHolder, position: Int) {
        val equipo = equipos[position]
        holder.nombre.text = equipo.nombre
        holder.categoria.text = equipo.categoria + " " + equipo.sexo
        holder.pj.text = "PJ: " + equipo.pj
        holder.pg.text = "PG: " + equipo.pg

        if (equipo.logoResId != "") {
            Glide.with(holder.itemView.context)
                .load(equipo.logoResId)
                .placeholder(R.drawable.escudopredeterminado)
                .error(R.drawable.escudopredeterminado)
                .into(holder.logo)
        }

        holder.itemView.setOnClickListener {
            onItemClick(equipo)
        }

        holder.btnEliminar.setOnClickListener {
            onDeleteClick(equipo)
        }
    }

    override fun getItemCount(): Int = equipos.size
}