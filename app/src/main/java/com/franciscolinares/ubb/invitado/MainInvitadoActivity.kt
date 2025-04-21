package com.franciscolinares.ubb.invitado

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.ActivityMainInvitadoBinding
import com.franciscolinares.ubb.invitado.RecyclerViewInvitado.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainInvitadoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainInvitadoBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var equipoAdapter: EquipoAdapter
    private lateinit var jugadorAdapter: JugadorAdapter
    private lateinit var idsEquipos: MutableList<String>
    private lateinit var idsJugadores: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainInvitadoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        idsEquipos = prefs.getStringSet("ids_equipos", emptySet())?.toMutableList() ?: mutableListOf()
        idsJugadores = prefs.getStringSet("idsJugadores", emptySet())?.toMutableList() ?: mutableListOf()

        cargarEquipos()
        cargarJugadores()

        binding.txtAAdirEqu.setOnClickListener { mostrarDialogoAgregarEquipo(prefs) }
        binding.txtAAdirJug.setOnClickListener { mostrarDialogoAgregarJugador(prefs) }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun cargarEquipos() {
        lifecycleScope.launch {
            val equipos = mutableListOf<Equipo>()
            val snapshot = db.collection("Equipos").get().await()
            for (equi in snapshot) {
                if (idsEquipos.contains(equi.id)) {
                    equipos.add(
                        Equipo(
                            equi.id,
                            equi["Nombre"].toString(),
                            equi["Categoria"].toString(),
                            equi["Sexo"].toString(),
                            equi["UrlFoto"].toString()
                        )
                    )
                }
            }
            equipoAdapter = EquipoAdapter(equipos, { equipo ->
                PreferenceManager.getDefaultSharedPreferences(this@MainInvitadoActivity).edit {
                    putString("idEquipo", equipo.idEquipo)
                }
                startActivity(Intent(this@MainInvitadoActivity, MainEquipoInvitadoActivity::class.java))
            }, { equipo ->
                equipos.remove(equipo)
                idsEquipos.remove(equipo.idEquipo)
                PreferenceManager.getDefaultSharedPreferences(this@MainInvitadoActivity).edit {
                    putStringSet("ids_equipos", idsEquipos.toSet())
                }
                equipoAdapter.notifyDataSetChanged()
            })

            binding.rvEquipos.layoutManager = LinearLayoutManager(this@MainInvitadoActivity, LinearLayoutManager.HORIZONTAL, false)
            binding.rvEquipos.adapter = equipoAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun cargarJugadores() {
        lifecycleScope.launch {
            val jugadores = mutableListOf<Jugador>()
            val snapshot = db.collection("Jugadores").get().await()
            for (jug in snapshot) {
                if (idsJugadores.contains(jug.id)) {
                    jugadores.add(
                        Jugador(
                            jug.id,
                            "${jug["Apellido1"]} ${jug["Apellido2"]}, ${jug["Nombre"]}",
                            jug["Categoria"].toString(),
                            jug["Sexo"].toString(),
                            jug["UrlFoto"].toString()
                        )
                    )
                }
            }
            jugadorAdapter = JugadorAdapter(jugadores, { jugador ->
                PreferenceManager.getDefaultSharedPreferences(this@MainInvitadoActivity).edit {
                    putString("idJugador", jugador.idJugador)
                }
                startActivity(Intent(this@MainInvitadoActivity, JugadorInvitadoActivity::class.java))
            }, { jugador ->
                jugadores.remove(jugador)
                idsJugadores.remove(jugador.idJugador)
                PreferenceManager.getDefaultSharedPreferences(this@MainInvitadoActivity).edit {
                    putStringSet("idsJugadores", idsJugadores.toSet())
                }
                jugadorAdapter.notifyDataSetChanged()
            })

            binding.rvJugadores.layoutManager = LinearLayoutManager(this@MainInvitadoActivity, LinearLayoutManager.HORIZONTAL, false)
            binding.rvJugadores.adapter = jugadorAdapter
        }
    }

    private fun mostrarDialogoAgregarEquipo(prefs: android.content.SharedPreferences) {
        val view = layoutInflater.inflate(R.layout.equipo_buscar_invitado, null)
        val spinCategoria = view.findViewById<Spinner>(R.id.spinCategoria)
        val spinSexo = view.findViewById<Spinner>(R.id.spinSexo)
        val spinEquipo = view.findViewById<Spinner>(R.id.spinEquipo)
        val btnAgregar = view.findViewById<Button>(R.id.btnAgregarEInvitado)

        inicializarSpinners(spinCategoria, spinSexo)
        val dialog = AlertDialog.Builder(this).setView(view).create()

        val actualizarEquipos: () -> Unit = {
            lifecycleScope.launch {
                val equipos = obtenerEquiposFiltrados(spinCategoria.selectedItem.toString(), spinSexo.selectedItem.toString())
                spinEquipo.adapter = ArrayAdapter(this@MainInvitadoActivity, R.layout.spinner_item, equipos)
            }
        }

        spinCategoria.onItemSelectedListener = crearOnItemSelectedListener(actualizarEquipos)
        spinSexo.onItemSelectedListener = crearOnItemSelectedListener(actualizarEquipos)

        btnAgregar.setOnClickListener {
            val id = spinEquipo.selectedItem?.toString()
            if (!id.isNullOrEmpty() && !idsEquipos.contains(id)) {
                idsEquipos.add(id)
                prefs.edit { putStringSet("ids_equipos", idsEquipos.toSet()) }
                cargarEquipos()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Equipo ya agregado o no seleccionado", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun mostrarDialogoAgregarJugador(prefs: android.content.SharedPreferences) {
        val view = layoutInflater.inflate(R.layout.jugador_buscar_invitado, null)
        val spinCategoria = view.findViewById<Spinner>(R.id.spinCategoria)
        val spinSexo = view.findViewById<Spinner>(R.id.spinSexo)
        val spinEquipo = view.findViewById<Spinner>(R.id.spinEquipo)
        val spinJugador = view.findViewById<Spinner>(R.id.spinJugador)
        val btnAgregar = view.findViewById<Button>(R.id.btnAgregarJInvitado)

        inicializarSpinners(spinCategoria, spinSexo)
        val dialog = AlertDialog.Builder(this).setView(view).create()

        val actualizarEquipos: () -> Unit = {
            lifecycleScope.launch {
                val equipos = obtenerEquiposFiltrados(spinCategoria.selectedItem.toString(), spinSexo.selectedItem.toString())
                spinEquipo.adapter = ArrayAdapter(this@MainInvitadoActivity, R.layout.spinner_item, equipos)

                if (equipos.isNotEmpty()) {
                    spinEquipo.setSelection(0)
                    val jugadores = obtenerJugadoresDeEquipo(equipos[0])
                    val adapterJugadores = ArrayAdapter(this@MainInvitadoActivity, R.layout.spinner_item, jugadores.map { it.nombre })
                    adapterJugadores.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    spinJugador.adapter = adapterJugadores
                } else {
                    // Limpiamos el spinner de jugadores si no hay equipos
                    spinJugador.adapter = null
                }
            }
        }

        val actualizarJugadores: () -> Unit = {
            lifecycleScope.launch {
                val jugadores = obtenerJugadoresDeEquipo(spinEquipo.selectedItem.toString())
                spinJugador.adapter = ArrayAdapter(this@MainInvitadoActivity, R.layout.spinner_item, jugadores)

            }
        }

        spinCategoria.onItemSelectedListener = crearOnItemSelectedListener(actualizarEquipos)
        spinSexo.onItemSelectedListener = crearOnItemSelectedListener(actualizarEquipos)
        spinEquipo.onItemSelectedListener = crearOnItemSelectedListener(actualizarJugadores)

        btnAgregar.setOnClickListener {
            val jugadorSeleccionado = spinJugador.selectedItem as? JugadorItemSpinner
            val id = jugadorSeleccionado?.id
            if (!id.isNullOrEmpty() && !idsJugadores.contains(id)) {
                idsJugadores.add(id)
                prefs.edit { putStringSet("idsJugadores", idsJugadores.toSet()) }
                cargarJugadores()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Jugador ya agregado o no seleccionado", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun inicializarSpinners(spinCategoria: Spinner, spinSexo: Spinner) {
        val categorias = listOf("Senior", "Junior", "Cadete", "Infantil")
        val sexos = listOf("Masculino", "Femenino")
        spinCategoria.adapter = ArrayAdapter(this, R.layout.spinner_item, categorias)
        spinSexo.adapter = ArrayAdapter(this, R.layout.spinner_item, sexos)
    }

    private fun crearOnItemSelectedListener(action: () -> Unit): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>, p1: View?, p2: Int, p3: Long) = action()
            override fun onNothingSelected(p0: AdapterView<*>) {}
        }
    }

    private suspend fun obtenerEquiposFiltrados(categoria: String, sexo: String): List<String> {
        val equipos = db.collection("Equipos").get().await()
        return equipos.filter {
            it["Categoria"] == categoria && it["Sexo"] == sexo
        }.map { it.id }
    }

    private suspend fun obtenerJugadoresDeEquipo(idEquipo: String): List<JugadorItemSpinner> = withContext(Dispatchers.IO) {
        val equipo = db.collection("Equipos").document(idEquipo).get().await()
        val plantilla = equipo.get("Jugadores") as? Map<*, *> ?: return@withContext emptyList()

        plantilla.keys.mapNotNull { id ->
            val jugadorDoc = db.collection("Jugadores").document(id.toString()).get().await()
            val nombre = "${jugadorDoc["Apellido1"]} ${jugadorDoc["Apellido2"]}, ${jugadorDoc["Nombre"]}"
            JugadorItemSpinner(nombre, id.toString())
        }
    }

}
