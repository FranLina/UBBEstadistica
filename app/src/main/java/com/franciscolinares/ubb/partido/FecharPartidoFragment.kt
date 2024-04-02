package com.franciscolinares.ubb.partido

import android.content.ContentValues
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentFecharPartidoBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso


class FecharPartidoFragment : Fragment() {

    private var _binding: FragmentFecharPartidoBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFecharPartidoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val equipoLocal = prefs.getString("EquipoLocal", "")
        val equipoVisitante = prefs.getString("EquipoVisitante", "")

        db.collection("Equipos").document(equipoLocal.toString())
            .get()
            .addOnSuccessListener {
                binding.txtEENombreLocal.text = it.get("Nombre").toString()
                if (it.get("UrlFoto") != "") {
                    Picasso.get()
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.escudopredeterminado)
                        .error(R.drawable.escudopredeterminado)
                        .into(binding.imageEELocal)
                }
            }
        db.collection("Equipos").document(equipoVisitante.toString())
            .get()
            .addOnSuccessListener {
                binding.txtEENombreVisitante.text = it.get("Nombre").toString()
                if (it.get("UrlFoto") != "") {
                    Picasso.get()
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.escudopredeterminado)
                        .error(R.drawable.escudopredeterminado)
                        .into(binding.imageEEVisitante)
                }
            }


        binding.btnFPartido.setOnClickListener {
            db.collection("Partidos").add(
                hashMapOf(
                    "EquipoLocal" to equipoLocal,
                    "EquipoVisitante" to equipoVisitante,
                    "Estado" to "No Comenzado",
                    "Resultado" to "0 - 0",
                    "Polideportivo" to binding.txtEEPolideportivo.text.toString(),
                    "Fecha" to binding.txtEEFecha.text.toString(),
                    "Hora" to binding.txtEEHora.text.toString(),
                    "Cuarto" to "1",
                    "Tiempo" to "10:00",
                    "FaltaL" to 0,
                    "FaltaV" to 0,
                    "TiempoML" to 2,
                    "TiempoMV" to 2,
                    "PlantillaL" to false,
                    "PlantillaV" to false,
                    "QuintetoL" to arrayListOf<String>(),
                    "QuintetoV" to arrayListOf<String>()
                ) as Map<String, Any>
            ).addOnSuccessListener {
                Toast.makeText(
                    binding.root.context,
                    "Se ha fechado con exito",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        return root
    }
}