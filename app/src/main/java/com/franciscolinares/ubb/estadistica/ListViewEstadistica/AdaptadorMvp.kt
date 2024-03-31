package com.franciscolinares.ubb.estadistica.ListViewEstadistica

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.franciscolinares.ubb.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.util.Locale

class AdaptadorMvp (private val mcontext: Context, private var listaJugadores: List<JugadorEstadistica>) :
ArrayAdapter<JugadorEstadistica>(mcontext, 0, listaJugadores) {

    private val db = Firebase.firestore

    @SuppressLint("ViewHolder", "SetTextI18n", "MissingInflatedId", "CutPasteId", "RtlHardcoded")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val prefs = PreferenceManager.getDefaultSharedPreferences(mcontext)
        val idPartido = prefs.getString("idPartido", "").toString()

        val layout: View = LayoutInflater.from(mcontext).inflate(R.layout.mvp_item, parent, false)
        val jugadorE = listaJugadores[position]

        db.collection("Estadisticas").document(idPartido).get()
            .addOnSuccessListener { esta ->
                val listJugador = esta.get("ListadoJugadores") as ArrayList<String>
                for (j in listJugador) {
                    val jugador = esta.get(j) as Map<String?, Any?>
                    if (jugador["equipo"].toString() == jugadorE.equipo && jugador["dorsal"].toString() == jugadorE.dorsal) {
                        db.collection("Jugadores").document(j).get()
                            .addOnSuccessListener { p->
                                if (p.get("UrlFoto") != "") {
                                    Picasso.get()
                                        .load(p.get("UrlFoto").toString())
                                        .placeholder(R.drawable.jugador_blanco)
                                        .error(R.drawable.jugador_blanco)
                                        .into(layout.findViewById<ImageView>(R.id.imgMVPJugador))
                                }
                            }
                    }
                }
            }
        layout.findViewById<TextView>(R.id.txtMVPNombre).text = jugadorE.nombre.toUpperCase(Locale.ROOT)
        layout.findViewById<TextView>(R.id.txtMVPDorsal).text = jugadorE.dorsal
        layout.findViewById<TextView>(R.id.txtMVPPuntos).text = jugadorE.puntos.toString()
        layout.findViewById<TextView>(R.id.txtMVPRebotes).text = jugadorE.rebotes.toString()
        layout.findViewById<TextView>(R.id.txtMVPAsistencias).text = jugadorE.asistencias.toString()
        layout.findViewById<TextView>(R.id.txtMVPFaltas).text = jugadorE.faltas.toString()
        layout.findViewById<TextView>(R.id.txtMVPRecuperaciones).text = jugadorE.recuperaciones.toString()
        layout.findViewById<TextView>(R.id.txtMVPPerdidas).text = jugadorE.perdidas.toString()
        layout.findViewById<TextView>(R.id.txtMVPTapones).text = jugadorE.tapones.toString()
        layout.findViewById<TextView>(R.id.txtMVPValoracion).text = jugadorE.valoracion.toString()

        return layout
    }

    fun updateData(newData: List<JugadorEstadistica>) {
        listaJugadores = newData
        notifyDataSetChanged() // Esto refresca la vista, pero no recrea todos los elementos
    }
}