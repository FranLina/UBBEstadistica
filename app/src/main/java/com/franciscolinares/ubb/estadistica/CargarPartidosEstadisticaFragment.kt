package com.franciscolinares.ubb.estadistica

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentCargarPartidosEstadisticaBinding
import com.franciscolinares.ubb.databinding.FragmentPartidoBinding
import com.franciscolinares.ubb.estadistica.ListViewEstadistica.AdaptadorPartidoEstadistica
import com.franciscolinares.ubb.partido.ListViewPartido.Partido
import com.franciscolinares.ubb.user.MainActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.navigation.findNavController
import androidx.core.content.edit

class CargarPartidosEstadisticaFragment : Fragment() {

    private var _binding: FragmentCargarPartidosEstadisticaBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCargarPartidosEstadisticaBinding.inflate(inflater, container, false)
        val root: View = binding.root

        db.collection("Partidos").orderBy("Fecha").get().addOnSuccessListener { datos ->
            val listaPartido = mutableListOf<Partido>()
            for (partido in datos) {
                val fechaString = partido.getString("Fecha").toString()
                val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fechaDate = formato.parse(fechaString) ?: Date()
                val p = Partido(
                    partido.id,
                    partido.get("EquipoLocal").toString(),
                    partido.get("EquipoVisitante").toString(),
                    partido.get("EquipoLocal").toString().dropLast(2),
                    partido.get("EquipoVisitante").toString().dropLast(2),
                    partido.get("Polideportivo").toString(),
                    partido.get("Resultado").toString(),
                    partido.get("Cuartos") as List<String>,
                    partido.get("Hora").toString(),
                    partido.get("Fecha").toString(),
                    fechaDate,
                    partido.get("Estado").toString()
                )
                listaPartido.add(p)
            }

            val listaOrdenada = listaPartido.sortedBy { it.fechaDate }

            val adapter = AdaptadorPartidoEstadistica(binding.root.context, listaOrdenada)

            binding.ListViewPartidoEstadistica.adapter = adapter

            binding.ListViewPartidoEstadistica.setOnItemClickListener { adapterView, view, i, l ->
                if (listaOrdenada[i].estado != "No Comenzado") {
                    val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
                    prefs.edit() {
                        putString("idPartido", listaOrdenada[i].id)
                    }
                    binding.root.findNavController()
                        .navigate(R.id.action_cargarPartidosEstadisticaFragment_to_cargaPartidoActivity)
                }
            }
        }

        return root
    }
}