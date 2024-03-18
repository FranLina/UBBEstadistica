package com.franciscolinares.ubb.partido.ListViewPartido

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.util.Log
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

class AdaptadorPartido(private val mcontext: Context, private val listaPartidos: List<Partido>) :
    ArrayAdapter<Partido>(mcontext, 0, listaPartidos) {

    private val db = Firebase.firestore

    @SuppressLint("ViewHolder", "CutPasteId")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layout = LayoutInflater.from(mcontext).inflate(R.layout.partido_item, parent, false)

        val partido = listaPartidos[position]

        layout.findViewById<TextView>(R.id.txtLVEquipoLocal).text = partido.local
        layout.findViewById<TextView>(R.id.txtLVEquipoVisitante).text = partido.visitante
        layout.findViewById<TextView>(R.id.txtLVPolideportivo).text = partido.polideportivo
        layout.findViewById<TextView>(R.id.txtLVFecha).text = partido.fecha
        layout.findViewById<TextView>(R.id.txtLVHora).text = partido.hora
        layout.findViewById<TextView>(R.id.txtLVResultado).text = partido.resultado
        layout.findViewById<TextView>(R.id.txtLVEstado).text = partido.estado

        if (partido.estado == "No Comenzado") {
            layout.findViewById<TextView>(R.id.txtLVResultado).visibility = View.GONE

        } else if (partido.estado == "En Directo") {
            layout.findViewById<TextView>(R.id.txtLVEstado)
                .setBackgroundColor(Color.parseColor("#4CAF50"))
            layout.findViewById<TextView>(R.id.txtLVResultado).visibility = View.GONE

        } else if (partido.estado == "Finalizado") {
            layout.findViewById<TextView>(R.id.txtLVEstado).setBackgroundColor(Color.RED)
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