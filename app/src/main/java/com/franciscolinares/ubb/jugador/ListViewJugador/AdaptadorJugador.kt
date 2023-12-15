package com.franciscolinares.ubb.jugador.ListViewJugador

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.franciscolinares.ubb.R
import com.squareup.picasso.Picasso

class AdaptadorJugador(private val mcontext: Context, private val listaJugadores: List<Jugador>) :
    ArrayAdapter<Jugador>(mcontext, 0, listaJugadores) {

    @SuppressLint("ViewHolder", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layout = LayoutInflater.from(mcontext).inflate(R.layout.jugador_item, parent, false)

        val jugador = listaJugadores[position]

        layout.findViewById<TextView>(R.id.txtLVJNombre)
            .setText("Nombre:  " + jugador.nombre + " " + jugador.apellido1 + " " + jugador.apellido2)
        layout.findViewById<TextView>(R.id.txtLVJCategoria)
            .setText("Categoria:  " + jugador.categoria)
        layout.findViewById<TextView>(R.id.txtLVJSexo)
            .setText("Sexo:  " + jugador.sexo)
        layout.findViewById<TextView>(R.id.txtLVJEquipo)
            .setText("Equipo:  " + jugador.equipo)

        if (jugador.foto != "") {
            Picasso.get()
                .load(jugador.foto)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(layout.findViewById<ImageView>(R.id.imageLVJJugador))
        }

        return layout
    }
}