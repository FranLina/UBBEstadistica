package com.franciscolinares.ubb.equipo.ListViewEquipo

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

class AdaptadorEquipo(private val mcontext: Context, private val listaEquipo: List<Equipo>) :
    ArrayAdapter<Equipo>(mcontext, 0, listaEquipo) {

    @SuppressLint("MissingInflatedId", "SetTextI18n", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layout = LayoutInflater.from(mcontext).inflate(R.layout.equipo_item, parent, false)

        val equipo = listaEquipo[position]

        layout.findViewById<TextView>(R.id.txtLVNombreEquipo).text ="Nombre:  " + equipo.nombreEquipo
        layout.findViewById<TextView>(R.id.txtLVECategoria).text = "Categoria:  " + equipo.categoria
        layout.findViewById<TextView>(R.id.txtLVESexo).text = "Sexo:  " + equipo.sexo
        layout.findViewById<TextView>(R.id.txtLVELocalidad).text = "Localidad:  " + equipo.localidad

        if (equipo.foto != "") {
            Picasso.get()
                .load(equipo.foto)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(layout.findViewById<ImageView>(R.id.imageLVEquipo))
        }

        return layout
    }

}