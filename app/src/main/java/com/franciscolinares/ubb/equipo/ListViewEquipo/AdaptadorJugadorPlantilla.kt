package com.franciscolinares.ubb.equipo.ListViewEquipo

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.franciscolinares.ubb.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class AdaptadorJugadorPlantilla(
    private val mcontext: Context,
    private var listaPlantilla: MutableList<JugadorPlantilla>
) :
    ArrayAdapter<JugadorPlantilla>(mcontext, 0, listaPlantilla) {
    private val db = Firebase.firestore

    @SuppressLint("MissingInflatedId", "SetTextI18n", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView =
            LayoutInflater.from(mcontext).inflate(R.layout.jugador_plantilla_item, parent, false)

        val jugador = listaPlantilla[position]

        val prefs = PreferenceManager.getDefaultSharedPreferences(mcontext)
        val equipo_id = prefs.getString("equipo_id", "").toString()

        listItemView.findViewById<TextView>(R.id.txtNombreJP).text =
            jugador.nombre + " " + jugador.apellido1 + " " + jugador.apellido2

        listItemView.findViewById<EditText>(R.id.txtDorsalJP).setText(jugador.dorsal)

        listItemView.findViewById<ImageButton>(R.id.btnEliminarJugadorE).setOnClickListener {
            eliminarJugadorDEquipo(jugador, equipo_id)
        }

        listItemView.findViewById<EditText>(R.id.txtDorsalJP)
            .addTextChangedListener(object : TextWatcher {
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

    fun eliminarJugadorDEquipo(ju: JugadorPlantilla, equipo_id: String) {
        var jugadores = hashMapOf<String, String>()
        db.collection("Jugadores").document(ju.id_jugador)
            .update(
                hashMapOf(
                    "Equipo" to ""
                ) as HashMap<String?, Any>
            )
        db.collection("Equipos").document(equipo_id)
            .get().addOnSuccessListener {
                jugadores = it.get("Jugadores") as HashMap<String, String>;
                jugadores.remove(ju.id_jugador)

                db.collection("Equipos").document(equipo_id).update(
                    hashMapOf(
                        "Jugadores" to jugadores
                    ) as HashMap<String?, Any>
                ).addOnSuccessListener {
                    // Elimina el jugador de la lista
                    listaPlantilla.remove(ju)
                    notifyDataSetChanged()
                    Toast.makeText(
                        context,
                        "Jugador borrado con exito",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

}