package com.franciscolinares.ubb.invitado

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.edit
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentJugadoresEquipoInvitadoBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale
import androidx.core.graphics.toColorInt
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Picasso


class JugadoresEquipoInvitadoFragment : Fragment() {

    private var _binding: FragmentJugadoresEquipoInvitadoBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("InflateParams", "SetTextI18n", "DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJugadoresEquipoInvitadoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idEquipo = prefs.getString("idEquipo", "").toString()

        db.collection("Equipos").document(idEquipo).get().addOnSuccessListener {
            val plantilla: HashMap<String, String> = it.get("Jugadores") as HashMap<String, String>

            plantilla.entries.withIndex().forEach { (index, jugador) ->
                val registro = LayoutInflater.from(binding.root.context).inflate(R.layout.row_jugador_equipo_invitado, null, false)

                if (index % 2 != 0)
                    registro.findViewById<TableRow>(R.id.filaJEInvitado).setBackgroundColor("#FFE4E4E4".toColorInt())

                db.collection("Jugadores").document(jugador.key).get().addOnSuccessListener { it2 ->

                    registro.findViewById<TextView>(R.id.txtFilaJEINombre).text =
                        it2["Apellido1"].toString() + " " + it2["Apellido2"].toString() + ", " + it2["Nombre"].toString()
                    if (it2.get("UrlFoto").toString() != "")
                        Picasso.get().load(it2.get("UrlFoto").toString()).placeholder(R.drawable.jugador_de_baloncesto)
                            .error(R.drawable.jugador_de_baloncesto)
                            .into(registro.findViewById<ImageView>(R.id.fotoJEInvitado))
                    registro.setOnClickListener {
                        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
                        prefs.edit {
                            putString("idJugador", jugador.key)
                        }
                        val intent = Intent(binding.root.context, JugadorInvitadoActivity::class.java)
                        startActivity(intent)
                    }
                }

                db.collection("Partidos").get().addOnSuccessListener { it2 ->
                    var conPartidos = 0
                    var parJug = 0
                    var minPar = 0
                    var punPar = 0
                    var asiPar = 0
                    var rebPar = 0
                    for (partido in it2) {
                        if (partido["EquipoLocal"].toString() == idEquipo || partido["EquipoVisitante"].toString() == idEquipo) {
                            conPartidos++
                            db.collection("Estadisticas").document(partido.id).get().addOnSuccessListener { it3 ->
                                val plan = it3.get("ListadoJugadores") as? ArrayList<String> ?: arrayListOf()
                                if (plan.contains(jugador.key)) {
                                    val jug = it3.get(jugador.key) as? HashMap<String, Any>
                                    if (jug != null) {
                                        if (jug["minutos"] != "00:00") {
                                            parJug++
                                            minPar += tiempoASegundos(jug["minutos"].toString())
                                            punPar += jug["puntos"].toString().toInt()
                                            asiPar += jug["asi"].toString().toInt()
                                            rebPar += (jug["rebD"].toString().toInt() + jug["rebO"].toString().toInt())
                                        }
                                    }
                                }
                                val promedioPuntos = if (parJug != 0) punPar.toFloat() / parJug else 0f
                                val promedioPuntosFormateado = String.format(Locale.US, "%.1f", promedioPuntos)
                                val promedioAsistencias = if (parJug != 0) asiPar.toFloat() / parJug else 0f
                                val promedioAsistenciasFormateado = String.format(Locale.US, "%.1f", promedioAsistencias)
                                val promedioRebotes = if (parJug != 0) rebPar.toFloat() / parJug else 0f
                                val promedioRebotesFormateado = String.format(Locale.US, "%.1f", promedioRebotes)
                                val promedioMinutos = if (parJug != 0) String.format(Locale.US, "%.1f", minPar.toFloat() / parJug / 60f) else "0.0"

                                registro.findViewById<TextView>(R.id.txtFilaJEIPart).text = "" + conPartidos
                                registro.findViewById<TextView>(R.id.txtFilaJEIPJ).text = "" + parJug
                                registro.findViewById<TextView>(R.id.txtFilaJEIMPP).text = "" + promedioMinutos
                                registro.findViewById<TextView>(R.id.txtFilaJEIPPP).text = "" + promedioPuntosFormateado
                                registro.findViewById<TextView>(R.id.txtFilaJEIAPP).text = "" + promedioAsistenciasFormateado
                                registro.findViewById<TextView>(R.id.txtFilaJEIRPP).text = "" + promedioRebotesFormateado
                            }

                        }
                    }
                }
                binding.TLJugadoresInvitado.addView(registro)

            }
        }

        return root
    }

    // Convierte "MM:SS" a segundos
    private fun tiempoASegundos(tiempo: String): Int {
        val partes = tiempo.split(":")
        val minutos = partes[0].toIntOrNull() ?: 0
        val segundos = partes[1].toIntOrNull() ?: 0
        return minutos * 60 + segundos
    }
}