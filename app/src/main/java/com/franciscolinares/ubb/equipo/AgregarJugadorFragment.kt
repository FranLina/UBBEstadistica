package com.franciscolinares.ubb.equipo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentAgregarJugadorBinding
import com.franciscolinares.ubb.databinding.FragmentConsultarEquipoBinding
import com.franciscolinares.ubb.jugador.ListViewJugador.AdaptadorJugador
import com.franciscolinares.ubb.jugador.ListViewJugador.Jugador
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AgregarJugadorFragment : Fragment() {

    private var _binding: FragmentAgregarJugadorBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private val listaJugadores = mutableListOf<Jugador>()
    private var equipo_id: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgregarJugadorBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        equipo_id = prefs.getString("equipo_id", "").toString()

        llenarListView()

        binding.ListViewAgregarJugador.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, pos, l ->
                val builder = AlertDialog.Builder(binding.root.context)
                val view = layoutInflater.inflate(R.layout.borrardialog, null)
                builder.setView(view)
                view.findViewById<TextView>(R.id.txtPregunta).text =
                    "¿Estas seguro que quieres añadir el jugador?"
                view.findViewById<TextView>(R.id.txtIdBorrar).text =
                    listaJugadores[pos].nombre + " " + listaJugadores[pos].apellido1 + " " + listaJugadores[pos].apellido2
                val dialog = builder.create()
                dialog.show()

                view.findViewById<Button>(R.id.btnSi).setOnClickListener {
                    agregarJugadorAEquipo(listaJugadores[pos].id_jugador)
                    dialog.hide()
                }

                view.findViewById<Button>(R.id.btnNo).setOnClickListener {
                    dialog.hide()
                }
            }

        return root
    }

    fun agregarJugadorAEquipo(jugador_id: String) {
        var jugadores = hashMapOf<String, String>()
        db.collection("Jugadores").document(jugador_id)
            .update(
                hashMapOf(
                    "Equipo" to equipo_id
                ) as HashMap<String?, Any>
            )
        db.collection("Equipos").document(equipo_id)
            .get().addOnSuccessListener {
                jugadores = it.get("Jugadores") as HashMap<String, String>;
                jugadores[jugador_id] = ""

                db.collection("Equipos").document(equipo_id).update(
                    hashMapOf(
                        "Jugadores" to jugadores
                    ) as HashMap<String?, Any>
                ).addOnSuccessListener {
                    Toast.makeText(
                        binding.root.context,
                        "Jugador agregado con exito",
                        Toast.LENGTH_SHORT
                    ).show()
                    llenarListView()
                }
            }
    }

    fun llenarListView() {
        listaJugadores.clear()
        db.collection("Jugadores")
            .whereEqualTo("Equipo", "")
            .get()
            .addOnSuccessListener {
                for (jugadores in it) {
                    val jugador = Jugador(
                        jugadores.id,
                        jugadores.get("Nombre").toString(),
                        jugadores.get("Apellido1").toString(),
                        jugadores.get("Apellido2").toString(),
                        jugadores.get("Categoria").toString(),
                        jugadores.get("Sexo").toString(),
                        jugadores.get("Equipo").toString(),
                        jugadores.get("UrlFoto").toString()
                    )
                    listaJugadores.add(jugador)
                }

                val adapter = AdaptadorJugador(binding.root.context, listaJugadores)

                binding.ListViewAgregarJugador.adapter = adapter
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}