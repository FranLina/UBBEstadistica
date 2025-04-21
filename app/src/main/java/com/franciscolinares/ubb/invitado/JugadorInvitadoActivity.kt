package com.franciscolinares.ubb.invitado

import android.annotation.SuppressLint
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.ActivityJugadorInvitadoBinding
import com.franciscolinares.ubb.partido.ListViewPartido.Partido
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.franciscolinares.ubb.utils.safeAwait
import kotlinx.coroutines.launch


class JugadorInvitadoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJugadorInvitadoBinding
    private val db = Firebase.firestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJugadorInvitadoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        enableEdgeToEdge()

        recuperarInfo()
        recuperInfoMediaYtotal()
        recuperarInfoPartidos()

    }

    @SuppressLint("SetTextI18n")
    private fun recuperarInfo() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idJugador = prefs.getString("idJugador", "").toString()

        lifecycleScope.launch {
            val jugador = db.collection("Jugadores").document(idJugador).get().safeAwait() ?: return@launch

            // Datos jugador
            binding.txtJugNomInv.text = "${jugador["Apellido1"]} ${jugador["Apellido2"]}, ${jugador["Nombre"]}"
            binding.txtJugCatInv.text = "${jugador["Categoria"]} ${jugador["Sexo"]}"

            val urlFotoJugador = jugador.getString("UrlFoto")
            if (!urlFotoJugador.isNullOrEmpty()) {
                Picasso.get()
                    .load(urlFotoJugador)
                    .placeholder(R.drawable.jugador_de_baloncesto)
                    .error(R.drawable.jugador_de_baloncesto)
                    .into(binding.imgJugInv)
            }

            // Datos equipo
            val equipoId = jugador.getString("Equipo") ?: return@launch
            val equipo = db.collection("Equipos").document(equipoId).get().safeAwait()
            equipo?.let {
                binding.txtEquInv.text = it.getString("Nombre") ?: ""
                val urlFotoEquipo = it.getString("UrlFoto")
                if (!urlFotoEquipo.isNullOrEmpty()) {
                    Picasso.get()
                        .load(urlFotoEquipo)
                        .placeholder(R.drawable.escudo_equipo)
                        .error(R.drawable.escudo_equipo)
                        .into(binding.imgEquInv)
                }
            }

            // Partidos y estadísticas
            val partidosSnap = db.collection("Partidos").get().safeAwait() ?: return@launch
            var parJug = 0
            var punTot = 0

            for (partido in partidosSnap.documents) {
                val esDelEquipo = partido.getString("EquipoLocal") == equipoId || partido.getString("EquipoVisitante") == equipoId
                if (!esDelEquipo) continue

                val estadistica = db.collection("Estadisticas").document(partido.id).get().safeAwait() ?: continue
                val listadoJugadores = estadistica.get("ListadoJugadores") as? List<String> ?: continue

                if (!listadoJugadores.contains(idJugador)) continue

                val jug = estadistica.get(idJugador) as? HashMap<String, Any> ?: continue
                val minutos = jug["minutos"]?.toString() ?: "00:00"
                if (minutos == "00:00") continue

                parJug++
                punTot += jug["puntos"].toString().toIntOrNull() ?: 0
            }

            val punPar = if (parJug != 0) punTot.toFloat() / parJug else 0f
            val punParFormateado = String.format(Locale.US, "%.1f", punPar)

            binding.txtPJInv.text = "PJ: $parJug"
            binding.txtPPPInv.text = "PPP: $punParFormateado"
            binding.txtPTSInv.text = "PTS: $punTot"
        }
    }

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    private fun recuperarInfoPartidos() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idJugador = prefs.getString("idJugador", "").toString()

        lifecycleScope.launch {
            val jugador = db.collection("Jugadores").document(idJugador).get().safeAwait() ?: return@launch
            val equipoJugador = jugador.getString("Equipo") ?: return@launch

            val partidosSnap = db.collection("Partidos").get().safeAwait() ?: return@launch

            val listaPartido = partidosSnap.documents.mapNotNull { partido ->
                val equipoLocal = partido.getString("EquipoLocal") ?: return@mapNotNull null
                val equipoVisitante = partido.getString("EquipoVisitante") ?: return@mapNotNull null
                if (equipoLocal == equipoJugador || equipoVisitante == equipoJugador) {
                    val fechaString = partido.getString("Fecha") ?: return@mapNotNull null
                    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val fechaDate = try {
                        formato.parse(fechaString)
                    } catch (e: Exception) {
                        null
                    } ?: return@mapNotNull null

                    Partido(
                        partido.id,
                        equipoLocal,
                        equipoVisitante,
                        partido.getString("Polideportivo") ?: "",
                        partido.getString("Resultado") ?: "",
                        partido.getString("Hora") ?: "",
                        fechaString,
                        fechaDate,
                        partido.getString("Estado") ?: ""
                    )
                } else null
            }.sortedBy { it.fechaDate }

            var parJug = 0

            for (partido in listaPartido) {
                val estadistica = db.collection("Estadisticas").document(partido.id).get().safeAwait() ?: continue
                val listaJugadores = estadistica.get("ListadoJugadores") as? List<String> ?: continue

                if (!listaJugadores.contains(idJugador)) continue

                val jug = estadistica.get(idJugador) as? HashMap<String, Any> ?: continue
                if (jug["minutos"] == "00:00") continue

                val registro = LayoutInflater.from(binding.root.context)
                    .inflate(R.layout.row_jugador_estadisticas_invitado, null, false)

                if (parJug % 2 != 0)
                    registro.findViewById<TableRow>(R.id.filaJEstaInvitado).setBackgroundColor("#FFE4E4E4".toColorInt())

                parJug++

                val rival = if (partido.local == equipoJugador) partido.visitante else partido.local
                registro.findViewById<TextView>(R.id.txtNEInvitado).text = "$rival ${partido.fecha}"
                registro.findViewById<TextView>(R.id.txtMinJInvitado).text = jug["minutos"].toString()
                registro.findViewById<TextView>(R.id.txtPTSInvitado).text = jug["puntos"].toString()
                registro.findViewById<TextView>(R.id.txtTLInvitado).text =
                    jug["tlA"].toString() + "/" + (jug["tlA"].toString().toInt() + jug["tlF"].toString().toInt())
                registro.findViewById<TextView>(R.id.txtTLPorInvitado).text =
                    ((jug["tlA"].toString().toDouble() / (jug["tlA"].toString().toDouble() + jug["tlF"].toString()
                        .toDouble())) * 100).toInt().toString() + "%"
                registro.findViewById<TextView>(R.id.txtT2Invitado).text =
                    jug["tc2pA"].toString() + "/" + (jug["tc2pA"].toString().toInt() + jug["tc2pF"].toString().toInt())
                registro.findViewById<TextView>(R.id.txtT2PorInvitado).text =
                    ((jug["tc2pA"].toString().toDouble() / (jug["tc2pA"].toString().toDouble() + jug["tc2pF"].toString()
                        .toDouble())) * 100).toInt().toString() + "%"
                registro.findViewById<TextView>(R.id.txtT3Invitado).text =
                    jug["tc3pA"].toString() + "/" + (jug["tc3pA"].toString().toInt() + jug["tc3pF"].toString().toInt())
                registro.findViewById<TextView>(R.id.txtT3PorInvitado).text =
                    ((jug["tc3pA"].toString().toDouble() / (jug["tc3pA"].toString().toDouble() + jug["tc3pF"].toString()
                        .toDouble())) * 100).toInt().toString() + "%"
                registro.findViewById<TextView>(R.id.txtRDInvitado).text = jug["rebD"].toString()
                registro.findViewById<TextView>(R.id.txtROInvitado).text = jug["rebO"].toString()
                registro.findViewById<TextView>(R.id.txtRTInvitado).text =
                    "" + (jug["rebD"].toString().toInt() + jug["rebO"].toString().toInt())
                registro.findViewById<TextView>(R.id.txtASTInvitado).text = jug["asi"].toString()
                registro.findViewById<TextView>(R.id.txtPERInvitado).text = jug["per"].toString()
                registro.findViewById<TextView>(R.id.txtRECInvitado).text = jug["recu"].toString()
                registro.findViewById<TextView>(R.id.txtTAPInvitado).text = jug["taCom"].toString()
                registro.findViewById<TextView>(R.id.txtFALInvitado).text = jug["falC"].toString()
                registro.findViewById<TextView>(R.id.txtVALInvitado).text = jug["val"].toString()

                binding.TLJugEstaInvitado.addView(registro)
            }
        }
    }

    @SuppressLint("SetTextI18n", "ResourceAsColor", "InflateParams", "MissingInflatedId")
    private fun recuperInfoMediaYtotal() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idJugador = prefs.getString("idJugador", "").toString()

        var min = 0
        var pts = 0
        var tc2A = 0
        var tc2F = 0
        var tc3A = 0
        var tc3F = 0
        var tlA = 0
        var tlF = 0
        var rebO = 0
        var rebD = 0
        var asi = 0
        var rec = 0
        var per = 0
        var tapC = 0
        var falC = 0
        var valoracion = 0

        lifecycleScope.launch {
            val jugador = db.collection("Jugadores").document(idJugador).get().safeAwait() ?: return@launch
            val equipoJugador = jugador.getString("Equipo") ?: return@launch

            val partidosSnap = db.collection("Partidos").get().safeAwait() ?: return@launch

            val listaPartido = partidosSnap.documents.mapNotNull { partido ->
                val equipoLocal = partido.getString("EquipoLocal") ?: return@mapNotNull null
                val equipoVisitante = partido.getString("EquipoVisitante") ?: return@mapNotNull null
                if (equipoLocal == equipoJugador || equipoVisitante == equipoJugador) {
                    val fechaString = partido.getString("Fecha") ?: return@mapNotNull null
                    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val fechaDate = try {
                        formato.parse(fechaString)
                    } catch (e: Exception) {
                        null
                    } ?: return@mapNotNull null

                    Partido(
                        partido.id,
                        equipoLocal,
                        equipoVisitante,
                        partido.getString("Polideportivo") ?: "",
                        partido.getString("Resultado") ?: "",
                        partido.getString("Hora") ?: "",
                        fechaString,
                        fechaDate,
                        partido.getString("Estado") ?: ""
                    )
                } else null
            }.sortedBy { it.fechaDate }

            var parJug = 0

            for (partido in listaPartido) {
                val estadistica = db.collection("Estadisticas").document(partido.id).get().safeAwait() ?: continue
                val listaJugadores = estadistica.get("ListadoJugadores") as? List<String> ?: continue

                if (!listaJugadores.contains(idJugador)) continue

                val jug = estadistica.get(idJugador) as? HashMap<String, Any> ?: continue
                if (jug["minutos"] == "00:00") continue

                parJug++
                min += tiempoASegundos(jug["minutos"].toString())
                pts += jug["puntos"].toString().toInt()
                tlA += jug["tlA"].toString().toInt()
                tlF += jug["tlF"].toString().toInt()
                tc2A += jug["tc2pA"].toString().toInt()
                tc2F += jug["tc2pF"].toString().toInt()
                tc3A += jug["tc3pA"].toString().toInt()
                tc3F += jug["tc3pF"].toString().toInt()
                rebD += jug["rebD"].toString().toInt()
                rebO += jug["rebO"].toString().toInt()
                asi += jug["asi"].toString().toInt()
                per += jug["per"].toString().toInt()
                rec += jug["recu"].toString().toInt()
                tapC += jug["taCom"].toString().toInt()
                falC += jug["falC"].toString().toInt()
                valoracion += jug["val"].toString().toInt()
            }

            val promedioMinutos = if (parJug != 0) String.format(Locale.US, "%.1f", min / parJug / 60f) else "0.0"
            val registroMedia = LayoutInflater.from(binding.root.context).inflate(R.layout.row_jugador_estadistica_invitado_total_media, null, false)
            registroMedia.findViewById<TextView>(R.id.txtNEInvitado).text = "MEDIA"
            registroMedia.findViewById<TextView>(R.id.txtMinJInvitado).text = promedioMinutos
            registroMedia.findViewById<TextView>(R.id.txtPTSInvitado).text = String.format(Locale.US, "%.1f", (pts.toDouble() / parJug))
            registroMedia.findViewById<TextView>(R.id.txtTLInvitado).text = String.format(Locale.US, "%.1f", (tlA.toDouble() / parJug))
            registroMedia.findViewById<TextView>(R.id.txtTLPorInvitado).text =
                ((tlA.toDouble() / (tlA.toDouble() + tlF.toDouble())) * 100).toInt().toString() + "%"
            registroMedia.findViewById<TextView>(R.id.txtT2Invitado).text = String.format(Locale.US, "%.1f", (tc2A.toDouble() / parJug))
            registroMedia.findViewById<TextView>(R.id.txtT2PorInvitado).text =
                ((tc2A.toDouble() / (tc2A.toDouble() + tc2F.toDouble())) * 100).toInt().toString() + "%"
            registroMedia.findViewById<TextView>(R.id.txtT3Invitado).text = String.format(Locale.US, "%.1f", (tc3A.toDouble() / parJug))
            registroMedia.findViewById<TextView>(R.id.txtT3PorInvitado).text =
                ((tc3A.toDouble() / (tc3A.toDouble() + tc3F.toDouble())) * 100).toInt().toString() + "%"
            registroMedia.findViewById<TextView>(R.id.txtRDInvitado).text = String.format(Locale.US, "%.1f", (rebD.toDouble() / parJug))
            registroMedia.findViewById<TextView>(R.id.txtROInvitado).text = String.format(Locale.US, "%.1f", (rebO.toDouble() / parJug))
            registroMedia.findViewById<TextView>(R.id.txtRTInvitado).text = String.format(Locale.US, "%.1f", ((rebO.toDouble() + rebD.toDouble()) / parJug))
            registroMedia.findViewById<TextView>(R.id.txtASTInvitado).text = String.format(Locale.US, "%.1f", (asi.toDouble() / parJug))
            registroMedia.findViewById<TextView>(R.id.txtPERInvitado).text = String.format(Locale.US, "%.1f", (per.toDouble() / parJug))
            registroMedia.findViewById<TextView>(R.id.txtRECInvitado).text = String.format(Locale.US, "%.1f", (rec.toDouble() / parJug))
            registroMedia.findViewById<TextView>(R.id.txtTAPInvitado).text = String.format(Locale.US, "%.1f", (tapC.toDouble() / parJug))
            registroMedia.findViewById<TextView>(R.id.txtFALInvitado).text = String.format(Locale.US, "%.1f", (falC.toDouble() / parJug))
            registroMedia.findViewById<TextView>(R.id.txtVALInvitado).text = String.format(Locale.US, "%.1f", (valoracion.toDouble() / parJug))
            binding.TLTotalMedia.addView(registroMedia)

            val registroTotal = LayoutInflater.from(binding.root.context).inflate(R.layout.row_jugador_estadistica_invitado_total_media, null, false)
            registroTotal.findViewById<TextView>(R.id.txtNEInvitado).text = "TOTAL"
            registroTotal.findViewById<TextView>(R.id.txtMinJInvitado).text = segundosATiempo(min)
            registroTotal.findViewById<TextView>(R.id.txtPTSInvitado).text = pts.toString()
            registroTotal.findViewById<TextView>(R.id.txtTLInvitado).text = tlA.toString() + "/" + (tlA + tlF)
            registroTotal.findViewById<TextView>(R.id.txtTLPorInvitado).text =
                ((tlA.toDouble() / (tlA.toDouble() + tlF.toDouble())) * 100).toInt().toString() + "%"
            registroTotal.findViewById<TextView>(R.id.txtT2Invitado).text = tc2A.toString() + "/" + (tc2A + tc2F)
            registroTotal.findViewById<TextView>(R.id.txtT2PorInvitado).text =
                ((tc2A.toDouble() / (tc2A.toDouble() + tc2F.toDouble())) * 100).toInt().toString() + "%"
            registroTotal.findViewById<TextView>(R.id.txtT3Invitado).text = tc3A.toString() + "/" + (tc3A + tc3F)
            registroTotal.findViewById<TextView>(R.id.txtT3PorInvitado).text =
                ((tc3A.toDouble() / (tc3A.toDouble() + tc3F.toDouble())) * 100).toInt().toString() + "%"
            registroTotal.findViewById<TextView>(R.id.txtRDInvitado).text = rebD.toString()
            registroTotal.findViewById<TextView>(R.id.txtROInvitado).text = rebO.toString()
            registroTotal.findViewById<TextView>(R.id.txtRTInvitado).text = (rebD + rebO).toString()
            registroTotal.findViewById<TextView>(R.id.txtASTInvitado).text = asi.toString()
            registroTotal.findViewById<TextView>(R.id.txtPERInvitado).text = per.toString()
            registroTotal.findViewById<TextView>(R.id.txtRECInvitado).text = rec.toString()
            registroTotal.findViewById<TextView>(R.id.txtTAPInvitado).text = tapC.toString()
            registroTotal.findViewById<TextView>(R.id.txtFALInvitado).text = falC.toString()
            registroTotal.findViewById<TextView>(R.id.txtVALInvitado).text = valoracion.toString()
            binding.TLTotalMedia.addView(registroTotal)
        }
    }

    // Convierte "MM:SS" a segundos
    private fun tiempoASegundos(tiempo: String): Int {
        val partes = tiempo.split(":")
        val minutos = partes[0].toIntOrNull() ?: 0
        val segundos = partes[1].toIntOrNull() ?: 0
        return minutos * 60 + segundos
    }

    // Convierte segundos a "MM:SS"
    @SuppressLint("DefaultLocale")
    private fun segundosATiempo(segundosTotales: Int): String {
        val minutos = segundosTotales / 60
        val segundos = segundosTotales % 60
        return String.format("%02d:%02d", minutos, segundos)
    }

}