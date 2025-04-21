package com.franciscolinares.ubb.invitado

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentPartidosEquipoinvitadoBinding
import com.franciscolinares.ubb.estadistica.ListViewEstadistica.AdaptadorPartidoEstadistica
import com.franciscolinares.ubb.partido.ListViewPartido.Partido
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.navigation.findNavController
import androidx.core.content.edit
import com.franciscolinares.ubb.estadistica.CargaPartidoActivity
import com.squareup.picasso.Picasso

class PartidosEquipoinvitadoFragment : Fragment() {

    private var _binding: FragmentPartidosEquipoinvitadoBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPartidosEquipoinvitadoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idEquipo = prefs.getString("idEquipo", "").toString()

        db.collection("Partidos").orderBy("Fecha").get().addOnSuccessListener {
            val listaPartido = mutableListOf<Partido>()
            for (partido in it) {
                if (partido["EquipoLocal"].toString() == idEquipo || partido["EquipoVisitante"].toString() == idEquipo) {
                    val fechaString = partido.getString("Fecha").toString()
                    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val fechaDate = formato.parse(fechaString) ?: Date()
                    val p = Partido(
                        partido.id,
                        partido.get("EquipoLocal").toString(),
                        partido.get("EquipoVisitante").toString(),
                        partido.get("Polideportivo").toString(),
                        partido.get("Resultado").toString(),
                        partido.get("Hora").toString(),
                        partido.get("Fecha").toString(),
                        fechaDate,
                        partido.get("Estado").toString()
                    )
                    listaPartido.add(p)
                }
            }

            val listaOrdenada = listaPartido.sortedBy { it.fechaDate }

            val adapter = AdaptadorPartidoEstadistica(binding.root.context, listaOrdenada)

            binding.LVPartidosInvitado.adapter = adapter

            binding.LVPartidosInvitado.setOnItemClickListener { adapterView, view, i, l ->
                if (listaOrdenada[i].estado != "No Comenzado") {
                    val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
                    prefs.edit() {
                        putString("idPartido", listaOrdenada[i].id)
                    }
                    val intent = Intent(binding.root.context, CargaPartidoActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        return root
    }

}