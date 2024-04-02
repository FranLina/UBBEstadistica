package com.franciscolinares.ubb.partido.ListViewPartido

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.estadistica.ListViewEstadistica.MinutoAMinuto
import com.squareup.picasso.Picasso
import java.util.Collections
import java.util.Locale

class AdaptadorJugadorConvocado(private val mcontext: Context, private var listaPlantilla: MutableList<JugadorConvocado>) :
    ArrayAdapter<JugadorConvocado>(mcontext, 0, listaPlantilla) {

    private val checkboxState = mutableMapOf<Int, Boolean>()

    init {
        for(i in listaPlantilla.indices){
            checkboxState[i] = false
        }
    }

    @SuppressLint("MissingInflatedId", "SetTextI18n", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView = LayoutInflater.from(mcontext).inflate(R.layout.jugador_convocado_item, parent, false)

        val jugador = listaPlantilla[position]

        val cb = listItemView.findViewById<CheckBox>(R.id.cbConvocado)

        listItemView.findViewById<TextView>(R.id.txtConvocadoNombre).text =
            (jugador.apellido1 + " " + jugador.apellido2 + ", " + jugador.nombre).toUpperCase(
                Locale.ROOT
            )
        listItemView.findViewById<EditText>(R.id.ettConvocadoDorsal).setText(jugador.dorsal)
        if (jugador.foto != "") {
            Picasso.get()
                .load(jugador.foto)
                .placeholder(R.drawable.jugador_blanco)
                .error(R.drawable.jugador_blanco)
                .into(listItemView.findViewById<ImageView>(R.id.imgConvocadoJugador))
        }
        cb.isChecked = checkboxState[position] ?: false

        cb.setOnCheckedChangeListener { _, isChecked ->
            checkboxState[position] = isChecked
            if (isChecked)
                jugador.convocado = isChecked
            else
                jugador.convocado = isChecked
        }
        listItemView.findViewById<EditText>(R.id.ettConvocadoDorsal).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Actualiza el valor en el ArrayList que proporciona datos al Adapter
                listaPlantilla[position].dorsal = s.toString()
            }
        })

        return listItemView
    }

    fun updateData(newData: List<JugadorConvocado>) {
        listaPlantilla = newData.toMutableList()
        notifyDataSetChanged() // Esto refresca la vista, pero no recrea todos los elementos
    }
}