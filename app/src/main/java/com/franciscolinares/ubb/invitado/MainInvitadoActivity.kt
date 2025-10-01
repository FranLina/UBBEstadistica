package com.franciscolinares.ubb.invitado

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.franciscolinares.ubb.LoginActivity
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.ActivityMainInvitadoBinding
import com.franciscolinares.ubb.invitado.RecyclerViewInvitado.*
import com.franciscolinares.ubb.utils.safeAwait
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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
    private var cargasActivas = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainInvitadoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        lifecycleScope.launch {
            val userDoc = db.collection("Users").document(userId).get().await()
            idsEquipos = (userDoc.get("mis_equipos") as? List<String>)?.toMutableList() ?: mutableListOf()
            idsJugadores = (userDoc.get("mis_jugadores") as? List<String>)?.toMutableList() ?: mutableListOf()

            cargarEquipos()
            cargarJugadores()
        }


        binding.txtAAdirEqu.setOnClickListener { mostrarDialogoAgregarEquipo() }
        binding.txtAAdirJug.setOnClickListener { mostrarDialogoAgregarJugador() }

        binding.btnLogout.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                db.collection("Users").document(user.uid)
                    .update("deviceToken", FieldValue.delete())
                    .addOnSuccessListener {
                        FirebaseAuth.getInstance().signOut()
                        startActivity(Intent(this, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        finish()
                    }
                    .addOnFailureListener {
                        Log.e("Logout", "Error al borrar el token: ${it.message}")
                    }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun cargarEquipos() {
        lifecycleScope.launch {
            iniciarCarga()
            try {
                val equiposMap = mutableMapOf<String, Equipo>()
                val snapshot = db.collection("Equipos").get().await()
                val partidosSnap = db.collection("Partidos").get().safeAwait() ?: return@launch

                for (equi in snapshot) {
                    if (idsEquipos.contains(equi.id)) {
                        // Partidos y estadísticas
                        var conPar = 0
                        var conParG = 0
                        for (partido in partidosSnap) {
                            if (partido["EquipoLocal"].toString() == equi.id || partido["EquipoVisitante"].toString() == equi.id) {
                                if (partido["Estado"].toString() == "Finalizado")
                                    conPar++
                                if (partido["EquipoLocal"].toString() == equi.id) {
                                    if (comprobarGanador(partido["Resultado"].toString()) == 1)
                                        conParG++
                                } else if (partido["EquipoVisitante"].toString() == equi.id) {
                                    if (comprobarGanador(partido["Resultado"].toString()) == 0)
                                        conParG++
                                }
                            }
                        }

                        equiposMap[equi.id] = Equipo(
                            equi.id,
                            equi["Nombre"].toString(),
                            equi["Categoria"].toString(),
                            equi["Sexo"].toString(),
                            conPar.toString(),
                            conParG.toString(),
                            equi["UrlFoto"].toString()
                        )
                    }
                }

                // Ordenar los equipos según el orden de idsEquipos
                val equiposOrdenados = idsEquipos.mapNotNull { equiposMap[it] }.toMutableList()

                equipoAdapter = EquipoAdapter(equiposOrdenados, { equipo ->
                    startActivity(Intent(this@MainInvitadoActivity, MainEquipoInvitadoActivity::class.java).apply {
                        putExtra("idEquipo", equipo.idEquipo)
                    })
                }, { equipo ->
                    val index = equiposOrdenados.indexOfFirst { it.idEquipo == equipo.idEquipo }
                    if (index != -1) {
                        equiposOrdenados.removeAt(index)
                        equipoAdapter.notifyItemRemoved(index)
                    }
                    idsEquipos.remove(equipo.idEquipo)
                    actualizarIds("mis_equipos", idsEquipos)

                })

                binding.rvEquipos.layoutManager = LinearLayoutManager(this@MainInvitadoActivity, LinearLayoutManager.HORIZONTAL, false)
                binding.rvEquipos.adapter = equipoAdapter
            } catch (e: Exception) {
                Log.e("Firebase", "Error al cargar jugadores: ${e.message}")
            } finally {
                finalizarCarga()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun cargarJugadores() {
        lifecycleScope.launch {
            iniciarCarga()
            try {
                val jugadoresMap = mutableMapOf<String, Jugador>()
                val snapshot = db.collection("Jugadores").get().await()
                val partidosSnap = db.collection("Partidos").get().safeAwait() ?: return@launch

                for (jug in snapshot) {
                    if (idsJugadores.contains(jug.id)) {
                        var parJug = 0
                        var punTot = 0

                        for (partido in partidosSnap.documents) {
                            val esDelEquipo =
                                partido.getString("EquipoLocal") == jug["Equipo"].toString() || partido.getString("EquipoVisitante") == jug["Equipo"].toString()
                            if (!esDelEquipo) continue

                            val estadistica = db.collection("Estadisticas").document(partido.id).get().safeAwait() ?: continue
                            val listadoJugadores = estadistica.get("ListadoJugadores") as? List<String> ?: continue

                            if (!listadoJugadores.contains(jug.id)) continue

                            val jugStats = estadistica.get(jug.id) as? HashMap<String, Any> ?: continue
                            val minutos = jugStats["minutos"]?.toString() ?: "00:00"
                            if (minutos == "00:00") continue

                            parJug++
                            punTot += jugStats["puntos"].toString().toIntOrNull() ?: 0
                        }

                        jugadoresMap[jug.id] = Jugador(
                            jug.id,
                            "${jug["Apellido1"]} ${jug["Apellido2"]}, ${jug["Nombre"]}",
                            jug["Categoria"].toString(),
                            jug["Sexo"].toString(),
                            punTot.toString(),
                            parJug.toString(),
                            jug["UrlFoto"].toString()
                        )
                    }
                }

                // Ordenar los jugadores según el orden de idsJugadores
                val jugadoresOrdenados = idsJugadores.mapNotNull { jugadoresMap[it] }.toMutableList()

                jugadorAdapter = JugadorAdapter(jugadoresOrdenados, { jugador ->
                    startActivity(Intent(this@MainInvitadoActivity, JugadorInvitadoActivity::class.java).apply {
                        putExtra("idJugador", jugador.idJugador)
                    })
                }, { jugador ->
                    val index = jugadoresOrdenados.indexOfFirst { it.idJugador == jugador.idJugador }
                    if (index != -1) {
                        jugadoresOrdenados.removeAt(index)
                        jugadorAdapter.notifyItemRemoved(index)
                    }
                    idsJugadores.remove(jugador.idJugador)
                    actualizarIds("mis_jugadores", idsJugadores)
                })

                binding.rvJugadores.layoutManager = LinearLayoutManager(this@MainInvitadoActivity, LinearLayoutManager.HORIZONTAL, false)
                binding.rvJugadores.adapter = jugadorAdapter
            } catch (e: Exception) {
                Log.e("Firebase", "Error al cargar jugadores: ${e.message}")
            } finally {
                finalizarCarga()
            }
        }
    }

    private fun iniciarCarga() {
        cargasActivas++
        mostrarCarga(true)
    }

    private fun finalizarCarga() {
        cargasActivas--
        if (cargasActivas <= 0) {
            mostrarCarga(false)
            cargasActivas = 0
        }
    }


    private fun mostrarCarga(mostrar: Boolean) {
        if (mostrar) {
            binding.loadingOverlay.show()
        } else {
            binding.loadingOverlay.hide()
        }
    }

    private fun comprobarGanador(resultado: String): Int {
        val ptsL = resultado.split(" - ")[0].toInt()
        val ptsV = resultado.split(" - ")[1].toInt()

        return if (ptsL > ptsV) 1 else if (ptsV > ptsL) 0 else -1
    }

    private fun actualizarIds(campo: String, lista: List<String>) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("Users").document(userId).update(campo, lista)
    }

    private fun mostrarDialogoAgregarEquipo() {
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
                actualizarIds("mis_equipos", idsEquipos)
                cargarEquipos()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Equipo ya agregado o no seleccionado", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun mostrarDialogoAgregarJugador() {
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
                actualizarIds("mis_jugadores", idsJugadores)
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
