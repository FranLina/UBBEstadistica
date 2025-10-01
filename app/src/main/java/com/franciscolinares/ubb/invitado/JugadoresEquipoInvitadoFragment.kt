package com.franciscolinares.ubb.invitado

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.edit
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentJugadoresEquipoInvitadoBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale
import androidx.core.graphics.toColorInt
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.Date


class JugadoresEquipoInvitadoFragment : Fragment() {

    private var _binding: FragmentJugadoresEquipoInvitadoBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private var idEquipo: String? = null

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

        idEquipo = activity?.intent?.getStringExtra("idEquipo")
        binding.loadingOverlay.show()
        try {
            db.collection("Equipos").document(idEquipo.toString()).get().addOnSuccessListener {
                val plantilla: HashMap<String, String> = it.get("Jugadores") as HashMap<String, String>

                plantilla.entries.withIndex().forEach { (index, jugador) ->
                    val registro = LayoutInflater.from(binding.root.context).inflate(R.layout.row_jugador_equipo_invitado, null, false)

                    if (index % 2 != 0)
                        registro.findViewById<TableRow>(R.id.filaJEInvitado).setBackgroundColor("#FFE4E4E4".toColorInt())

                    db.collection("Jugadores").document(jugador.key).get().addOnSuccessListener { it2 ->

                        registro.findViewById<TextView>(R.id.txtFilaJEINombre).text =
                            it2["Apellido1"].toString() + " " + it2["Apellido2"].toString() + ", " + it2["Nombre"].toString()
                        if (it2.get("UrlFoto").toString() != "")
                            Glide.with(root.context).load(it2.get("UrlFoto").toString()).placeholder(R.drawable.jugador_de_baloncesto)
                                .error(R.drawable.jugador_de_baloncesto).centerCrop().override(100, 100)
                                .into(registro.findViewById(R.id.fotoJEInvitado))

                        registro.setOnClickListener {
                            val intent = Intent(binding.root.context, JugadorInvitadoActivity::class.java).apply {
                                putExtra("idJugador", jugador.key)
                            }
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
                                if (partido["Estado"].toString() == "Finalizado")
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

                                    registro.findViewById<TextView>(R.id.txtFilaJEIPart).text = "" + conPartidos
                                    registro.findViewById<TextView>(R.id.txtFilaJEIPJ).text = "" + parJug
                                    registro.findViewById<TextView>(R.id.txtFilaJEIMPP).text = formatearPromedioMinutos(minPar, parJug)
                                    registro.findViewById<TextView>(R.id.txtFilaJEIPPP).text = formatearPromedioEstadistica(punPar, parJug)
                                    registro.findViewById<TextView>(R.id.txtFilaJEIAPP).text = formatearPromedioEstadistica(asiPar, parJug)
                                    registro.findViewById<TextView>(R.id.txtFilaJEIRPP).text = formatearPromedioEstadistica(rebPar, parJug)
                                }

                            }
                        }
                    }
                    binding.TLJugadoresInvitado.addView(registro)

                }
            }
        } catch (_: Exception) {

        } finally {
            binding.loadingOverlay.hide()
        }

        binding.btnExcelPlantilla.setOnClickListener {
            exportarEstadisticasAPartirDeFirebase(idEquipo.toString(), binding.root.context)
        }

        return root
    }

    private fun exportarEstadisticasAPartirDeFirebase(equipo: String, context: Context) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Plantilla")
        val estilos = crearEstilos(workbook)
        val tamanoCelda = 400
        sheet.setColumnWidth(0, 5000)

        val titulos = listOf(
            "NOMBRE", "PART", "PJ.", "MIN.PP",
            "PTS.PP", "TL.PP", "T2.PP", "T3.PP", "ASI.PP", "REB.PP", "REC.PP",
            "PER.PP", "TAP.PP", "FAL.PP", "VAL.PP"
        )

        // Crear encabezados
        val filaEncabezado = sheet.createRow(0)
        filaEncabezado.height = 500
        titulos.forEachIndexed { index, titulo ->
            val celda = filaEncabezado.createCell(index)
            celda.setCellValue(titulo)
            celda.cellStyle = estilos["encabezadoA"] as XSSFCellStyle?
        }

        val query = db.collection("Partidos")
            .where(
                Filter.or(
                    Filter.equalTo("EquipoLocal", equipo),
                    Filter.equalTo("EquipoVisitante", equipo)
                )
            )

        db.collection("Equipos").document(equipo).get().addOnSuccessListener { equipoDoc ->
            val plantilla = equipoDoc.get("Jugadores") as? Map<String, String> ?: return@addOnSuccessListener

            val tareas = mutableListOf<Task<*>>()

            // Ordenar plantilla por dorsal
            val plantillaOrdenada = plantilla.entries.sortedBy {
                val dorsal = it.value.split(" ").firstOrNull()?.toIntOrNull() ?: Int.MAX_VALUE
                dorsal
            }

            plantillaOrdenada.withIndex().forEach { (index, jugador) ->
                val row = sheet.createRow(index + 1).apply { height = tamanoCelda.toShort() }
                val celdas = List(titulos.size) { colIndex ->
                    row.createCell(colIndex).apply {
                        cellStyle = if (index % 2 == 0)
                            (if (colIndex == 0) estilos["primera"] else estilos["celda"]) as XSSFCellStyle?
                        else
                            (if (colIndex == 0) estilos["primeraImpar"] else estilos["celdaImpar"]) as XSSFCellStyle?
                    }
                }

                val tarea = query.get().continueWithTask { partidosTask ->
                    val partidos = partidosTask.result ?: return@continueWithTask Tasks.forResult(null)
                    var totalPartidos = 0
                    var partidosJugados = 0
                    var totalMinutos = 0
                    var totalPuntos = 0
                    var totalTL = 0
                    var total2pts = 0
                    var total3pts = 0
                    var totalAsistencias = 0
                    var totalRebotes = 0
                    var totalRecuperaciones = 0
                    var totalPerdidas = 0
                    var totalTapones = 0
                    var totalFaltas = 0
                    var totalValoracion = 0
                    var nombreJugador = ""

                    val estadisticasTasks = partidos.documents.map { partido ->
                        if (partido["Estado"] == "Finalizado") {
                            totalPartidos++
                            db.collection("Estadisticas").document(partido.id).get().addOnSuccessListener { estadisticaDoc ->
                                val listado = estadisticaDoc.get("ListadoJugadores") as? List<String> ?: return@addOnSuccessListener
                                if (jugador.key in listado) {
                                    val datos = estadisticaDoc.get(jugador.key) as? Map<String, Any> ?: return@addOnSuccessListener
                                    if (datos["minutos"] != "00:00") {
                                        partidosJugados++
                                        totalMinutos += tiempoASegundos(datos["minutos"].toString())
                                        totalPuntos += (datos["puntos"] as? Long)?.toInt() ?: 0
                                        totalTL += (datos["tlA"] as? Long)?.toInt() ?: 0
                                        total2pts += (datos["tc2pA"] as? Long)?.toInt() ?: 0
                                        total3pts += (datos["tc3pA"] as? Long)?.toInt() ?: 0
                                        totalAsistencias += (datos["asi"] as? Long)?.toInt() ?: 0
                                        val rebO = (datos["rebO"] as? Long)?.toInt() ?: 0
                                        val rebD = (datos["rebD"] as? Long)?.toInt() ?: 0
                                        totalRebotes += rebO + rebD
                                        totalRecuperaciones += (datos["recu"] as? Long)?.toInt() ?: 0
                                        totalPerdidas += (datos["per"] as? Long)?.toInt() ?: 0
                                        totalTapones += (datos["taCom"] as? Long)?.toInt() ?: 0
                                        totalFaltas += (datos["falC"] as? Long)?.toInt() ?: 0
                                        totalValoracion += (datos["val"] as? Long)?.toInt() ?: 0
                                        nombreJugador = "${datos["dorsal"]} ${datos["nombre"]}"
                                    }
                                }
                            }
                        } else {
                            Tasks.forResult(null)
                        }
                    }
                    Tasks.whenAllComplete(estadisticasTasks).addOnSuccessListener {
                        celdas[0].setCellValue(nombreJugador.ifEmpty { "Desconocido" })
                        celdas[1].setCellValue(totalPartidos.toString())
                        celdas[2].setCellValue(partidosJugados.toString())
                        celdas[3].setCellValue(formatearPromedioMinutos(totalMinutos, partidosJugados))
                        celdas[4].setCellValue(formatearPromedioEstadistica(totalPuntos, partidosJugados))
                        celdas[5].setCellValue(formatearPromedioEstadistica(totalTL, partidosJugados))
                        celdas[6].setCellValue(formatearPromedioEstadistica(total2pts, partidosJugados))
                        celdas[7].setCellValue(formatearPromedioEstadistica(total3pts, partidosJugados))
                        celdas[8].setCellValue(formatearPromedioEstadistica(totalAsistencias, partidosJugados))
                        celdas[9].setCellValue(formatearPromedioEstadistica(totalRebotes, partidosJugados))
                        celdas[10].setCellValue(formatearPromedioEstadistica(totalRecuperaciones, partidosJugados))
                        celdas[11].setCellValue(formatearPromedioEstadistica(totalPerdidas, partidosJugados))
                        celdas[12].setCellValue(formatearPromedioEstadistica(totalTapones, partidosJugados))
                        celdas[13].setCellValue(formatearPromedioEstadistica(totalFaltas, partidosJugados))
                        celdas[14].setCellValue(formatearPromedioEstadistica(totalValoracion, partidosJugados))
                    }
                }
                tareas.add(tarea)
            }

            Tasks.whenAllComplete(tareas).addOnSuccessListener {
                // Guardar Excel después de todas las tareas
                guardarExcelEnDispositivo(workbook, context, equipo)
            }
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

    private fun guardarExcelEnDispositivo(workbook: XSSFWorkbook, context: Context, equipo: String) {
        try {
            val nombreArchivo = "plantilla-${equipo}-${LocalDate.now()}.xlsx"

            // Obtener la carpeta de Descargas
            val directorioDescargas = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val archivoExcel = File(directorioDescargas, nombreArchivo)

            val outputStream = FileOutputStream(archivoExcel)
            workbook.write(outputStream)
            outputStream.close()

            Toast.makeText(context, "Excel guardado en Descargas", Toast.LENGTH_SHORT).show()

            // Opcional: Escanear el archivo para que aparezca en el gestor de archivos
            MediaScannerConnection.scanFile(
                context,
                arrayOf(archivoExcel.absolutePath),
                arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                null
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

    private fun formatearPromedioEstadistica(estadistica: Int, parJug: Int): String {
        return if (parJug > 0) {
            val promedio = estadistica.toFloat() / parJug
            if (promedio % 1 == 0f)
                promedio.toInt().toString()
            else
                String.format(Locale.US, "%.1f", promedio)
        } else {
            "0"
        }
    }

    private fun formatearPromedioMinutos(estadistica: Int, parJug: Int): String {
        return if (parJug > 0) {
            val promedio = estadistica.toFloat() / parJug / 60f
            if (promedio % 1 == 0f)
                promedio.toInt().toString()
            else
                String.format(Locale.US, "%.1f", promedio)
        } else {
            "0"
        }
    }
}