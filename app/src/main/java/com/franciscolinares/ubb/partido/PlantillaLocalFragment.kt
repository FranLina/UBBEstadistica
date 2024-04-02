package com.franciscolinares.ubb.partido

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.navigation.Navigation
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentPlantillaLocalBinding
import com.franciscolinares.ubb.partido.ListViewPartido.AdaptadorJugadorConvocado
import com.franciscolinares.ubb.partido.ListViewPartido.JugadorConvocado
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.util.Locale
import java.util.UUID

class PlantillaLocalFragment : Fragment() {

    private var _binding: FragmentPlantillaLocalBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private lateinit var myAdapter: AdaptadorJugadorConvocado
    private lateinit var listView: ListView
    private val listaPlantilla = mutableListOf<JugadorConvocado>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlantillaLocalBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val equipoLocal = prefs.getString("EquipoLocal", "")
        val idPartido = prefs.getString("idPartido", "").toString()

        listView = binding.LVPlantillaLocal

        db.collection("Equipos").document(equipoLocal.toString()).get().addOnSuccessListener {
            binding.txtPlantillaEquipoL.text = it.get("Nombre").toString().toUpperCase(Locale.ROOT)

            if (it.get("UrlFoto") != "") {
                Picasso.get()
                    .load(it.get("UrlFoto").toString())
                    .placeholder(R.drawable.escudo_equipo)
                    .error(R.drawable.escudo_equipo)
                    .into(binding.imageEquipoLocal)
            }

            recuperaJugadores(root, equipoLocal.toString(), it.get("Jugadores") as HashMap<String, String>)
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

                if (nombreJ != "") {
                    val j = JugadorConvocado(
                        UUID.randomUUID().toString(),
                        "",
                        nombreJ,
                        apellido1J,
                        apellido2J,
                        "",
                        "",
                        false
                    )
                    listaPlantilla.add(j)
                    myAdapter.updateData(listaPlantilla)
                    dialog.hide()
                } else {
                    // Manejar errores aquí
                    Toast.makeText(
                        binding.root.context,
                        "No hay un nombre asignado al Jugador",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            dialog.show()
        }

        binding.btnGuardarPlantillaL.setOnClickListener {

            var sw = 0
            var cont = 0
            val listJugadores = ArrayList<String?>()
            val jugadoresCount = listView.adapter.count
            val allItems = arrayListOf<JugadorConvocado>()

            //recuperamos todos los items del ListView de plantilla para poder verificar los dorsales
            for (i in 0 until jugadoresCount) {
                val item = listView.adapter.getItem(i)
                allItems.add(item as JugadorConvocado)
            }

            //Verificamos que no se repita ningun numero ni haya un jugador sin dorsal
            if (verificarDorsales(allItems)) {

                val jugadores = hashMapOf<String, String>()
                for (j in allItems) {
                    if (j.convocado)
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
                    for (j in allItems) {
                        if (j.convocado) {
                            val jugador = hashMapOf(
                                "dorsal" to j.dorsal,
                                "nombre" to j.apellido1 + " " + j.apellido2 + ", " + j.nombre,
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
                            listJugadores.add(j.id_jugador)

                            db.collection("Estadisticas").document(idPartido).get()
                                .addOnSuccessListener {
                                    if (sw == 1) {
                                        db.collection("Estadisticas").document(idPartido)
                                            .update(
                                                hashMapOf(
                                                    j.id_jugador to jugador,
                                                ) as Map<String, Any>
                                            )
                                    } else {
                                        db.collection("Estadisticas").document(idPartido)
                                            .set(
                                                hashMapOf(
                                                    j.id_jugador to jugador,
                                                    "ListadoJugadores" to listJugadores
                                                ) as Map<String, Any>
                                            )
                                        val listRegistro = ArrayList<Map<String?, Any?>>()
                                        db.collection("MinutoaMinuto")
                                            .document(idPartido)
                                            .set(
                                                hashMapOf(
                                                    "registro" to listRegistro,
                                                ) as Map<String, Any>
                                            )
                                        sw = 1
                                    }
                                }

                            db.collection("Estadisticas").document(idPartido)
                                .update(
                                    hashMapOf(
                                        "ListadoJugadores" to listJugadores,
                                    ) as Map<String, Any>
                                )
                        }
                    }
                    db.collection("Partidos").document(idPartido)
                        .update(
                            hashMapOf(
                                "PlantillaL" to true,
                            ) as Map<String, Any>
                        )

                    Toast.makeText(binding.root.context, "Cargado el equipo local con exito", Toast.LENGTH_SHORT).show()
                    Navigation.findNavController(binding.root).navigate(R.id.action_plantillaLocalFragment_to_cargarPlantillasFragment)
                }
            } else {
                // Manejar errores aquí
                Toast.makeText(
                    binding.root.context,
                    "Hay un dorsal repetido o sin asignar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return root
    }

    private fun recuperaJugadores(viewDialog: View, equipo_id: String, plan: HashMap<String, String>) {
        listaPlantilla.clear()
        db.collection("Jugadores")
            .whereEqualTo("Equipo", equipo_id)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val jugadores = task.result
                    if (jugadores != null && !jugadores.isEmpty) {
                        for (jugador in jugadores) {
                            for (ju in plan) {
                                if (jugador.id == ju.key) {
                                    val j = JugadorConvocado(
                                        jugador.id,
                                        jugador.get("UrlFoto").toString(),
                                        jugador.get("Nombre").toString(),
                                        jugador.get("Apellido1").toString(),
                                        jugador.get("Apellido2").toString(),
                                        ju.value,
                                        jugador.get("Equipo").toString(),
                                        false
                                    )
                                    listaPlantilla.add(j)
                                    continue
                                }
                            }
                        }
                        myAdapter = AdaptadorJugadorConvocado(viewDialog.context, listaPlantilla)
                        listView.adapter = myAdapter

                    }
                } else {
                    // Manejar errores aquí
                    Toast.makeText(
                        binding.root.context,
                        "Error al recuperar los jugadores del equipo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    fun verificarDorsales(jugadoresP: ArrayList<JugadorConvocado>): Boolean {
        // Verificar que no haya dorsal repetido o vacío
        val dorsalNoRepetidosNoVacios = jugadoresP.map { it.dorsal }
            .let { campos ->
                campos.distinct().size == campos.size && campos.none { it.isEmpty() }
            }

        return dorsalNoRepetidosNoVacios
    }
}