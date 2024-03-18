package com.franciscolinares.ubb.partido

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentCargarPlantillasBinding
import com.franciscolinares.ubb.databinding.FragmentGestionarPartidosBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CargarPlantillasFragment : Fragment() {

    private var _binding: FragmentCargarPlantillasBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCargarPlantillasBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idpartido = prefs.getString("idPartido", "")

        db.collection("Partidos").document(idpartido.toString()).get()
            .addOnSuccessListener {
                val editor = prefs.edit()
                editor.putString("EquipoLocal", it.getString("EquipoLocal"))
                editor.putString("EquipoVisitante", it.getString("EquipoVisitante"))
                editor.apply()
            }

        binding.btnEquipo.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_cargarPlantillasFragment_to_plantillaLocalFragment)
        }

        binding.btnIniciarPartido.setOnClickListener {

            db.collection("Estadisticas").document(idpartido.toString()).get()
                .addOnSuccessListener {
                    val editor = prefs.edit()
                    editor.putString("idPartido", idpartido.toString())
                    editor.apply()

                    if (it.exists())
                        Navigation.findNavController(binding.root)
                            .navigate(R.id.action_cargarPlantillasFragment_to_partidoFragment)
                    else
                        Toast.makeText(
                            binding.root.context,
                            "Carga las plantillas para poder seguir",
                            Toast.LENGTH_SHORT
                        ).show()
                }

        }

        return root
    }
}