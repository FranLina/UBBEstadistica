package com.franciscolinares.ubb.invitado

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.ActivityJugadorInvitadoBinding
import com.franciscolinares.ubb.partido.ListViewPartido.Partido
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale
import com.franciscolinares.ubb.utils.safeAwait
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.Filter
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

class JugadorInvitadoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJugadorInvitadoBinding
    private val db = Firebase.firestore
    private var idJugador: String? = null

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

        idJugador = intent.getStringExtra("idJugador")
        recuperarInfo(binding.root.context)
        recuperInfoMediaYtotal()
        recuperarInfoPartidos()

        binding.btnExcelJugador.setOnClickListener {
            exportarEstadisticasAPartirDeFirebase(binding.root.context)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun recuperarInfo(context: Context) {
        lifecycleScope.launch {
            val jugador = db.collection("Jugadores").document(idJugador.toString()).get().safeAwait() ?: return@launch

            // Datos jugador
            binding.txtJugNomInv.text = "${jugador["Apellido1"]} ${jugador["Apellido2"]}, ${jugador["Nombre"]}"
            binding.txtJugCatInv.text = "${jugador["Categoria"]} ${jugador["Sexo"]}"

            val urlFotoJugador = jugador.getString("UrlFoto")
            if (!urlFotoJugador.isNullOrEmpty()) {
                Glide.with(context).load(urlFotoJugador).placeholder(R.drawable.jugador_de_baloncesto).error(R.drawable.jugador_de_baloncesto)
                    .into(binding.imgJugInv)
            }

            // Datos equipo
            val equipoId = jugador.getString("Equipo") ?: return@launch
            val equipo = db.collection("Equipos").document(equipoId).get().safeAwait()
            equipo?.let {
                binding.txtEquInv.text = it.getString("Nombre") ?: ""
                val urlFotoEquipo = it.getString("UrlFoto")
                if (!urlFotoEquipo.isNullOrEmpty()) {
                    Glide.with(binding.root.context).load(urlFotoEquipo).placeholder(R.drawable.escudopredeterminado).centerCrop().override(100, 100)
                        .error(R.drawable.escudopredeterminado).into(binding.imgEquInv)
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

                if (!listadoJugadores.contains(idJugador.toString())) continue

                val jug = estadistica.get(idJugador.toString()) as? HashMap<String, Any> ?: continue
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
        lifecycleScope.launch {
            val jugador = db.collection("Jugadores").document(idJugador.toString()).get().safeAwait() ?: return@launch
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
                        equipoLocal.dropLast(2),
                        equipoVisitante.dropLast(2),
                        partido.getString("Polideportivo") ?: "",
                        partido.getString("Resultado") ?: "",
                        partido.get("Cuartos") as List<String>,
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

                if (!listaJugadores.contains(idJugador.toString())) continue

                val jug = estadistica.get(idJugador.toString()) as? HashMap<String, Any> ?: continue
                if (jug["minutos"] == "00:00") continue

                val registro = LayoutInflater.from(binding.root.context).inflate(R.layout.row_jugador_estadisticas_invitado, null, false)

                if (parJug % 2 != 0) registro.findViewById<TableRow>(R.id.filaJEstaInvitado).setBackgroundColor("#FFE4E4E4".toColorInt())

                parJug++

                val rival = if (partido.local == equipoJugador) partido.visitante else partido.local
                registro.findViewById<TextView>(R.id.txtNEInvitado).text = "$rival ${partido.fecha}"
                registro.findViewById<TextView>(R.id.txtMinJInvitado).text = jug["minutos"].toString()
                registro.findViewById<TextView>(R.id.txtPTSInvitado).text = jug["puntos"].toString()
                registro.findViewById<TextView>(R.id.txtTLInvitado).text =
                    jug["tlA"].toString() + "/" + (jug["tlA"].toString().toInt() + jug["tlF"].toString().toInt())
                registro.findViewById<TextView>(R.id.txtTLPorInvitado).text =
                    ((jug["tlA"].toString().toDouble() / (jug["tlA"].toString().toDouble() + jug["tlF"].toString().toDouble())) * 100).toInt()
                        .toString() + "%"
                registro.findViewById<TextView>(R.id.txtT2Invitado).text =
                    jug["tc2pA"].toString() + "/" + (jug["tc2pA"].toString().toInt() + jug["tc2pF"].toString().toInt())
                registro.findViewById<TextView>(R.id.txtT2PorInvitado).text =
                    ((jug["tc2pA"].toString().toDouble() / (jug["tc2pA"].toString().toDouble() + jug["tc2pF"].toString().toDouble())) * 100).toInt()
                        .toString() + "%"
                registro.findViewById<TextView>(R.id.txtT3Invitado).text =
                    jug["tc3pA"].toString() + "/" + (jug["tc3pA"].toString().toInt() + jug["tc3pF"].toString().toInt())
                registro.findViewById<TextView>(R.id.txtT3PorInvitado).text =
                    ((jug["tc3pA"].toString().toDouble() / (jug["tc3pA"].toString().toDouble() + jug["tc3pF"].toString().toDouble())) * 100).toInt()
                        .toString() + "%"
                registro.findViewById<TextView>(R.id.txtRDInvitado).text = jug["rebD"].toString()
                registro.findViewById<TextView>(R.id.txtROInvitado).text = jug["rebO"].toString()
                registro.findViewById<TextView>(R.id.txtRTInvitado).text = "" + (jug["rebD"].toString().toInt() + jug["rebO"].toString().toInt())
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
            val jugador = db.collection("Jugadores").document(idJugador.toString()).get().safeAwait() ?: return@launch
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
                        equipoLocal.dropLast(2),
                        equipoVisitante.dropLast(2),
                        partido.getString("Polideportivo") ?: "",
                        partido.getString("Resultado") ?: "",
                        partido.get("Cuartos") as List<String>,
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

                if (!listaJugadores.contains(idJugador.toString())) continue

                val jug = estadistica.get(idJugador.toString()) as? HashMap<String, Any> ?: continue
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

            val registroMedia = LayoutInflater.from(binding.root.context).inflate(R.layout.row_jugador_estadistica_invitado_total_media, null, false)
            registroMedia.findViewById<TextView>(R.id.txtNEInvitado).text = "MEDIA"
            registroMedia.findViewById<TextView>(R.id.txtMinJInvitado).text = formatearPromedioMinutos(min, parJug)
            registroMedia.findViewById<TextView>(R.id.txtPTSInvitado).text = formatearPromedioEstadistica(pts, parJug)
            registroMedia.findViewById<TextView>(R.id.txtTLInvitado).text = formatearPromedioEstadistica(tlA, parJug)
            registroMedia.findViewById<TextView>(R.id.txtTLPorInvitado).text =
                ((tlA.toDouble() / (tlA.toDouble() + tlF.toDouble())) * 100).toInt().toString() + "%"
            registroMedia.findViewById<TextView>(R.id.txtT2Invitado).text = formatearPromedioEstadistica(tc2A, parJug)
            registroMedia.findViewById<TextView>(R.id.txtT2PorInvitado).text =
                ((tc2A.toDouble() / (tc2A.toDouble() + tc2F.toDouble())) * 100).toInt().toString() + "%"
            registroMedia.findViewById<TextView>(R.id.txtT3Invitado).text = formatearPromedioEstadistica(tc3A, parJug)
            registroMedia.findViewById<TextView>(R.id.txtT3PorInvitado).text =
                ((tc3A.toDouble() / (tc3A.toDouble() + tc3F.toDouble())) * 100).toInt().toString() + "%"
            registroMedia.findViewById<TextView>(R.id.txtRDInvitado).text = formatearPromedioEstadistica(rebD, parJug)
            registroMedia.findViewById<TextView>(R.id.txtROInvitado).text = formatearPromedioEstadistica(rebO, parJug)
            registroMedia.findViewById<TextView>(R.id.txtRTInvitado).text =
                String.format(Locale.US, "%.1f", ((rebO.toDouble() + rebD.toDouble()) / parJug))
            registroMedia.findViewById<TextView>(R.id.txtASTInvitado).text = formatearPromedioEstadistica(asi, parJug)
            registroMedia.findViewById<TextView>(R.id.txtPERInvitado).text = formatearPromedioEstadistica(per, parJug)
            registroMedia.findViewById<TextView>(R.id.txtRECInvitado).text = formatearPromedioEstadistica(rec, parJug)
            registroMedia.findViewById<TextView>(R.id.txtTAPInvitado).text = formatearPromedioEstadistica(tapC, parJug)
            registroMedia.findViewById<TextView>(R.id.txtFALInvitado).text = formatearPromedioEstadistica(falC, parJug)
            registroMedia.findViewById<TextView>(R.id.txtVALInvitado).text = formatearPromedioEstadistica(valoracion, parJug)
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

    private fun exportarEstadisticasAPartirDeFirebase(context: Context) {

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

        var nombreJugador = ""

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Estadisticas del Jugador")
        val estilos = crearEstilos(workbook)
        val tamanoCelda = 400
        sheet.setColumnWidth(0, 5000)

        val titulos = listOf(
            "Partido", "MIN", "PTS", "TL", "%TL", "T2", "%T2", "T3", "%T3", "REB D", "REB O", "REB", "AST", "PÉR", "REC", "TAP", "FAL", "VAL"
        )

        // Crear encabezados
        val filaEncabezado = sheet.createRow(0)
        filaEncabezado.height = 500
        titulos.forEachIndexed { index, titulo ->
            val celda = filaEncabezado.createCell(index)
            celda.setCellValue(titulo)
            celda.cellStyle = estilos["encabezadoA"] as XSSFCellStyle?
        }

        lifecycleScope.launch {
            val jugador = db.collection("Jugadores").document(idJugador.toString()).get().safeAwait() ?: return@launch
            val equipoJugador = jugador.getString("Equipo") ?: return@launch
            nombreJugador = jugador.getString("Nombre") ?: return@launch

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
                        equipoLocal.dropLast(2),
                        equipoVisitante.dropLast(2),
                        partido.getString("Polideportivo") ?: "",
                        partido.getString("Resultado") ?: "",
                        partido.get("Cuartos") as List<String>,
                        partido.getString("Hora") ?: "",
                        fechaString,
                        fechaDate,
                        partido.getString("Estado") ?: ""
                    )
                } else null
            }.sortedBy { it.fechaDate }

            Log.d("Excel", "Partidos encontrados: ${listaPartido.size}")

            var parJug = 0
            var index = 0

            for (partido in listaPartido) {

                val estadistica = db.collection("Estadisticas").document(partido.id).get().safeAwait() ?: continue
                val listaJugadores = estadistica.get("ListadoJugadores") as? List<String> ?: continue

                if (!listaJugadores.contains(idJugador.toString())) continue

                val jug = estadistica.get(idJugador.toString()) as? HashMap<String, Any> ?: continue
                if (jug["minutos"] == "00:00") continue

                Log.d("Excel", "Procesando partido: ${partido.id}")
                Log.d("Excel", "Listado jugadores: ${listaJugadores}")

                Log.d("Excel", "Minutos jugador: ${jug["minutos"]}")

                val row = sheet.createRow(index + 1).apply { height = tamanoCelda.toShort() }
                val celdas = List(titulos.size) { colIndex ->
                    row.createCell(colIndex).apply {
                        cellStyle = if (parJug % 2 == 0) (if (colIndex == 0) estilos["primera"] else estilos["celda"]) as XSSFCellStyle?
                        else (if (colIndex == 0) estilos["primeraImpar"] else estilos["celdaImpar"]) as XSSFCellStyle?
                    }
                }.toMutableList()

                parJug++
                index++

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

                val rival = if (partido.local == equipoJugador) partido.visitante else partido.local
                celdas[0].setCellValue("$rival ${partido.fecha}")
                celdas[1].setCellValue(jug["minutos"].toString())
                celdas[2].setCellValue(jug["puntos"].toString())
                celdas[3].setCellValue(jug["tlA"].toString() + "/" + (jug["tlA"].toString().toInt() + jug["tlF"].toString().toInt()))
                celdas[4].setCellValue(
                    ((jug["tlA"].toString().toDouble() / (jug["tlA"].toString().toDouble() + jug["tlF"].toString().toDouble())) * 100).toInt()
                        .toString() + "%"
                )
                celdas[5].setCellValue(jug["tc2pA"].toString() + "/" + (jug["tc2pA"].toString().toInt() + jug["tc2pF"].toString().toInt()))
                celdas[6].setCellValue(
                    ((jug["tc2pA"].toString().toDouble() / (jug["tc2pA"].toString().toDouble() + jug["tc2pF"].toString().toDouble())) * 100).toInt()
                        .toString() + "%"
                )
                celdas[7].setCellValue(jug["tc3pA"].toString() + "/" + (jug["tc3pA"].toString().toInt() + jug["tc3pF"].toString().toInt()))
                celdas[8].setCellValue(
                    ((jug["tc3pA"].toString().toDouble() / (jug["tc3pA"].toString().toDouble() + jug["tc3pF"].toString().toDouble())) * 100).toInt()
                        .toString() + "%"
                )
                celdas[9].setCellValue(jug["rebD"].toString())
                celdas[10].setCellValue(jug["rebO"].toString())
                celdas[11].setCellValue("" + (jug["rebD"].toString().toInt() + jug["rebO"].toString().toInt()))
                celdas[12].setCellValue(jug["asi"].toString())
                celdas[13].setCellValue(jug["per"].toString())
                celdas[14].setCellValue(jug["recu"].toString())
                celdas[15].setCellValue(jug["taCom"].toString())
                celdas[16].setCellValue(jug["falC"].toString())
                celdas[17].setCellValue(jug["val"].toString())
            }

            val rowMedia = sheet.createRow(index + 1).apply { height = tamanoCelda.toShort() }
            val celdasMedia = List(titulos.size) { colIndex ->
                rowMedia.createCell(colIndex).apply {
                    cellStyle = if ((index + 2) % 2 == 0) (if (colIndex == 0) estilos["primera"] else estilos["celda"]) as XSSFCellStyle?
                    else (if (colIndex == 0) estilos["primeraImpar"] else estilos["celdaImpar"]) as XSSFCellStyle?
                }
            }.toMutableList()

            celdasMedia[0].setCellValue("MEDIA")
            celdasMedia[1].setCellValue(formatearPromedioMinutos(min, parJug))
            celdasMedia[2].setCellValue(formatearPromedioEstadistica(pts, parJug))
            celdasMedia[3].setCellValue(formatearPromedioEstadistica(tlA, parJug))
            celdasMedia[4].setCellValue(((tlA.toDouble() / (tlA.toDouble() + tlF.toDouble())) * 100).toInt().toString() + "%")
            celdasMedia[5].setCellValue(formatearPromedioEstadistica(tc2A, parJug))
            celdasMedia[6].setCellValue(((tc2A.toDouble() / (tc2A.toDouble() + tc2F.toDouble())) * 100).toInt().toString() + "%")
            celdasMedia[7].setCellValue(formatearPromedioEstadistica(tc3A, parJug))
            celdasMedia[8].setCellValue(((tc3A.toDouble() / (tc3A.toDouble() + tc3F.toDouble())) * 100).toInt().toString() + "%")
            celdasMedia[9].setCellValue(formatearPromedioEstadistica(rebD, parJug))
            celdasMedia[10].setCellValue(formatearPromedioEstadistica(rebO, parJug))
            celdasMedia[11].setCellValue(String.format(Locale.US, "%.1f", ((rebO.toDouble() + rebD.toDouble()) / parJug)))
            celdasMedia[12].setCellValue(formatearPromedioEstadistica(asi, parJug))
            celdasMedia[13].setCellValue(formatearPromedioEstadistica(per, parJug))
            celdasMedia[14].setCellValue(formatearPromedioEstadistica(rec, parJug))
            celdasMedia[15].setCellValue(formatearPromedioEstadistica(tapC, parJug))
            celdasMedia[16].setCellValue(formatearPromedioEstadistica(falC, parJug))
            celdasMedia[17].setCellValue(formatearPromedioEstadistica(valoracion, parJug))

            val rowTotal = sheet.createRow(index + 2).apply { height = tamanoCelda.toShort() }
            val celdasTotal = List(titulos.size) { colIndex ->
                rowTotal.createCell(colIndex).apply {
                    cellStyle = if ((index + 1) % 2 == 0) (if (colIndex == 0) estilos["primera"] else estilos["celda"]) as XSSFCellStyle?
                    else (if (colIndex == 0) estilos["primeraImpar"] else estilos["celdaImpar"]) as XSSFCellStyle?
                }
            }.toMutableList()

            celdasTotal[0].setCellValue("TOTAL")
            celdasTotal[1].setCellValue(segundosATiempo(min))
            celdasTotal[2].setCellValue(pts.toString())
            celdasTotal[3].setCellValue(tlA.toString() + "/" + (tlA + tlF))
            celdasTotal[4].setCellValue(((tlA.toDouble() / (tlA.toDouble() + tlF.toDouble())) * 100).toInt().toString() + "%")
            celdasTotal[5].setCellValue(tc2A.toString() + "/" + (tc2A + tc2F))
            celdasTotal[6].setCellValue(((tc2A.toDouble() / (tc2A.toDouble() + tc2F.toDouble())) * 100).toInt().toString() + "%")
            celdasTotal[7].setCellValue(tc3A.toString() + "/" + (tc3A + tc3F))
            celdasTotal[8].setCellValue(((tc3A.toDouble() / (tc3A.toDouble() + tc3F.toDouble())) * 100).toInt().toString() + "%")
            celdasTotal[9].setCellValue(rebD.toString())
            celdasTotal[10].setCellValue(rebO.toString())
            celdasTotal[11].setCellValue((rebD + rebO).toString())
            celdasTotal[12].setCellValue(asi.toString())
            celdasTotal[13].setCellValue(per.toString())
            celdasTotal[14].setCellValue(rec.toString())
            celdasTotal[15].setCellValue(tapC.toString())
            celdasTotal[16].setCellValue(falC.toString())
            celdasTotal[17].setCellValue(valoracion.toString())

            guardarExcelEnDispositivo(workbook, context, nombreJugador)
        }
    }

    private fun crearEstilos(workbook: XSSFWorkbook): Map<String, CellStyle> {
        val estilos = mutableMapOf<String, CellStyle>()

        //  Estilo para encabezado Principal (Fondo Amarillo, negrita, texto blanco)
        val estiloEncabezadoPrincipal = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.YELLOW.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
            setFont(workbook.createFont().apply {
                bold = true
                color = IndexedColors.BLACK.index
            })
        }
        estilos["encabezadoPrincipal"] = estiloEncabezadoPrincipal

        //  Estilo para encabezados (Fondo Negro, negrita, texto blanco)
        val estiloEncabezado = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.BLACK.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
            setFont(workbook.createFont().apply {
                bold = true
                color = IndexedColors.WHITE.index
            })
        }
        estilos["encabezado"] = estiloEncabezado

        //  Estilo para encabezados (Fondo Negro, negrita, texto blanco)
        val estiloEncabezadoA = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.BLACK.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
            setFont(workbook.createFont().apply {
                bold = true
                color = IndexedColors.YELLOW.index
            })
        }
        estilos["encabezadoA"] = estiloEncabezadoA

        //  Estilo para celdas normales (Texto alineado al centro)
        val estiloCeldas = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.WHITE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
        }
        estilos["celda"] = estiloCeldas

        //  Estilo para celdas normales impares (Texto alineado al centro)
        val estiloCeldasImpar = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
        }
        estilos["celdaImpar"] = estiloCeldasImpar

        //  Estilo para celdas nombre (Texto alineado al Izquierda)
        val primeraCelda = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.WHITE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.LEFT
            verticalAlignment = VerticalAlignment.CENTER
            setFont(workbook.createFont().apply {
                bold = true
            })
        }
        estilos["primera"] = primeraCelda

        //  Estilo para celdas nombre impares (Texto alineado al Izquierda)
        val primeraCeldasImpar = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.LEFT
            verticalAlignment = VerticalAlignment.CENTER
            setFont(workbook.createFont().apply {
                bold = true
            })
        }
        estilos["primeraImpar"] = primeraCeldasImpar

        return estilos
    }

    private fun guardarExcelEnDispositivo(workbook: XSSFWorkbook, context: Context, jugador: String) {
        try {
            val nombreArchivo = "estadisticas-${jugador}-${LocalDate.now()}.xlsx"

            // Obtener la carpeta de Descargas
            val directorioDescargas = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val archivoExcel = File(directorioDescargas, nombreArchivo)

            val outputStream = FileOutputStream(archivoExcel)
            workbook.write(outputStream)
            outputStream.close()

            Toast.makeText(context, "Excel guardado en Descargas", Toast.LENGTH_SHORT).show()

            // Opcional: Escanear el archivo para que aparezca en el gestor de archivos
            MediaScannerConnection.scanFile(
                context, arrayOf(archivoExcel.absolutePath), arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), null
            )

            abrirExcel(archivoExcel, context)
        } catch (e: Exception) {
            Log.e("Excel", "Error al guardar Excel", e)
        }
    }

    private fun abrirExcel(file: File, context: Context) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(intent, "Abrir con"))
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir el archivo", Toast.LENGTH_SHORT).show()
            Log.e("Excel", "Error al abrir Excel", e)
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

    private fun formatearPromedioEstadistica(estadistica: Int, parJug: Int): String {
        return if (parJug > 0) {
            val promedio = estadistica.toFloat() / parJug
            if (promedio % 1 == 0f) promedio.toInt().toString()
            else String.format(Locale.US, "%.1f", promedio)
        } else {
            "0"
        }
    }

    private fun formatearPromedioMinutos(estadistica: Int, parJug: Int): String {
        return if (parJug > 0) {
            val promedio = estadistica.toFloat() / parJug / 60f
            if (promedio % 1 == 0f) promedio.toInt().toString()
            else String.format(Locale.US, "%.1f", promedio)
        } else {
            "0"
        }
    }

}