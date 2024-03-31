package com.franciscolinares.ubb.estadistica

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentMVPBinding
import com.franciscolinares.ubb.estadistica.ListViewEstadistica.AdaptadorMvp
import com.franciscolinares.ubb.estadistica.ListViewEstadistica.JugadorEstadistica
import com.franciscolinares.ubb.estadistica.ListViewEstadistica.MinutoAMinuto
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.util.Locale

class MVPFragment : Fragment() {

    private var _binding: FragmentMVPBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private var i = 0
    private val handler = Handler()
    private lateinit var myAdapterLocal: AdaptadorMvp
    private lateinit var listViewLocal: ListView
    private lateinit var myAdapterVisitante: AdaptadorMvp
    private lateinit var listViewVisitante: ListView
    private var jugLocal: ArrayList<JugadorEstadistica> = arrayListOf()
    private var jugVisitante: ArrayList<JugadorEstadistica> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMVPBinding.inflate(inflater, container, false)
        val root = binding.root

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()
        listViewLocal = binding.LVMVPLocal
        listViewVisitante = binding.LVMVPVisitante

        recuperaInfo()

        db.collection("Estadisticas").document(idPartido).get()
            .addOnSuccessListener {
                val listJugador = it.get("ListadoJugadores") as ArrayList<String>
                val jugadores: ArrayList<Map<String?, Any?>> = arrayListOf()
                for (j in 0..<listJugador.count()) {
                    val jugador = it.get(listJugador[j]) as Map<String?, Any?>
                    jugadores.add(jugador)
                }
                jugLocal = obtenerMejoresJugadores(3, "Local", jugadores)
                myAdapterLocal = AdaptadorMvp(binding.root.context, jugLocal)
                listViewLocal.adapter = myAdapterLocal

                jugVisitante = obtenerMejoresJugadores(3, "Visitante", jugadores)
                myAdapterVisitante = AdaptadorMvp(binding.root.context, jugVisitante)
                listViewVisitante.adapter = myAdapterVisitante
            }

        i = binding.progressBar.progress

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
                    db.collection("Partidos").document(idPartido).get()
                        .addOnSuccessListener { partido ->
                            if (partido.get("Estado") != "Finalizado") {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador = it.get("ListadoJugadores") as ArrayList<String>
                                        val jugadores: ArrayList<Map<String?, Any?>> = arrayListOf()
                                        for (j in 0..<listJugador.count()) {
                                            val jugador = it.get(listJugador[j]) as Map<String?, Any?>
                                            jugadores.add(jugador)
                                        }
                                        jugLocal = obtenerMejoresJugadores(3, "Local", jugadores)
                                        myAdapterLocal.updateData(jugLocal)

                                        jugVisitante = obtenerMejoresJugadores(3, "Visitante", jugadores)
                                        myAdapterVisitante.updateData(jugVisitante)
                                    }

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

    private fun obtenerMejoresJugadores(cantidad: Int, equipo: String, listaJugadores: ArrayList<Map<String?, Any?>>): ArrayList<JugadorEstadistica> {
        val jugadoresEstadistica = mutableListOf<JugadorEstadistica>()
        val jugadoresEquipo = listaJugadores.filter {
            it["equipo"] == equipo
        }
        val jugadoresOrdenados = jugadoresEquipo.sortedByDescending {
            it["val"] as Long
        }
        val mejoresJugadores = ArrayList(jugadoresOrdenados.take(cantidad))
        for (j in mejoresJugadores) {
            val dato = JugadorEstadistica(
                j["dorsal"].toString(),
                j["nombre"].toString(),
                j["equipo"].toString(),
                j["puntos"].toString().toInt(),
                (j["rebO"].toString().toInt() + j["rebD"].toString().toInt()),
                j["asi"].toString().toInt(),
                j["falC"].toString().toInt(),
                j["recu"].toString().toInt(),
                j["per"].toString().toInt(),
                j["taCom"].toString().toInt(),
                j["val"].toString().toInt()
            )
            jugadoresEstadistica.add(dato)
        }
        return ArrayList(jugadoresEstadistica)
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
                binding.txtNombreELocalMVP.text = it.get("EquipoLocal").toString().toUpperCase(Locale.ROOT)
                binding.txtNombreEVisitanteMVP.text = it.get("EquipoVisitante").toString().toUpperCase(Locale.ROOT)
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
                    Picasso.get()
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.escudo_equipo)
                        .error(R.drawable.escudo_equipo)
                        .into(binding.imageLocalPartidoMVP)
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
                    Picasso.get()
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.escudo_equipo)
                        .error(R.drawable.escudo_equipo)
                        .into(binding.imageVisitantePartidoMVP)
                }
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }

}