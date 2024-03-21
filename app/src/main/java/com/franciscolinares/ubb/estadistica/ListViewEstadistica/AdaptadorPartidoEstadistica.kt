package com.franciscolinares.ubb.estadistica.ListViewEstadistica

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.partido.ListViewPartido.Partido
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class AdaptadorPartidoEstadistica(private val mcontext: Context, private val listaPartidos: List<Partido>) :
    ArrayAdapter<Partido>(mcontext, 0, listaPartidos) {

    private val db = Firebase.firestore

    @SuppressLint("ViewHolder", "SetTextI18n", "CutPasteId")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layout = LayoutInflater.from(mcontext).inflate(R.layout.partido_estadistica_item, parent, false)

        val partido = listaPartidos[position]

        layout.findViewById<TextView>(R.id.txtLVEquipoLocal).text = partido.local
        layout.findViewById<TextView>(R.id.txtLVEquipoVisitante).text = partido.visitante
        layout.findViewById<TextView>(R.id.txtLVPolideportivo).text = partido.polideportivo
        layout.findViewById<TextView>(R.id.txtLVFecha).text = partido.fecha
        layout.findViewById<TextView>(R.id.txtLVHora).text = partido.hora
        layout.findViewById<TextView>(R.id.txtLVEstadoPartido).text = partido.estado

        when (partido.estado) {
            "No Comenzado" -> {

            }
            "En Directo" -> {
                layout.findViewById<View>(R.id.lineaSeparadoraEstado).setBackgroundColor(Color.parseColor("#4CAF50"))
                layout.findViewById<TextView>(R.id.txtLVEstadoPartido).setTextColor(Color.parseColor("#4CAF50"))
            }
            "Finalizado" -> {
                layout.findViewById<TextView>(R.id.txtLVResultado).text = partido.resultado
                layout.findViewById<TextView>(R.id.txtLVResultado).setTextSize(TypedValue.COMPLEX_UNIT_SP, 28F)
                layout.findViewById<TextView>(R.id.txtLVResultado).visibility = View.VISIBLE
                layout.findViewById<TextView>(R.id.txtLVEstadoPartido).setTextColor(Color.RED)
                layout.findViewById<View>(R.id.lineaSeparadoraEstado).setBackgroundColor(Color.RED)

            }
        }

        db.collection("Equipos").document(partido.local).get()
            .addOnSuccessListener {
                if (it.get("UrlFoto") != "") {
                    Picasso.get()
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.escudopredeterminado)
                        .error(R.drawable.escudopredeterminado)
                        .into(layout.findViewById<ImageView>(R.id.imageLVLocal))
                }
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }

        db.collection("Equipos").document(partido.visitante).get()
            .addOnSuccessListener {
                if (it.get("UrlFoto") != "") {
                    Picasso.get()
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.escudopredeterminado)
                        .error(R.drawable.escudopredeterminado)
                        .into(layout.findViewById<ImageView>(R.id.imageLVVisitante))
                }
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
        return layout
    }
}