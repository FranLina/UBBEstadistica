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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.constraintlayout.widget.ConstraintSet.Layout
import com.bumptech.glide.Glide
import com.franciscolinares.ubb.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.util.Locale
import androidx.core.graphics.toColorInt

class AdaptadorMinuto(private val mcontext: Context, private var listaMinuto: List<MinutoAMinuto>) :
    ArrayAdapter<MinutoAMinuto>(mcontext, 0, listaMinuto) {

    private val db = Firebase.firestore

    @SuppressLint("ViewHolder", "SetTextI18n", "MissingInflatedId", "CutPasteId", "RtlHardcoded")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val prefs = PreferenceManager.getDefaultSharedPreferences(mcontext)
        val idPartido = prefs.getString("idPartido", "").toString()

        val layout: View
        val minuto = listaMinuto[position]

        layout = if (minuto.equipo == "Local") {
            LayoutInflater.from(mcontext).inflate(R.layout.minutoaminuto, parent, false)
        } else {
            LayoutInflater.from(mcontext).inflate(R.layout.minutoaminuto_visitante, parent, false)
        }

        cambiaFoto(minuto.imgAccion, layout.findViewById(R.id.imgAccionMAM))

        db.collection("Estadisticas").document(idPartido).get()
            .addOnSuccessListener { esta ->
                val listJugador = esta.get("ListadoJugadores") as ArrayList<String>
                for (j in listJugador) {
                    val jugador = esta.get(j) as Map<String?, Any?>
                    if (jugador["equipo"].toString() == minuto.equipo && jugador["dorsal"].toString() == minuto.dorsal) {
                        db.collection("Jugadores").document(j).get()
                            .addOnSuccessListener { p ->
                                if (p.get("UrlFoto") != "") {
                                    Glide.with(mcontext)
                                        .load(p.get("UrlFoto").toString())
                                        .placeholder(R.drawable.jugador_de_baloncesto)
                                        .override(120,100)
                                        .centerCrop()
                                        .error(R.drawable.jugador_de_baloncesto)
                                        .into(layout.findViewById(R.id.imgJugadorMAM))
                                }
                            }
                    }
                }
            }

        when (minuto.tipoFrase) {
            "1" -> {
                layout.findViewById<TextView>(R.id.txtLVMFrase).text = minuto.frase
                layout.findViewById<TextView>(R.id.txtLVM).text = "#" + minuto.dorsal + ", p " + minuto.cuarto + ", " + minuto.tiempo
                layout.findViewById<TextView>(R.id.txtLVMNombre).text = minuto.nombre.uppercase(Locale.ROOT)
                layout.findViewById<TextView>(R.id.txtLVMResultado).visibility = View.VISIBLE
                layout.findViewById<TextView>(R.id.txtLVMResultado).text = minuto.resultado
            }

            "2" -> {
                layout.setPaddingRelative(120, 0, 120, 0)
                layout.findViewById<ImageView>(R.id.imgAccionMAM).visibility = View.GONE
                layout.findViewById<TextView>(R.id.txtLVMFrase).setPadding(0, 30, 0, 10)
                layout.findViewById<TextView>(R.id.txtLVMFrase).text = minuto.frase + " " + minuto.cuarto
                layout.findViewById<TextView>(R.id.txtLVMFrase).setBackgroundColor("#FF4CAF50".toColorInt())
                layout.findViewById<TextView>(R.id.txtLVMFrase).setTextColor(Color.WHITE)
                layout.findViewById<TextView>(R.id.txtLVMFrase).gravity = Gravity.CENTER
                layout.findViewById<TextView>(R.id.txtLVM).setPadding(0, 0, 0, 20)
                layout.findViewById<TextView>(R.id.txtLVM).text = minuto.tiempo + " h."
                layout.findViewById<TextView>(R.id.txtLVM).gravity = Gravity.CENTER
                layout.findViewById<TextView>(R.id.txtLVM).setBackgroundColor("#FF4CAF50".toColorInt())
                layout.findViewById<TextView>(R.id.txtLVM).setTextColor(Color.WHITE)
                layout.findViewById<View>(R.id.lineaSeparadora).visibility = View.GONE
                layout.findViewById<TextView>(R.id.txtLVMNombre).visibility = View.GONE
                layout.findViewById<ImageView>(R.id.imgJugadorMAM).visibility = View.GONE
            }

            "3" -> {
                if (minuto.resultado == "") {
                    layout.findViewById<TextView>(R.id.txtLVMFrase).text = minuto.frase + " #" + minuto.dorsal
                    layout.findViewById<TextView>(R.id.txtLVM).text = "p " + minuto.cuarto + ", " + minuto.tiempo
                } else {
                    layout.findViewById<TextView>(R.id.txtLVMFrase).text = minuto.frase
                    layout.findViewById<TextView>(R.id.txtLVM).text = "#" + minuto.dorsal + ", p " + minuto.cuarto + ", " + minuto.tiempo
                }
                layout.findViewById<TextView>(R.id.txtLVMNombre).text = minuto.nombre.uppercase(Locale.ROOT)
            }

            "4" -> {
                layout.setPaddingRelative(120, 0, 120, 0)
                layout.findViewById<ImageView>(R.id.imgAccionMAM).visibility = View.GONE
                layout.findViewById<TextView>(R.id.txtLVMFrase).setPadding(0, 30, 0, 0)
                layout.findViewById<TextView>(R.id.txtLVMFrase).text = minuto.frase + " " + minuto.cuarto
                layout.findViewById<TextView>(R.id.txtLVMFrase).setBackgroundColor("#3A3A3A".toColorInt())
                layout.findViewById<TextView>(R.id.txtLVMFrase).setTextColor(Color.WHITE)
                layout.findViewById<TextView>(R.id.txtLVMFrase).gravity = Gravity.CENTER
                layout.findViewById<TextView>(R.id.txtLVM).setPadding(0, 0, 0, 20)
                layout.findViewById<TextView>(R.id.txtLVM).text = minuto.tiempo + " h."
                layout.findViewById<TextView>(R.id.txtLVM).gravity = Gravity.CENTER
                layout.findViewById<TextView>(R.id.txtLVM).setBackgroundColor("#3A3A3A".toColorInt())
                layout.findViewById<TextView>(R.id.txtLVM).setTextColor(Color.WHITE)
                layout.findViewById<View>(R.id.lineaSeparadora).visibility = View.GONE
                layout.findViewById<TextView>(R.id.txtLVMNombre).visibility = View.GONE
                layout.findViewById<ImageView>(R.id.imgJugadorMAM).visibility = View.GONE
            }
        }

        return layout
    }

    fun updateData(newData: List<MinutoAMinuto>) {
        listaMinuto = newData
        notifyDataSetChanged() // Esto refresca la vista, pero no recrea todos los elementos
    }

    private fun cambiaFoto(tipoImg: String, imagenAccion: ImageView) {
        val drawableRes = when (tipoImg) {
            "1" -> R.drawable.cambio
            "2" -> R.drawable.tmblanco
            "3" -> R.drawable.faltanb
            "4" -> R.drawable.tiro_libre
            "5" -> R.drawable.canasta1
            "6" -> R.drawable.canasta_fallada
            "7" -> R.drawable.canasta2
            "8" -> R.drawable.canasta3
            "9" -> R.drawable.asistencia
            "10" -> R.drawable.perdida
            "11" -> R.drawable.recuperacion
            "12" -> R.drawable.tapon_recibido
            "13" -> R.drawable.tapon_cometido
            "14" -> R.drawable.rebote
            else -> R.drawable.camiseta_de_baloncesto
        }

        Glide.with(mcontext)
            .load(drawableRes)
            .placeholder(drawableRes)
            .error(drawableRes)
            .into(imagenAccion)
    }

}