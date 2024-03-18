package com.franciscolinares.ubb.partido

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.navigation.Navigation
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentCargarPlantillasBinding
import com.franciscolinares.ubb.databinding.FragmentPlantillaLocalBinding
import com.franciscolinares.ubb.equipo.ListViewEquipo.JugadorPlantilla
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.util.UUID

class PlantillaLocalFragment : Fragment() {

    private var _binding: FragmentPlantillaLocalBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlantillaLocalBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val equipoLocal = prefs.getString("EquipoLocal", "")
        val idpartido = prefs.getString("idPartido", "")

        db.collection("Equipos").document(equipoLocal.toString()).get()
            .addOnSuccessListener {
                if (it.get("UrlFoto") != "") {
                    Picasso.get()
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(binding.imageEquipoLocal)
                }
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }


        val contenedor: LinearLayout = binding.contenedor
        val listCheckBox: ArrayList<CheckBox> = java.util.ArrayList<CheckBox>()
        var index = 0
        val listaDorsalesI = arrayListOf<Int>()
        val listaIdJugadores = arrayListOf<String>()

        db.collection("Equipos").document(equipoLocal.toString()).get()
            .addOnSuccessListener { documentSnapshot ->
                val listJugadores = documentSnapshot.get("Jugadores") as HashMap<String, String>
                for (jugador in listJugadores) {
                    db.collection("Jugadores").document(jugador.key).get()
                        .addOnSuccessListener {
                            val checkbox: CheckBox = CheckBox(binding.root.context)
                            checkbox.text =
                                it.get("Apellido2").toString() + " " + it.get("Apellido1") +
                                        " " + it.get("Nombre") + " , " + jugador.value
                            checkbox.id = index
                            index++
                            contenedor.addView(checkbox)
                            listCheckBox.add(checkbox)
                            listaIdJugadores.add(it.id)
                        }
                }
            }

        binding.btnInscribirJugadorLocal.setOnClickListener {
            val builder = AlertDialog.Builder(binding.root.context)
            val view = layoutInflater.inflate(R.layout.inscribir_jugador_partido, null)
            builder.setView(view)
            val dialog = builder.create()

            view.findViewById<Button>(R.id.btnInscribirJP).setOnClickListener {
                val nombreJ = view.findViewById<EditText>(R.id.txtIJPNombre).text.toString()
                val apellido1J = view.findViewById<EditText>(R.id.txtIJPApellido1).text.toString()
                val apellido2J = view.findViewById<EditText>(R.id.txtIJPApellido2).text.toString()
                val dorsalJ = view.findViewById<EditText>(R.id.txtIJPDorsal).text.toString()

                if (dorsalJ != "") {

                    db.collection("Equipos").document(equipoLocal.toString()).get()
                        .addOnSuccessListener {
                            val listaDorsalesR = arrayListOf<Int>()
                            for (jugador in it.get("Jugadores") as HashMap<String, String>) {
                                listaDorsalesR.add(jugador.value.toInt())
                            }
                            listaDorsalesI.add(dorsalJ.toInt())
                            listaDorsalesR += listaDorsalesI
                            if (!tieneNumerosRepetidos(listaDorsalesR)) {
                                val checkbox: CheckBox = CheckBox(binding.root.context)
                                checkbox.text = "$apellido1J $apellido2J $nombreJ , $dorsalJ"
                                checkbox.id = index
                                listaIdJugadores.add(UUID.randomUUID().toString())
                                index++
                                contenedor.addView(checkbox)
                                listCheckBox.add(checkbox)
                                dialog.hide()
                            } else {
                                // Manejar errores aquí
                                Toast.makeText(
                                    binding.root.context,
                                    "Hay un dorsal repetido",
                                    Toast.LENGTH_SHORT
                                ).show()
                                listaDorsalesI.remove(dorsalJ.toInt())
                            }
                        }
                } else {
                    // Manejar errores aquí
                    Toast.makeText(
                        binding.root.context,
                        "No hay un dorsal asignado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            dialog.show()
        }

        binding.btnGuardarPlantillaL.setOnClickListener {
            var sw = 0
            val listJugadores = ArrayList<String?>()
            var cont = 0

            for (checkbox in listCheckBox)
                if (checkbox.isChecked) {
                    cont++
                }

            if (cont > 12) {
                Toast.makeText(
                    binding.root.context,
                    "No puedes inscribir mas de 12 jugadores en acta",
                    Toast.LENGTH_SHORT
                ).show()

            } else if (cont < 5) {
                Toast.makeText(
                    binding.root.context,
                    "Tienes que inscribir al menos 5 jugadores en acta",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                for (i in 0..listCheckBox.count() - 1) {
                    val checkbox: CheckBox = listCheckBox[i]
                    if (checkbox.isChecked) {

                        val jugadorCB = checkbox.text.split(" , ")
                        val id = listaIdJugadores[i]
                        listJugadores.add(id)

                        val jugador = hashMapOf(
                            "dorsal" to jugadorCB[1],
                            "nombre" to jugadorCB[0],
                            "minutos" to 0,
                            "equipo" to "Local",
                            "asi" to 0,
                            "falR" to 0,
                            "falC" to 0,
                            "per" to 0,
                            "puntos" to 0,
                            "rebD" to 0,
                            "rebO" to 0,
                            "recu" to 0,
                            "taCom" to 0,
                            "taRec" to 0,
                            "tc2pA" to 0,
                            "tc2pF" to 0,
                            "tc3pA" to 0,
                            "tc3pF" to 0,
                            "tlA" to 0,
                            "tlF" to 0,
                            "val" to 0
                        ) as Map<String, Any>

                        db.collection("Estadisticas").document(idpartido.toString()).get()
                            .addOnSuccessListener {

                                if (sw == 1) {
                                    db.collection("Estadisticas").document(idpartido.toString())
                                        .update(
                                            hashMapOf(
                                                id to jugador,
                                            ) as Map<String, Any>
                                        )
                                } else {
                                    val listRegistro = ArrayList<Map<String?, Any?>>()
                                    db.collection("Estadisticas").document(idpartido.toString())
                                        .set(
                                            hashMapOf(
                                                id to jugador,
                                                "ListadoJugadores" to listJugadores
                                            ) as Map<String, Any>
                                        )
                                    db.collection("MinutoaMinuto")
                                        .document(idpartido.toString())
                                        .set(
                                            hashMapOf(
                                                "registro" to listRegistro,
                                            ) as Map<String, Any>
                                        )
                                    sw = 1
                                }
                            }


                    }
                }

                db.collection("Estadisticas").document(idpartido.toString())
                    .update(
                        hashMapOf(
                            "ListadoJugadores" to listJugadores,
                        ) as Map<String, Any>
                    )

                Toast.makeText(
                    binding.root.context,
                    "Cargado el equipo local con exito",
                    Toast.LENGTH_SHORT
                ).show()
                Navigation.findNavController(binding.root)
                    .navigate(R.id.action_plantillaLocalFragment_to_plantillaVisitanteFragment)
            }
        }

        return root
    }

    fun tieneNumerosRepetidos(array: ArrayList<Int>): Boolean {
        val set = HashSet<Int>()
        for (num in array) {
            if (!set.add(num)) {  // hay un número repetido
                return true
            }
        }
        return false // No se encontraron números repetidos
    }


}