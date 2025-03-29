package com.franciscolinares.ubb.estadistica.ListViewEstadistica

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.franciscolinares.ubb.R

class AdaptadorTiroJugador(
    private val mcontext: Context,
    private var listaPlantilla: MutableList<TiroJugador>,
    private val onCheckBoxChanged: (TiroJugador) -> Unit
) :
    ArrayAdapter<TiroJugador>(mcontext, 0, listaPlantilla) {

    private val checkboxState = mutableMapOf<Int, Boolean>()

    init {
        for (i in listaPlantilla.indices) {
            checkboxState[i] = true
        }
    }

    @SuppressLint("MissingInflatedId", "SetTextI18n", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView = LayoutInflater.from(mcontext).inflate(R.layout.jugador_tiro_item, parent, false)

        val jugador = listaPlantilla[position]

        val cb = listItemView.findViewById<CheckBox>(R.id.cbTiroJugador)

        if ((jugador.nombre).length > 13) {
            listItemView.findViewById<TextView>(R.id.txtNombreJugadorTiro).text = "${(jugador.nombre.toUpperCase()).substring(0, 10)}..."
        } else {
            listItemView.findViewById<TextView>(R.id.txtNombreJugadorTiro).text = jugador.nombre.toUpperCase()
        }
        listItemView.findViewById<TextView>(R.id.txtDorsalJugadorTiro).text = jugador.dorsal

        cb.isChecked = checkboxState[position] ?: true
        cb.setOnCheckedChangeListener(null) // Evitar bucles infinitos

        if (jugador.esSelector) {
            cb.isChecked = listaPlantilla.drop(1).all { it.convocado } // Asegura estado correcto

            cb.setOnCheckedChangeListener { _, isChecked ->
                listaPlantilla.forEach { if (!it.esSelector) it.convocado = isChecked }
                notifyDataSetChanged() // Refrescar el ListView
                onCheckBoxChanged(jugador) // Actualizar tiros
            }
        } else {
            cb.isChecked = jugador.convocado

            cb.setOnCheckedChangeListener { _, isChecked ->
                jugador.convocado = isChecked

                // Verificar si todos los jugadores están seleccionados o no
                val todosSeleccionados = listaPlantilla.drop(1).all { it.convocado }
                listaPlantilla[0].convocado = todosSeleccionados // Actualizar CheckBox general

                notifyDataSetChanged() // Refrescar UI
                onCheckBoxChanged(jugador) // Actualizar tiros
            }
        }
        return listItemView
    }

    fun updateData(newData: List<TiroJugador>) {
        listaPlantilla = newData.toMutableList()
        notifyDataSetChanged() // Esto refresca la vista, pero no recrea todos los elementos
    }
}