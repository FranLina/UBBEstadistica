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
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ListView
import android.widget.TextView
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentTiroCampoBinding
import com.franciscolinares.ubb.estadistica.ListViewEstadistica.AdaptadorTiroJugador
import com.franciscolinares.ubb.estadistica.ListViewEstadistica.TiroJugador
import com.franciscolinares.ubb.partido.ListViewPartido.AdaptadorJugadorConvocado
import com.franciscolinares.ubb.partido.ListViewPartido.JugadorConvocado
import com.franciscolinares.ubb.partido.TiroView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.util.Locale

class TiroCampoFragment : Fragment() {

    private var _binding: FragmentTiroCampoBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private var i = 0
    private val handler = Handler()
    private lateinit var myAdapterLocal: AdaptadorTiroJugador
    private lateinit var myAdapterVistante: AdaptadorTiroJugador
    private lateinit var listViewLocal: ListView
    private lateinit var listViewVisitante: ListView
    private val listaPlantillaLocal = mutableListOf<TiroJugador>()
    private val listaPlantillaVisitante = mutableListOf<TiroJugador>()
    private var jugadores: ArrayList<Map<String?, Any?>> = arrayListOf()

    private val checkBoxListener = CompoundButton.OnCheckedChangeListener { _, _ ->
        recuperarTiros()
    }

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


        // Asignar el listener a todos los CheckBox
        binding.cbCuarto1.setOnCheckedChangeListener(checkBoxListener)
        binding.cbCuarto2.setOnCheckedChangeListener(checkBoxListener)
        binding.cbCuarto3.setOnCheckedChangeListener(checkBoxListener)
        binding.cbCuarto4.setOnCheckedChangeListener(checkBoxListener)

        recuperaInfo()
        binding.campoTiro.viewTreeObserver.addOnGlobalLayoutListener {
            recuperarTiros()
        }

        listViewLocal = binding.LVJugadorTiroLocal
        listViewVisitante = binding.LVJugadorTiroVisitante

        recuperarJugadores(root)

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
                                recuperarTiros()
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

    private fun recuperarJugadores(viewDialog: View) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()

        listaPlantillaLocal.add(TiroJugador("TODOS", "", "", true, true))
        listaPlantillaVisitante.add(TiroJugador("TODOS", "", "", true, true))

        db.collection("Estadisticas").document(idPartido).get()
            .addOnSuccessListener {
                val listJugador = it.get("ListadoJugadores") as ArrayList<String>
                for (j in 0..<listJugador.count()) {
                    jugadores.add((it.get(listJugador[j]) as Map<String?, Any?>))
                }
                val jugadoresOrdenados = jugadores.sortedWith(compareBy<Map<String?, Any?>> { it["equipo"] as? String }.thenBy {
                    (it["dorsal"] as? String)?.toIntOrNull() ?: Int.MAX_VALUE
                })
                for (j in 0..<jugadoresOrdenados.count()) {
                    val jugador = jugadoresOrdenados[j]
                    val ju = TiroJugador(
                        jugador["nombre"].toString(),
                        jugador["dorsal"].toString(),
                        jugador["equipo"].toString(),
                        true,
                        false
                    )
                    if (jugador["equipo"] == "Local") {
                        listaPlantillaLocal.add(ju)
                    } else {
                        listaPlantillaVisitante.add(ju)
                    }
                }
                myAdapterLocal = AdaptadorTiroJugador(viewDialog.context, listaPlantillaLocal) { recuperarTiros() }
                listViewLocal.adapter = myAdapterLocal

                myAdapterVistante = AdaptadorTiroJugador(viewDialog.context, listaPlantillaVisitante) { recuperarTiros() }
                listViewVisitante.adapter = myAdapterVistante

            }
    }

    private fun recuperarTiros() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()

        val cuartos = comprobarCheckBoxCuartos()
        val jugadoresL = comprobarCheckBoxJugador(listViewLocal)
        val jugadoresV = comprobarCheckBoxJugador(listViewVisitante)

        if (cuartos.isNotEmpty()) {
            if (binding.campoTiro.width > 0 && binding.campoTiro.height > 0) {
                db.collection("Estadisticas").document(idPartido).get()
                    .addOnSuccessListener {
                        val listJugador = it.get("ListadoJugadores") as ArrayList<String>
                        val tiros: ArrayList<TiroView.Tiro> = arrayListOf()
                        for (j in 0..<listJugador.count()) {
                            val jugador = it.get(listJugador[j]) as Map<String?, Any?>
                            val tirosJ = jugador["tiros"] as ArrayList<Map<String, Any>>

                            if (jugador["equipo"] == "Local" && jugadoresL.contains(jugador["dorsal"])) {
                                for (tiro in tirosJ) {
                                    if (cuartos.contains(tiro["cuarto"].toString().toInt())) {
                                        tiros.add(
                                            TiroView.Tiro(
                                                tiro["x"].toString().toFloat(),
                                                tiro["y"].toString().toFloat(),
                                                tiro["encestado"] as Boolean,
                                                jugador["equipo"] as String
                                            )
                                        )
                                    }
                                }
                            } else if (jugador["equipo"] == "Visitante" && jugadoresV.contains(jugador["dorsal"])) {
                                for (tiro in tirosJ) {
                                    if (cuartos.contains(tiro["cuarto"].toString().toInt())) {
                                        tiros.add(
                                            TiroView.Tiro(
                                                tiro["x"].toString().toFloat(),
                                                tiro["y"].toString().toFloat(),
                                                tiro["encestado"] as Boolean,
                                                jugador["equipo"] as String
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        binding.tiroView2.mostrarTiros(tiros)
                    }
            }
        } else {
            val tiros: ArrayList<TiroView.Tiro> = arrayListOf()
            binding.tiroView2.mostrarTiros(tiros)
        }
    }

    private fun comprobarCheckBoxCuartos(): ArrayList<Int> {
        val cuartos = ArrayList<Int>()
        if (binding.cbCuarto1.isChecked)
            cuartos.add(1)
        if (binding.cbCuarto2.isChecked)
            cuartos.add(2)
        if (binding.cbCuarto3.isChecked)
            cuartos.add(3)
        if (binding.cbCuarto4.isChecked)
            cuartos.add(4)
        return cuartos
    }

    private fun comprobarCheckBoxJugador(listView: ListView): ArrayList<String> {
        val seleccionados = ArrayList<String>()
        for (i in 0 until listView.childCount) {
            val itemView = listView.getChildAt(i) // Obtiene la vista del elemento
            val checkBox = itemView?.findViewById<CheckBox>(R.id.cbTiroJugador)
            val dorsal = itemView?.findViewById<TextView>(R.id.txtDorsalJugadorTiro)

            if (checkBox != null && checkBox.isChecked) {
                if (dorsal != null) {
                    seleccionados.add(dorsal.text.toString())
                }
            }
        }

        return seleccionados
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