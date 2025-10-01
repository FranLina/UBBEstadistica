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
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentGestionarPartidosBinding
import com.franciscolinares.ubb.partido.ListViewPartido.AdaptadorPartido
import com.franciscolinares.ubb.partido.ListViewPartido.Partido
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.navigation.findNavController

class GestionarPartidosFragment : Fragment() {

    private var _binding: FragmentGestionarPartidosBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private var listaPartidos = mutableListOf<Partido>()
    private var listaOrdenada = mutableListOf<Partido>()

    private lateinit var myAdapter: AdaptadorPartido
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGestionarPartidosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        listView = binding.ListViewPartidos

        llenarListView()

        listView.setOnItemClickListener { adapterView, view, i, l ->

            val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
            val editor = prefs.edit()
            editor.putString("idPartido", listaOrdenada[i].id)
            editor.apply()
            binding.root.findNavController()
                .navigate(R.id.action_gestionarPartidosFragment_to_cargarPlantillasFragment)

        }

        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { arg0, arg1, pos, id ->

            val builder = AlertDialog.Builder(binding.root.context)
            val view = layoutInflater.inflate(R.layout.borrardialog, null)
            builder.setView(view)
            view.findViewById<TextView>(R.id.txtIdBorrar).text = listaOrdenada[pos].local + " vs " + listaOrdenada[pos].visitante
            val dialog = builder.create()
            dialog.show()

            view.findViewById<Button>(R.id.btnSi).setOnClickListener {
                if (listaOrdenada[pos].id != "") {
                    db.collection("Partidos")
                        .document(listaOrdenada[pos].id).delete()
                        .addOnSuccessListener {
                            Toast.makeText(binding.root.context, "Borrado con exito", Toast.LENGTH_SHORT).show()
                            db.collection("Estadisticas").document(listaOrdenada[pos].id).delete()
                            db.collection("MinutoaMinuto").document(listaOrdenada[pos].id).delete()
                            llenarListView()
                            dialog.hide()
                        }.addOnFailureListener { exception ->
                            Log.w(
                                ContentValues.TAG,
                                "Error deletting documents.",
                                exception
                            )
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

    private fun llenarListView() {
        listaPartidos.clear()
        db.collection("Partidos").orderBy("Fecha").get().addOnSuccessListener {
            for (partido in it) {
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
                listaPartidos.add(p)
            }

            listaOrdenada = listaPartidos.sortedBy { it.fechaDate }.toMutableList()

            myAdapter = AdaptadorPartido(binding.root.context, listaOrdenada)

            listView.adapter = myAdapter
        }
    }
}