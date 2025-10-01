package com.franciscolinares.ubb.estadistica.ListViewEstadistica

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.partido.ListViewPartido.Partido
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class AdaptadorPartidoEstadistica(private val mcontext: Context, private val listaPartidos: List<Partido>) :
    ArrayAdapter<Partido>(mcontext, 0, listaPartidos) {

    private val db = Firebase.firestore

    @SuppressLint("ViewHolder", "SetTextI18n", "CutPasteId", "MissingInflatedId", "ResourceAsColor")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layout = LayoutInflater.from(mcontext).inflate(R.layout.partido_estadistica_item, parent, false)

        val partido = listaPartidos[position]

        val linearLocal = layout.findViewById<LinearLayout>(R.id.LLLocal)
        val linearVisitante = layout.findViewById<LinearLayout>(R.id.LLVisitante)

        layout.findViewById<TextView>(R.id.txtLVEquipoLocal).text = partido.nombreLocal.uppercase()
        layout.findViewById<TextView>(R.id.txtLVEquipoVisitante).text = partido.nombreVisitante.uppercase()
        layout.findViewById<TextView>(R.id.txtLVPolideportivo).text = "📍 " + partido.polideportivo
        layout.findViewById<TextView>(R.id.txtLVFecha).text = partido.fecha
        layout.findViewById<TextView>(R.id.txtLVHora).text = partido.hora
        layout.findViewById<TextView>(R.id.txtLVDia).text = obtenerDiaDeLaSemana(partido.fecha)
        layout.findViewById<TextView>(R.id.txtLVEstadoPartido).text = "🏀 " + partido.estado

        when (partido.estado) {
            "No Comenzado" -> {

            }

            "En Directo" -> {
                layout.findViewById<TextView>(R.id.txtLVEstadoPartido).setBackgroundResource(R.drawable.background_redondo_bottom_all_green)
                layout.findViewById<TextView>(R.id.txtLVEstadoPartido).setTextColor(Color.WHITE)
                layout.findViewById<LinearLayout>(R.id.LLResultado).visibility = View.GONE
            }

            "Finalizado" -> {
                layout.findViewById<TextView>(R.id.txtLVRLocal).text = partido.resultado.split(" - ")[0]
                layout.findViewById<TextView>(R.id.txtLVRVisitante).text = partido.resultado.split(" - ")[1]
                if (comprobarGanador(partido.resultado) == 1) {
                    layout.findViewById<TextView>(R.id.txtLVRLocal).setTextColor(ContextCompat.getColor(layout.context, R.color.yellow_logo_700))
                } else {
                    layout.findViewById<TextView>(R.id.txtLVRVisitante).setTextColor(ContextCompat.getColor(layout.context, R.color.yellow_logo_700))
                }
                layout.findViewById<TextView>(R.id.txtLVEstadoPartido).setBackgroundResource(R.drawable.background_redondo_bottom_all_red)
                layout.findViewById<TextView>(R.id.txtLVEstadoPartido).setTextColor(Color.WHITE)
                if (partido.cuartos.isNotEmpty()) {
                    mostrarResultadosPorCuarto(partido.cuartos, linearLocal, linearVisitante, layout.context)
                    layout.findViewById<LinearLayout>(R.id.LLCuartos).visibility = View.VISIBLE
                    layout.findViewById<View>(R.id.LineaSeparar).visibility = View.VISIBLE
                }

            }
        }

        db.collection("Equipos").document(partido.local).get()
            .addOnSuccessListener {
                if (it.get("UrlFoto") != "") {

                    Glide.with(mcontext)
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.escudopredeterminado)
                        .error(R.drawable.escudopredeterminado)
                        .into(layout.findViewById(R.id.imageLVLocal))

                    Glide.with(mcontext)
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.escudopredeterminado)
                        .error(R.drawable.escudopredeterminado)
                        .into(layout.findViewById(R.id.imageLVCuartoLocal))

                }
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }

        db.collection("Equipos").document(partido.visitante).get()
            .addOnSuccessListener {
                if (it.get("UrlFoto") != "") {

                    Glide.with(mcontext)
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.escudopredeterminado)
                        .error(R.drawable.escudopredeterminado)
                        .into(layout.findViewById(R.id.imageLVVisitante))

                    Glide.with(mcontext)
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.escudopredeterminado)
                        .error(R.drawable.escudopredeterminado)
                        .into(layout.findViewById(R.id.imageLVCuartoVisitante))
                }
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
        return layout
    }

    private fun mostrarResultadosPorCuarto(
        resultados: List<String>,
        layoutEquipoLocal: LinearLayout,
        layoutEquipoVisitante: LinearLayout,
        context: Context
    ) {
        // Limpia layouts por si ya tienen datos previos
        //layoutEquipoLocal.removeAllViews()
        //layoutEquipoVisitante.removeAllViews()

        for (resultado in resultados) {
            val partes = resultado.split(" - ").map { it.trim() }

            if (partes.size != 2) continue  // Ignora formatos incorrectos

            val puntosEquipo1 = partes[0].toIntOrNull()
            val puntosEquipo2 = partes[1].toIntOrNull()

            if (puntosEquipo1 == null || puntosEquipo2 == null) continue

            val textViewEquipo1 = TextView(context)
            val textViewEquipo2 = TextView(context)

            textViewEquipo1.text = puntosEquipo1.toString()
            textViewEquipo2.text = puntosEquipo2.toString()

            // Color amarillo para el equipo con más puntos, gris para el otro
            if (puntosEquipo1 > puntosEquipo2) {
                textViewEquipo1.setTextColor(ContextCompat.getColor(context, R.color.yellow_logo_700))
                textViewEquipo2.setTextColor(Color.GRAY)
            } else if (puntosEquipo2 > puntosEquipo1) {
                textViewEquipo1.setTextColor(Color.GRAY)
                textViewEquipo2.setTextColor(ContextCompat.getColor(context, R.color.yellow_logo_700))
            } else {
                // Mismo color si empatan
                textViewEquipo1.setTextColor(Color.DKGRAY)
                textViewEquipo2.setTextColor(Color.DKGRAY)
            }

            // Estilos opcionales (puedes ajustar según diseño)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // Peso = 1
            )
            textViewEquipo1.layoutParams = params
            textViewEquipo2.layoutParams = params
            textViewEquipo1.textSize = 14f
            textViewEquipo2.textSize = 14f
            textViewEquipo1.setTypeface(null, Typeface.BOLD)
            textViewEquipo2.setTypeface(null, Typeface.BOLD)
            textViewEquipo1.setPadding(6, 5, 6, 5)
            textViewEquipo2.setPadding(6, 5, 6, 5)

            layoutEquipoLocal.addView(textViewEquipo1)
            layoutEquipoVisitante.addView(textViewEquipo2)
        }
    }


    private fun comprobarGanador(resultado: String): Int {
        val ptsL = resultado.split(" - ")[0].toInt()
        val ptsV = resultado.split(" - ")[1].toInt()

        return if (ptsL > ptsV) 1 else 0
    }

    private fun obtenerDiaDeLaSemana(fecha: String, formato: String = "dd/MM/yyyy"): String {
        val sdf = SimpleDateFormat(formato, Locale("es", "ES"))
        val date = sdf.parse(fecha)
        val diaSemana = SimpleDateFormat("EEEE", Locale("es", "ES")).format(date)
        return diaSemana.replaceFirstChar { it.uppercase() }
    }
}