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
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentEnVivoBinding
import com.franciscolinares.ubb.estadistica.ListViewEstadistica.AdaptadorMinuto
import com.franciscolinares.ubb.estadistica.ListViewEstadistica.MinutoAMinuto
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.util.Locale

class EnVivoFragment : Fragment() {

    private var _binding: FragmentEnVivoBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private var i = 0
    private val handler = Handler()
    private lateinit var myAdapter: AdaptadorMinuto
    private lateinit var listView: ListView

    private var jugadores: ArrayList<Map<String?, Any?>> = arrayListOf()
    private var quintetoL: ArrayList<String> = arrayListOf()
    private var quintetoV: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEnVivoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()
        listView = binding.LVMinutoAMinuto

        binding.txtBTJP.setOnClickListener {
            if (binding.contenedorJugCam.visibility == View.VISIBLE) {
                binding.contenedorJugCam.visibility = View.GONE
            } else {
                recuperaQuinteto()
                jugadores.clear()
                db.collection("Estadisticas").document(idPartido).get()
                    .addOnSuccessListener {
                        val listJugador = it.get("ListadoJugadores") as ArrayList<String>

                        for (j in 0..<listJugador.count()) {
                            val jugador = it.get(listJugador[j]) as Map<String?, Any?>
                            for (i in 0..<quintetoL.count()) {
                                if (jugador["dorsal"] == quintetoL[i] && jugador["equipo"] == "Local")
                                    jugadores.add(jugador)
                            }
                            for (i in 0..<quintetoV.count()) {
                                if (jugador["dorsal"] == quintetoV[i] && jugador["equipo"] == "Visitante")
                                    jugadores.add(jugador)
                            }

                        }
                        recuperaDatosEstadistica(jugadores)
                        binding.contenedorJugCam.visibility = View.VISIBLE
                    }
            }
        }

        recuperaInfo()

