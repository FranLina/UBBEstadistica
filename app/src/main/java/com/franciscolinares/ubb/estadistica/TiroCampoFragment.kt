package com.franciscolinares.ubb.estadistica

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.RectF
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentMVPBinding
import com.franciscolinares.ubb.databinding.FragmentTiroCampoBinding
import com.franciscolinares.ubb.partido.TiroView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.util.Locale
import kotlin.math.max

class TiroCampoFragment : Fragment() {

    private var _binding: FragmentTiroCampoBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private var i = 0
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTiroCampoBinding.inflate(inflater, container, false)
        val root = binding.root

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()

        i = binding.progressBar.progress

        recuperaInfo()
        recuperarAllTiros()

        Thread {
            while (i < 100) {
                i += 1
                handler.post {
                    binding.progressBar.progress = i
                }
                try {
                    Thread.sleep(125)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                if (i == 100) {
                    i = 0
                    recuperarAllTiros()
                    db.collection("Partidos").document(idPartido).get()
                        .addOnSuccessListener { partido ->
                            if (partido.get("Estado") != "Finalizado") {
                                db.collection("Partidos").document(idPartido).get()
                                    .addOnSuccessListener { documentSnapshot ->
                                        binding.txtPuntosLocalPartido.text =
                                            (documentSnapshot.get("Resultado").toString()
                                                .split(" - "))[0]
                                        binding.txtPuntosVisitantePartido.text =
                                            (documentSnapshot.get("Resultado").toString()
                                                .split(" - "))[1]
                                    }
                            }
                        }
                }
            }
        }.start()

        return root
    }

    private fun recuperarAllTiros() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()

        binding.campoTiro.viewTreeObserver.addOnGlobalLayoutListener {
            if (binding.campoTiro.width > 0 && binding.campoTiro.height > 0) {
                db.collection("Estadisticas").document(idPartido).get()
                    .addOnSuccessListener {
                        val listJugador = it.get("ListadoJugadores") as ArrayList<String>
                        for (j in 0..<listJugador.count()) {
                            val jugador = it.get(listJugador[j]) as Map<String?, Any?>
                            val tiros = jugador["tiros"] as ArrayList<Map<String, Any>>
                            for (tiro in tiros) {
                                binding.tiroView2.agregarTiro(
                                    tiro["x"].toString().toFloat(),
                                    tiro["y"].toString().toFloat(),
                                    tiro["encestado"] as Boolean,
                                    jugador["equipo"] as String
                                )
                            }
                        }
                    }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun recuperaInfo() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()

        db.collection("Partidos").document(idPartido).get()
            .addOnSuccessListener {
                binding.txtNombreELocal.text = "  " + it.get("EquipoLocal").toString().toUpperCase(
                    Locale.ROOT
                )
                binding.txtNombreEVisitante.text = "  " + it.get("EquipoVisitante").toString().toUpperCase(Locale.ROOT)
                binding.txtPuntosLocalPartido.text =
                    (it.get("Resultado").toString().split(" - "))[0]
                binding.txtPuntosVisitantePartido.text =
                    (it.get("Resultado").toString().split(" - "))[1]
                cargaEscudos(it.get("EquipoLocal").toString(), it.get("EquipoVisitante").toString())
            }
    }

    private fun cargaEscudos(eLocal: String, eVisitante: String) {

        db.collection("Equipos").document(eLocal).get()
            .addOnSuccessListener {
                if (it.get("UrlFoto") != "") {
                    Picasso.get()
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.escudo_equipo)
                        .error(R.drawable.escudo_equipo)
                        .into(binding.imageLocalPartido)
                }
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }

        db.collection("Equipos").document(eVisitante).get()
            .addOnSuccessListener {
                if (it.get("UrlFoto") != "") {
                    Picasso.get()
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.escudo_equipo)
                        .error(R.drawable.escudo_equipo)
                        .into(binding.imageVisitantePartido)
                }
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }


}