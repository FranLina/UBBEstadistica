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

        db.collection("Partidos").orderBy("Fecha").get().addOnSuccessListener {
            val listaPartido = mutableListOf<Partido>()
            for (partido in it) {
                    val p = Partido(
                        partido.id,
                        partido.get("EquipoLocal").toString(),
                        partido.get("EquipoVisitante").toString(),
                        partido.get("Polideportivo").toString(),
                        partido.get("Resultado").toString(),
                        partido.get("Hora").toString(),
                        partido.get("Fecha").toString(),
                        partido.get("Estado").toString()
                    )
                listaPartido.add(p)
            }

            val adapter = AdaptadorPartidoEstadistica(binding.root.context, listaPartido)

            binding.ListViewPartidoEstadistica.adapter = adapter

            binding.ListViewPartidoEstadistica.setOnItemClickListener { adapterView, view, i, l ->
                if (listaPartido[i].estado != "No Comenzado") {
                    val prefs =
                        PreferenceManager.getDefaultSharedPreferences(binding.root.context)
                    val editor = prefs.edit()
                    editor.putString("idPartido", listaPartido[i].id)
                    editor.apply()
                    Navigation.findNavController(binding.root)
                        .navigate(R.id.action_cargarPartidosEstadisticaFragment_to_cargaPartidoActivity)
                }
            }
        }

        return root
    }
}