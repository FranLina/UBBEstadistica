package com.franciscolinares.ubb.partido

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentCrearPartidoBinding
import com.franciscolinares.ubb.databinding.FragmentGestionarPartidosBinding
import com.franciscolinares.ubb.partido.ListViewPartido.AdaptadorPartido
import com.franciscolinares.ubb.partido.ListViewPartido.Partido
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class GestionarPartidosFragment : Fragment() {

    private var _binding: FragmentGestionarPartidosBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGestionarPartidosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        db.collection("Partidos").orderBy("Fecha").get().addOnSuccessListener {
            val listaPartidos = mutableListOf<Partido>()
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
                listaPartidos.add(p)
            }

            val adapter = AdaptadorPartido(binding.root.context, listaPartidos)

            binding.ListViewPartidos.adapter = adapter

            binding.ListViewPartidos.setOnItemClickListener { adapterView, view, i, l ->

                val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
                val editor = prefs.edit()
                editor.putString("idPartido", listaPartidos[i].id)
                editor.apply()
                Navigation.findNavController(binding.root)
                    .navigate(R.id.action_gestionarPartidosFragment_to_cargarPlantillasFragment)

            }
        }



        return root
    }
}