        db.collection("MinutoaMinuto").document(idPartido).get().addOnSuccessListener {
            val listaminuto = mutableListOf<MinutoAMinuto>()
            val lista = it.get("registro") as ArrayList<Map<String?, Any?>>
            for (i in (lista.count() - 1) downTo 0) {
                val minutoMap = lista[i]
                val minuto = MinutoAMinuto(
                    minutoMap["cuarto"].toString(),
                    minutoMap["dorsal"].toString(),
                    minutoMap["nombre"].toString(),
                    minutoMap["equipo"].toString(),
                    minutoMap["frase"].toString(),
                    minutoMap["resultado"].toString(),
                    minutoMap["tiempo"].toString(),
                    minutoMap["tipoFrase"].toString(),
                    minutoMap["tipoImg"].toString()
                )
                listaminuto.add(minuto)
            }
            myAdapter = AdaptadorMinuto(binding.root.context, listaminuto)
            listView.adapter = myAdapter
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
                                db.collection("MinutoaMinuto").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listaminuto = mutableListOf<MinutoAMinuto>()
                                        val lista =
                                            it.get("registro") as ArrayList<Map<String?, Any?>>
                                        for (i in (lista.count() - 1) downTo 0) {
                                            val minutoMap = lista[i]
                                            val minuto = MinutoAMinuto(
                                                minutoMap["cuarto"].toString(),
                                                minutoMap["dorsal"].toString(),
                                                minutoMap["nombre"].toString(),
                                                minutoMap["equipo"].toString(),
                                                minutoMap["frase"].toString(),
                                                minutoMap["resultado"].toString(),
                                                minutoMap["tiempo"].toString(),
                                                minutoMap["tipoFrase"].toString(),
                                                minutoMap["tipoImg"].toString()
                                            )
                                            listaminuto.add(minuto)
                                        }
                                        myAdapter.updateData(listaminuto)
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

                                recuperaQuinteto()
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        val newJugadores: ArrayList<Map<String?, Any?>> =
                                            arrayListOf()

                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                it.get(listJugador[j]) as Map<String?, Any?>
                                            for (i in 0..<quintetoL.count()) {
                                                if (jugador["dorsal"] == quintetoL[i] && jugador["equipo"] == "Local")
                                                    newJugadores.add(jugador)
                                            }
                                            for (i in 0..<quintetoV.count()) {
                                                if (jugador["dorsal"] == quintetoV[i] && jugador["equipo"] == "Visitante")
                                                    newJugadores.add(jugador)
                                            }

                                        }
                                        actualizaDatosEstadistica(newJugadores)
                                    }
                            }
                        }
                }
            }
        }.start()

        return root
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
                binding.txtNombreEVisitante.text = "  " + it.get("EquipoVisitante").toString().toUpperCase(
                    Locale.ROOT
                )
                binding.txtEquipoLJC.text = it.get("EquipoLocal").toString().toUpperCase(Locale.ROOT)
                binding.txtEquipoVJC.text = it.get("EquipoVisitante").toString().toUpperCase(Locale.ROOT)
                binding.txtPuntosLocalPartido.text =
                    (it.get("Resultado").toString().split(" - "))[0]
                binding.txtPuntosVisitantePartido.text =
                    (it.get("Resultado").toString().split(" - "))[1]
                cargaEscudos(it.get("EquipoLocal").toString(), it.get("EquipoVisitante").toString())
            }
    }

    private fun recuperaQuinteto() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()

        db.collection("Partidos").document(idPartido).get()
            .addOnSuccessListener {
                quintetoL = it.get("QuintetoL") as ArrayList<String>
                quintetoV = it.get("QuintetoV") as ArrayList<String>
            }
    }

    @SuppressLint("SetTextI18n", "InflateParams", "ResourceAsColor")
    private fun recuperaDatosEstadistica(jugadores: ArrayList<Map<String?, Any?>>) {

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()

        binding.TLEquipoLJC.removeAllViews()
        binding.TLEquipoVJC.removeAllViews()

        val cabeceraLJC = LayoutInflater.from(binding.root.context)
            .inflate(R.layout.row_cabecera_jugadores_campo, null, false)
        binding.TLEquipoLJC.addView(cabeceraLJC)

        val cabeceraVJC = LayoutInflater.from(binding.root.context)
            .inflate(R.layout.row_cabecera_jugadores_campo, null, false)
        binding.TLEquipoVJC.addView(cabeceraVJC)

        for (j in 0..<jugadores.count()) {

            val jugador = jugadores[j]

            val registro = LayoutInflater.from(binding.root.context)
                .inflate(R.layout.row_jugadores_campo, null, false)

            registro.findViewById<TextView>(R.id.txtJCDorsal).text = jugador["dorsal"].toString()

            db.collection("Estadisticas").document(idPartido).get()
                .addOnSuccessListener { it ->
                    val listJugador = it.get("ListadoJugadores") as ArrayList<String>

                    for (j in 0..<listJugador.count()) {
                        val player = it.get(listJugador[j]) as HashMap<String?, Any?>
                        if (player["dorsal"] == jugador["dorsal"] && player["equipo"] == jugador["equipo"]) {
                            db.collection("Jugadores").document(listJugador[j]).get()
                                .addOnSuccessListener { p ->
                                    if (p.get("UrlFoto") != "") {
                                        Picasso.get()
                                            .load(p.get("UrlFoto").toString())
                                            .placeholder(R.drawable.jugador_blanco)
                                            .error(R.drawable.jugador_blanco)
                                            .into(registro.findViewById<ImageView>(R.id.imgJCFoto))
                                    }
                                }
                        }

                    }
                }

            registro.findViewById<TextView>(R.id.txtJCNombre).text =
                if (jugador["nombre"].toString().length > 23) {
                    "${jugador["nombre"].toString().toUpperCase(Locale.ROOT).substring(0, 20)}..."
                } else {
                    jugador["nombre"].toString().toUpperCase(Locale.ROOT)
                }
            registro.findViewById<TextView>(R.id.txtJCPuntos).text = jugador["puntos"].toString()
            registro.findViewById<TextView>(R.id.txtJCRebotes).text =
                (jugador["rebO"].toString().toInt() + jugador["rebD"].toString().toInt()).toString()
            registro.findViewById<TextView>(R.id.txtJCAsistencias).text = jugador["asi"].toString()
            registro.findViewById<TextView>(R.id.txtJCFalta).text = jugador["falC"].toString()
            registro.findViewById<TextView>(R.id.txtJCValoracion).text = jugador["val"].toString()

            if (jugador["equipo"] == "Local") {
                binding.TLEquipoLJC.addView(registro)
            } else {
                binding.TLEquipoVJC.addView(registro)
            }
        }
    }

    private fun actualizaDatosEstadistica(
        newJugadores: ArrayList<Map<String?, Any?>>
    ) {
        // Actualizar los datos subyacentes
        jugadores.clear()
        jugadores.addAll(newJugadores)

        // Llamar a recuperaDatosEstadistica para reflejar los cambios en la UI
        recuperaDatosEstadistica(jugadores)
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
                        .into(binding.imgEquipoLJC)
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
                        .into(binding.imgEquipoVJC)
                }
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }

}