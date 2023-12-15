package com.franciscolinares.ubb.equipo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
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
import com.franciscolinares.ubb.databinding.FragmentConsultarEquipoBinding
import com.franciscolinares.ubb.equipo.ListViewEquipo.AdaptadorEquipo
import com.franciscolinares.ubb.equipo.ListViewEquipo.AdaptadorJugadorPlantilla
import com.franciscolinares.ubb.equipo.ListViewEquipo.Equipo
import com.franciscolinares.ubb.equipo.ListViewEquipo.JugadorPlantilla
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ConsultarEquipoFragment : Fragment() {

    private var _binding: FragmentConsultarEquipoBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private var listaEquipos = mutableListOf<Equipo>()
    private val listaPlantilla = mutableListOf<JugadorPlantilla>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("MissingInflatedId", "CutPasteId", "CommitPrefEdits")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConsultarEquipoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        llenarListView()

        binding.txtCoENombre.addTextChangedListener(object : TextWatcher {
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

        binding.ListViewEquipos.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->
                val builder = AlertDialog.Builder(binding.root.context)
                val view = layoutInflater.inflate(R.layout.plantilla_dialog, null)
                builder.setView(view)
                val dialog = builder.create()

                //Guardamos el id del equipo para poder hacer las gestiones
                val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
                val editor = prefs.edit()
                editor.putString("equipo_id", listaEquipos[i].id)
                editor.apply()

                view.findViewById<TextView>(R.id.txtNombreEquipo).text =
                    listaEquipos[i].nombreEquipo

                llenarListViewPlantilla(view, listaEquipos[i].id, listaEquipos[i].plantilla)

                //Cuando pulsamos el boton lo enviamos al Fragmento para poder ver los jugadores disponibles
                view.findViewById<Button>(R.id.btnAñadirJugador).setOnClickListener {
                    dialog.hide()
                    Navigation.findNavController(binding.root)
                        .navigate(R.id.action_consultarEquipoFragment_to_agregarJugadorFragment)
                }

                view.findViewById<Button>(R.id.btnGuardarPlantilla).setOnClickListener {
                    val jugadoresCount =
                        view.findViewById<ListView>(R.id.ListViewPlantilla).adapter.count
                    val allItems = arrayListOf<JugadorPlantilla>()

                    //recuperamos todos los items del ListView de plantilla para poder verificar los dorsales
                    for (i in 0 until jugadoresCount) {
                        val item =
                            view.findViewById<ListView>(R.id.ListViewPlantilla).adapter.getItem(i)
                        allItems.add(item as JugadorPlantilla)
                    }

                    //Verificamos que no se repita ningun numero ni haya un jugador sin dorsal
                    if (verificarDorsales(allItems)) {
                        val jugadores = hashMapOf<String, String>()
                        for (j in allItems) {
                            jugadores[j.id_jugador] = j.dorsal
                        }
                        db.collection("Equipos").document(listaEquipos[i].id)
                            .update(
                                hashMapOf(
                                    "Jugadores" to jugadores
                                ) as HashMap<String?, Any>
                            ).addOnSuccessListener {
                                Toast.makeText(
                                    binding.root.context,
                                    "Guardada la plantilla con exito",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        dialog.hide()
                        llenarListView()
                    } else {
                        // Manejar errores aquí
                        Toast.makeText(
                            binding.root.context,
                            "Hay un dorsal repetido o sin asignar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                dialog.show()
            }

        //Se si deja pulsado el equipo, saldra un dialog para borrar el equipo en cuestion
        binding.ListViewEquipos.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { arg0, arg1, pos, id ->

                val builder = AlertDialog.Builder(binding.root.context)
                val view = layoutInflater.inflate(R.layout.borrardialog, null)
                builder.setView(view)
                view.findViewById<TextView>(R.id.txtIdBorrar).text = listaEquipos[pos].nombreEquipo
                val dialog = builder.create()
                dialog.show()

                view.findViewById<Button>(R.id.btnSi).setOnClickListener {
                    db.collection("Equipos")
                        .document(listaEquipos[pos].id).delete()
                        .addOnSuccessListener {
                            Toast.makeText(
                                binding.root.context,
                                "Borrado con exito",
                                Toast.LENGTH_SHORT
                            ).show()
                        }.addOnFailureListener { exception ->
                            Toast.makeText(
                                binding.root.context,
                                "Error al borrar",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    db.collection("Jugadores")
                        .whereEqualTo("Equipo", listaEquipos[pos].id)
                        .get()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val jugadores = task.result
                                if (jugadores != null && !jugadores.isEmpty) {
                                    for (jugador in jugadores) {
                                        db.collection("Jugadores").document(jugador.id).update(
                                            hashMapOf(
                                                "Equipo" to ""
                                            ) as HashMap<String?, Any>
                                        ).addOnSuccessListener {

                                        }
                                    }
                                }
                            } else {
                                // Manejar errores aquí
                                Toast.makeText(
                                    binding.root.context,
                                    "Error al borra los jugadores del equipo",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    llenarListView()
                    dialog.hide()
                }

                view.findViewById<Button>(R.id.btnNo).setOnClickListener {
                    dialog.hide()
                }

                true

            }

        return root
    }

    //Función para comprobar que no se repita un equipo en la lista
    fun comprobarListaSinRepetidos(lista: MutableList<Equipo>): List<Equipo> {
        return lista.distinctBy { it.id }
    }

    //Función para verificar los dorsales
    fun verificarDorsales(jugadoresP: ArrayList<JugadorPlantilla>): Boolean {
        // Verificar que no haya dorsal repetido o vacío
        val dorsalNoRepetidosNoVacios = jugadoresP.map { it.dorsal }
            .let { campos ->
                campos.distinct().size == campos.size && campos.none { it.isEmpty() }
            }

        return dorsalNoRepetidosNoVacios
    }

    //Función que llena el ListView de equipos
    fun llenarListView() {
        listaEquipos.clear()
        db.collection("Equipos").get().addOnSuccessListener {
            for (equipos in it) {
                val equipo = Equipo(
                    equipos.id,
                    equipos.get("Nombre").toString(),
                    equipos.get("Categoria").toString(),
                    equipos.get("Sexo").toString(),
                    equipos.get("Localidad").toString(),
                    equipos.get("UrlFoto").toString(),
                    equipos.get("Jugadores") as HashMap<String, String>
                )
                listaEquipos.add(equipo)
            }

            //Función para comprobar que no se repita un equipo en la lista
            listaEquipos = comprobarListaSinRepetidos(listaEquipos) as MutableList<Equipo>

            val adapter = AdaptadorEquipo(binding.root.context, listaEquipos)

            binding.ListViewEquipos.adapter = adapter
        }
    }

    //Función que llena el ListView de equipos buscados por nombre
    fun llenarListViewNombre(nombre: String) {
        listaEquipos.clear()
        if (nombre.isNotEmpty()) {
            db.collection("Equipos")
                .whereGreaterThanOrEqualTo("Nombre", nombre)
                .whereLessThan("Nombre", nombre + "\uf8ff")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val equipos = task.result
                        if (equipos != null && !equipos.isEmpty) {
                            // Procesa los equipos que cumplen con la condición

                            for (equipo in equipos) {
                                val equipo = Equipo(
                                    equipo.id,
                                    equipo.get("Nombre").toString(),
                                    equipo.get("Categoria").toString(),
                                    equipo.get("Sexo").toString(),
                                    equipo.get("Localidad").toString(),
                                    equipo.get("UrlFoto").toString(),
                                    equipo.get("Jugadores") as HashMap<String, String>
                                )
                                listaEquipos.add(equipo)
                            }
                        }

                        //Función para comprobar que no se repita un equipo en la lista
                        listaEquipos = comprobarListaSinRepetidos(listaEquipos) as MutableList<Equipo>

                        val adapter = AdaptadorEquipo(binding.root.context, listaEquipos)

                        binding.ListViewEquipos.adapter = adapter
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

    //Función que llena el ListView de la plantilla del equipo pulsado
    fun llenarListViewPlantilla(
        viewDialog: View,
        equipo_id: String,
        plan: HashMap<String, String>
    ) {
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
                                    val j = JugadorPlantilla(
                                        jugador.id,
                                        jugador.get("Nombre").toString(),
                                        jugador.get("Apellido1").toString(),
                                        jugador.get("Apellido2").toString(),
                                        ju.value
                                    )
                                    listaPlantilla.add(j)
                                    continue
                                }
                            }
                        }
                        val adapter =
                            AdaptadorJugadorPlantilla(viewDialog.context, listaPlantilla)

                        viewDialog.findViewById<ListView>(R.id.ListViewPlantilla).adapter =
                            adapter

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}