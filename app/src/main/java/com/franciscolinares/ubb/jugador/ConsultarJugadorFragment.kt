package com.franciscolinares.ubb.jugador

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentConsultarJugadorBinding
import com.franciscolinares.ubb.databinding.FragmentCrearJugadorBinding
import com.franciscolinares.ubb.jugador.ListViewJugador.AdaptadorJugador
import com.franciscolinares.ubb.jugador.ListViewJugador.Jugador
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ConsultarJugadorFragment : Fragment() {

    private var _binding: FragmentConsultarJugadorBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private val listaJugadores = mutableListOf<Jugador>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConsultarJugadorBinding.inflate(inflater, container, false)
        val root: View = binding.root

        llenarListView()

        binding.txtCoJNombre.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                // Este método se llama para notificar que algo está a punto de cambiar en el texto.
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                // Este método se llama para notificar que el texto ha cambiado.
                // Puedes ejecutar tu función aquí.
                val nuevoTexto = charSequence.toString()
                //llenarListViewNombre(nuevoTexto)
            }

            override fun afterTextChanged(editable: Editable) {
                // Este método se llama para notificar que el texto ha cambiado después de que se haya aplicado el cambio.
                llenarListViewNombre(editable.toString())
            }
        })

        binding.ListViewJugador.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { arg0, arg1, pos, id ->

                val builder = AlertDialog.Builder(binding.root.context)
                val view = layoutInflater.inflate(R.layout.borrardialog, null)
                builder.setView(view)
                view.findViewById<TextView>(R.id.txtIdBorrar).text =
                    listaJugadores[pos].nombre + " " + listaJugadores[pos].apellido1 + " " + listaJugadores[pos].apellido2
                val dialog = builder.create()
                dialog.show()

                view.findViewById<Button>(R.id.btnSi).setOnClickListener {
                    db.collection("Jugadores")
                        .document(listaJugadores[pos].id_jugador).delete()
                        .addOnSuccessListener {
                            Toast.makeText(
                                binding.root.context,
                                "Borrado con exito",
                                Toast.LENGTH_SHORT
                            ).show()
                        }.addOnFailureListener { exception ->
                            Log.w(
                                ContentValues.TAG,
                                "Error deletting documents.",
                                exception
                            )
                        }
                    db.collection("Equipos").document(listaJugadores[pos].equipo).get()
                        .addOnSuccessListener {
                            val listJugador = it.get("Jugadores") as HashMap<String, String>;
                            listJugador.remove(listaJugadores[pos].id_jugador)
                            db.collection("Equipos").document(listaJugadores[pos].equipo).update(
                                hashMapOf(
                                    "Jugadores" to listJugador
                                ) as HashMap<String?, Any>
                            ).addOnSuccessListener {
                                llenarListView()
                                dialog.hide()
                            }
                        }
                }

                view.findViewById<Button>(R.id.btnNo).setOnClickListener {
                    dialog.hide()
                }

                true
            }


        return root
    }

    fun llenarListView() {
        listaJugadores.clear()
        db.collection("Jugadores").get().addOnSuccessListener {
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

            binding.ListViewJugador.adapter = adapter
        }
    }

    fun llenarListViewNombre(nombre: String) {
        if (nombre.isNotEmpty()) {
            listaJugadores.clear()
            db.collection("Jugadores")
                .whereGreaterThanOrEqualTo(
                    "Nombre",
                    nombre.substring(0, 1).toUpperCase() + nombre.substring(1)
                )
                .whereLessThan(
                    "Nombre",
                    nombre.substring(0, 1).toUpperCase() + nombre.substring(1) + "\uf8ff"
                )
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val jugadores = task.result
                        if (jugadores != null && !jugadores.isEmpty) {
                            // Procesa los jugadores que cumplen con la condición

                            for (jugador in jugadores) {
                                val jugador = Jugador(
                                    jugador.id,
                                    jugador.get("Nombre").toString(),
                                    jugador.get("Apellido1").toString(),
                                    jugador.get("Apellido2").toString(),
                                    jugador.get("Categoria").toString(),
                                    jugador.get("Sexo").toString(),
                                    jugador.get("Equipo").toString(),
                                    jugador.get("UrlFoto").toString()
                                )
                                listaJugadores.add(jugador)
                            }
                        }
                        val adapter = AdaptadorJugador(binding.root.context, listaJugadores)

                        binding.ListViewJugador.adapter = adapter
                    } else {
                        // Manejar errores aquí
                        Toast.makeText(
                            binding.root.context,
                            "Error al realizar la consulta",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            llenarListView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}