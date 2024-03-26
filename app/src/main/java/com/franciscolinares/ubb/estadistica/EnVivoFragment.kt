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
import com.franciscolinares.ubb.databinding.FragmentEnVivoBinding
import com.franciscolinares.ubb.estadistica.ListViewEstadistica.AdaptadorMinuto
import com.franciscolinares.ubb.estadistica.ListViewEstadistica.MinutoAMinuto
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class EnVivoFragment : Fragment() {

    private var _binding: FragmentEnVivoBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private var i = 0
    private val handler = Handler()
    private lateinit var myAdapter: AdaptadorMinuto
    private lateinit var listView: ListView

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

        recuperaInfo()

        db.collection("MinutoaMinuto").document(idPartido).get().addOnSuccessListener {
            val listaminuto = mutableListOf<MinutoAMinuto>()
            val lista = it.get("registro") as ArrayList<Map<String?, Any?>>
            for (i in (lista.count() - 1) downTo 0) {
                val minutoMap = lista[i]
                val minuto = MinutoAMinuto(
                    minutoMap["cuarto"].toString(),
                    minutoMap["dorsal"].toString(),
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
                binding.txtNombreELocal.text = "  " + it.get("EquipoLocal").toString()
                binding.txtNombreEVisitante.text = "  " + it.get("EquipoVisitante").toString()
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