package com.franciscolinares.ubb.partido

import android.opengl.Visibility
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentConsultarEquipoBinding
import com.franciscolinares.ubb.databinding.FragmentCrearPartidoBinding
import com.franciscolinares.ubb.equipo.ListViewEquipo.AdaptadorEquipo
import com.franciscolinares.ubb.equipo.ListViewEquipo.Equipo
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CrearPartidoFragment : Fragment() {

    private var _binding: FragmentCrearPartidoBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private var listaEquipos = mutableListOf<Equipo>()
    private var listaEquiposSeleccionados = mutableListOf<Equipo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearPartidoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        llenarListView()
        binding.btnCEnfrenta.visibility = View.GONE

        binding.txtConEquipo.addTextChangedListener(object : TextWatcher {
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

        binding.ListViewEquipos.setOnItemClickListener { adapterView, view, i, l ->
            if (listaEquiposSeleccionados.count() < 2) {
                listaEquiposSeleccionados.add(listaEquipos[i])
                comprobarEquipos()
            }

            val adapterSeleccionados =
                AdaptadorEquipo(binding.root.context, listaEquiposSeleccionados)

            binding.ListViewEquiposSeleccionado.adapter = adapterSeleccionados
            llenarListView()
            binding.txtConEquipo.setText("")
        }

        binding.ListViewEquiposSeleccionado.setOnItemClickListener { adapterView, view, i, l ->
            listaEquiposSeleccionados.removeAt(i)
            comprobarEquipos()
            val adapterSeleccionados =
                AdaptadorEquipo(binding.root.context, listaEquiposSeleccionados)

            binding.ListViewEquiposSeleccionado.adapter = adapterSeleccionados
            llenarListView()
            binding.txtConEquipo.setText("")
        }

        binding.btnCEnfrenta.setOnClickListener {
            val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
            val editor = prefs.edit()
            editor.putString("EquipoLocal", listaEquiposSeleccionados[0].id)
            editor.putString("EquipoVisitante", listaEquiposSeleccionados[1].id)
            editor.apply()

            Navigation.findNavController(binding.root)
                .navigate(R.id.action_crearPartidoFragment_to_fecharPartidoFragment)
        }

        return root
    }

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
            listaEquipos = sinEquipoSeleccionado() as MutableList<Equipo>

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
                        listaEquipos =
                            comprobarListaSinRepetidos(listaEquipos) as MutableList<Equipo>
                        listaEquipos = sinEquipoSeleccionado() as MutableList<Equipo>

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

    fun sinEquipoSeleccionado(): List<Equipo> {
        return listaEquipos.filterNot { elemento -> listaEquiposSeleccionados.contains(elemento) }
    }

    fun comprobarListaSinRepetidos(lista: MutableList<Equipo>): List<Equipo> {
        return lista.distinctBy { it.id }
    }

    fun comprobarEquipos() {
        if (listaEquiposSeleccionados.count() < 2)
            binding.btnCEnfrenta.visibility = View.GONE
        else
            binding.btnCEnfrenta.visibility = View.VISIBLE
    }
}