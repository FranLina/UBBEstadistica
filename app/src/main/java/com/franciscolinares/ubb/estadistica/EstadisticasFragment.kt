package com.franciscolinares.ubb.estadistica

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentEstadisticasBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
import java.util.Locale

class EstadisticasFragment : Fragment() {

    private var _binding: FragmentEstadisticasBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private var i = 0
    private val handler = Handler()
    private var jugadores: ArrayList<Map<String?, Any?>> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEstadisticasBinding.inflate(inflater, container, false)
        val root = binding.root

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()

        recuperaInfo()

        binding.btnExcelLocal.setOnClickListener {
            exportarEstadisticasAPartirDeFirebase("Local", binding.root.context)
        }

        binding.btnExcelVisitante.setOnClickListener {
            exportarEstadisticasAPartirDeFirebase("Visitante", binding.root.context)
        }

        db.collection("Estadisticas").document(idPartido).get().addOnSuccessListener {
            val listJugador = it.get("ListadoJugadores") as ArrayList<String>

            for (j in 0..<listJugador.count()) {
                jugadores.add((it.get(listJugador[j]) as Map<String?, Any?>))
            }
            recuperaDatosEstadistica(jugadores)
        }

        i = binding.progressBar.progress

        Thread {
            while (i < 100) {
                i += 1
                handler.post {
                    binding.progressBar.progress = i
                }
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                if (i == 100) {
                    i = 0
                    db.collection("Partidos").document(idPartido).get().addOnSuccessListener { partido ->
                        if (partido.get("Estado") != "Finalizado") {
                            db.collection("Partidos").document(idPartido).get().addOnSuccessListener { documentSnapshot ->
                                binding.txtPuntosLocalPartido.text = (documentSnapshot.get("Resultado").toString().split(" - "))[0]
                                binding.txtPuntosVisitantePartido.text = (documentSnapshot.get("Resultado").toString().split(" - "))[1]
                            }

                            db.collection("Estadisticas").document(idPartido).get().addOnSuccessListener {
                                val listJugador = it.get("ListadoJugadores") as ArrayList<String>
                                val newJugadores: ArrayList<Map<String?, Any?>> = arrayListOf()
                                for (j in 0..<listJugador.count()) {
                                    newJugadores.add((it.get(listJugador[j]) as Map<String?, Any?>))
                                }
                                actualizaDatosEstadistica(newJugadores)
                            }

                        }
                    }
                }
            }
        }.start()

        return root
    }

    private fun exportarEstadisticasAPartirDeFirebase(equipo: String, context: Context) {

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Estadísticas")
        val estilos = crearEstilos(workbook)

        val tamanoCelda = 400

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
        var tapR = 0
        var falC = 0
        var falR = 0
        var valoracion = 0
        jugadores.clear()

        // Crear encabezados
        val filaEncabezado = sheet.createRow(0)
        val titulos = listOf(
            "", "", "TC 2P", "", "TC 3P", "", "TL", "", "", "REB", "", "", "", "", "TAP", "", "FAL", "", ""
        )
        for ((cel, titulo) in titulos.withIndex()) {
            val celda = filaEncabezado.createCell(cel)
            filaEncabezado.height = 500
            celda.setCellValue(titulo)
            celda.cellStyle = estilos["encabezadoPrincipal"] as XSSFCellStyle?
        }
        sheet.addMergedRegion(CellRangeAddress(0, 0, 2, 3))
        sheet.addMergedRegion(CellRangeAddress(0, 0, 4, 5))
        sheet.addMergedRegion(CellRangeAddress(0, 0, 6, 7))
        sheet.addMergedRegion(CellRangeAddress(0, 0, 14, 15))
        sheet.addMergedRegion(CellRangeAddress(0, 0, 16, 17))

        // Crear encabezados
        val filaEncabezado2 = sheet.createRow(1)
        val titulos2 = listOf(
            "Nombre", "PTS", "A/I", "%", "A/I", "%", "A/I", "%", "Def.", "Ofe.", "Tot.", "AS", "REC", "PÉR", "Com.", "Rec.", "Com.", "Rec.", "VAL"
        )
        for ((cel, titulo2) in titulos2.withIndex()) {
            val celda = filaEncabezado2.createCell(cel)
            filaEncabezado2.height = 500
            celda.setCellValue(titulo2)
            if (cel == 0 || cel == 1 || cel == 11 || cel == 12 || cel == 13 || cel == 18) celda.cellStyle = estilos["encabezadoA"] as XSSFCellStyle?
            else celda.cellStyle = estilos["encabezado"] as XSSFCellStyle?
        }

        var rowIndex = 2

        db.collection("Estadisticas").document(idPartido).get().addOnSuccessListener { it ->
            val listJugador = it.get("ListadoJugadores") as ArrayList<String>

            for (j in 0..<listJugador.count()) {
                jugadores.add((it.get(listJugador[j]) as Map<String?, Any?>))
            }

            val jugadoresOrdenados = jugadores.sortedBy { (it["dorsal"] as? String)?.toIntOrNull() ?: Int.MAX_VALUE }

            // Recorrer jugadores y obtener estadísticas
            for (jugador in jugadoresOrdenados) {
                if (jugador["equipo"].toString() == equipo) {
                    pts += jugador["puntos"].toString().toInt()
                    tc2A += jugador["tc2pA"].toString().toInt()
                    tc2F += jugador["tc2pF"].toString().toInt()
                    tc3A += jugador["tc3pA"].toString().toInt()
                    tc3F += jugador["tc3pF"].toString().toInt()
                    tlA += jugador["tlA"].toString().toInt()
                    tlF += jugador["tlF"].toString().toInt()
                    rebD += jugador["rebD"].toString().toInt()
                    rebO += jugador["rebO"].toString().toInt()
                    asi += jugador["asi"].toString().toInt()
                    rec += jugador["recu"].toString().toInt()
                    per += jugador["per"].toString().toInt()
                    tapC += jugador["taCom"].toString().toInt()
                    tapR += jugador["taRec"].toString().toInt()
                    falC += jugador["falC"].toString().toInt()
                    falR += jugador["falR"].toString().toInt()
                    valoracion += jugador["val"].toString().toInt()

                    // Escribir fila de estadísticas
                    val row = sheet.createRow(rowIndex++)
                    row.height = tamanoCelda.toShort()
                    val celdas = ArrayList<Cell>()
                    for (cel in 0..18) {
                        val celda = row.createCell(cel)
                        if (rowIndex % 2 == 0) celda.cellStyle = estilos["celda"] as XSSFCellStyle?
                        else celda.cellStyle = estilos["celdaImpar"] as XSSFCellStyle?
                        celdas.add(celda)
                    }

                    if (rowIndex % 2 == 0) celdas[0].cellStyle = estilos["primera"] as XSSFCellStyle?
                    else celdas[0].cellStyle = estilos["primeraImpar"] as XSSFCellStyle?

                    celdas[0].setCellValue(jugador["dorsal"].toString() + " " + jugador["nombre"].toString())
                    celdas[1].setCellValue(jugador["puntos"].toString())
                    celdas[2].setCellValue(
                        jugador["tc2pA"].toString() + "/" + (jugador["tc2pA"].toString().toInt() + jugador["tc2pF"].toString().toInt())
                    )
                    celdas[3].setCellValue(
                        ((jugador["tc2pA"].toString().toDouble() / (jugador["tc2pA"].toString().toDouble() + jugador["tc2pF"].toString()
                            .toDouble())) * 100).toInt().toString()
                    )
                    celdas[4].setCellValue(
                        jugador["tc3pA"].toString() + "/" + (jugador["tc3pA"].toString().toInt() + jugador["tc3pF"].toString().toInt())
                    )
                    celdas[5].setCellValue(
                        ((jugador["tc3pA"].toString().toDouble() / (jugador["tc3pA"].toString().toDouble() + jugador["tc3pF"].toString()
                            .toDouble())) * 100).toInt().toString()
                    )
                    celdas[6].setCellValue(
                        jugador["tlA"].toString() + "/" + (jugador["tlA"].toString().toInt() + jugador["tlF"].toString().toInt())
                    )
                    celdas[7].setCellValue(
                        ((jugador["tlA"].toString().toDouble() / (jugador["tlA"].toString().toDouble() + jugador["tlF"].toString()
                            .toDouble())) * 100).toInt().toString()
                    )
                    celdas[8].setCellValue(jugador["rebD"].toString())
                    celdas[9].setCellValue(jugador["rebO"].toString())
                    celdas[10].setCellValue((jugador["rebD"].toString().toInt() + jugador["rebO"].toString().toInt()).toString())
                    celdas[11].setCellValue(jugador["asi"].toString())
                    celdas[12].setCellValue(jugador["recu"].toString())
                    celdas[13].setCellValue(jugador["per"].toString())
                    celdas[14].setCellValue(jugador["taCom"].toString())
                    celdas[15].setCellValue(jugador["taRec"].toString())
                    celdas[16].setCellValue(jugador["falC"].toString())
                    celdas[17].setCellValue(jugador["falR"].toString())
                    celdas[18].setCellValue(jugador["val"].toString())
                    celdas.clear()
                }
            }

            // Escribir fila de estadísticas Total
            val row = sheet.createRow(rowIndex++)
            row.height = 500
            val celdas = ArrayList<Cell>()
            for (cel in 0..18) {
                val celda = row.createCell(cel)
                if (cel == 0 || cel == 1 || cel == 11 || cel == 12 || cel == 13 || cel == 18) celda.cellStyle =
                    estilos["encabezadoA"] as XSSFCellStyle?
                else celda.cellStyle = estilos["encabezado"] as XSSFCellStyle?
                celdas.add(celda)
            }

            celdas[0].setCellValue("Totales")
            celdas[1].setCellValue(pts.toString())
            celdas[2].setCellValue(tc2A.toString() + "/" + (tc2A + tc2F))
            celdas[3].setCellValue(((tc2A.toDouble() / (tc2A.toDouble() + tc2F.toDouble())) * 100).toInt().toString())
            celdas[4].setCellValue(tc3A.toString() + "/" + (tc3A + tc3F))
            celdas[5].setCellValue(((tc3A.toDouble() / (tc3A.toDouble() + tc3F.toDouble())) * 100).toInt().toString())
            celdas[6].setCellValue(tlA.toString() + "/" + (tlA + tlF))
            celdas[7].setCellValue(((tlA.toDouble() / (tlA.toDouble() + tlF.toDouble())) * 100).toInt().toString())
            celdas[8].setCellValue(rebD.toString())
            celdas[9].setCellValue(rebO.toString())
            celdas[10].setCellValue((rebD + rebO).toString())
            celdas[11].setCellValue(asi.toString())
            celdas[12].setCellValue(rec.toString())
            celdas[13].setCellValue(per.toString())
            celdas[14].setCellValue(tapC.toString())
            celdas[15].setCellValue(tapR.toString())
            celdas[16].setCellValue(falC.toString())
            celdas[17].setCellValue(falR.toString())
            celdas[18].setCellValue(valoracion.toString())
            celdas.clear()

            // Guardar archivo Excel
            guardarExcelEnDispositivo(workbook, context, equipo)
        }
        sheet.setColumnWidth(0, 5000)
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
            val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
            val idPartido = prefs.getString("idPartido", "").toString()

            db.collection("Partidos").document(idPartido).get().addOnSuccessListener {
                val nombreArchivo =
                    "estadisticas_" + it.get("EquipoLocal") + "_" + it.get("EquipoVisitante") + "_" + it.get("Fecha").toString().replace("/", "-") + "_" + equipo + ".xlsx"

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
            }
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

    @SuppressLint("SetTextI18n")
    private fun recuperaInfo() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()

        db.collection("Partidos").document(idPartido).get().addOnSuccessListener {
            binding.txtNombreELocal.text = "  " + it.get("EquipoLocal").toString().toUpperCase(
                Locale.ROOT
            )
            binding.txtNombreEVisitante.text = "  " + it.get("EquipoVisitante").toString().toUpperCase(Locale.ROOT)
            binding.txtNombreELocalEsta.text = it.get("EquipoLocal").toString().toUpperCase(Locale.ROOT)
            binding.txtNombreEVisitanteEsta.text = it.get("EquipoVisitante").toString().toUpperCase(Locale.ROOT)
            binding.txtPuntosLocalPartido.text = (it.get("Resultado").toString().split(" - "))[0]
            binding.txtPuntosVisitantePartido.text = (it.get("Resultado").toString().split(" - "))[1]
            cargaEscudos(it.get("EquipoLocal").toString(), it.get("EquipoVisitante").toString())
        }
    }

    @SuppressLint("SetTextI18n", "InflateParams", "ResourceAsColor", "MissingInflatedId")
    private fun recuperaDatosEstadistica(
        jugadores: ArrayList<Map<String?, Any?>>
    ) {
        var ptsL = 0
        var tc2AL = 0
        var tc2FL = 0
        var tc3AL = 0
        var tc3FL = 0
        var tlAL = 0
        var tlFL = 0
        var rebOL = 0
        var rebDL = 0
        var asiL = 0
        var recL = 0
        var perL = 0
        var tapCL = 0
        var tapRL = 0
        var falCL = 0
        var falRL = 0
        var valL = 0

        var ptsV = 0
        var tc2AV = 0
        var tc2FV = 0
        var tc3AV = 0
        var tc3FV = 0
        var tlAV = 0
        var tlFV = 0
        var rebOV = 0
        var rebDV = 0
        var asiV = 0
        var recV = 0
        var perV = 0
        var tapCV = 0
        var tapRV = 0
        var falCV = 0
        var falRV = 0
        var valV = 0

        binding.TLLocal.removeAllViews()
        binding.TLLocalNombre.removeAllViews()
        binding.TLVisitante.removeAllViews()
        binding.TLVisitanteNombre.removeAllViews()

        val cabeceraLPN = LayoutInflater.from(binding.root.context).inflate(R.layout.row_cabecera_principal_nombre, null, false)
        val cabeceraLN = LayoutInflater.from(binding.root.context).inflate(R.layout.row_cabecera_nombre, null, false)
        binding.TLLocalNombre.addView(cabeceraLPN)
        binding.TLLocalNombre.addView(cabeceraLN)

        val cabeceraLP = LayoutInflater.from(binding.root.context).inflate(R.layout.row_cabecera_principal, null, false)
        val cabeceraL = LayoutInflater.from(binding.root.context).inflate(R.layout.row_cabecera, null, false)
        binding.TLLocal.addView(cabeceraLP)
        binding.TLLocal.addView(cabeceraL)

        val cabeceraVPN = LayoutInflater.from(binding.root.context).inflate(R.layout.row_cabecera_principal_nombre, null, false)
        val cabeceraVN = LayoutInflater.from(binding.root.context).inflate(R.layout.row_cabecera_nombre, null, false)
        binding.TLVisitanteNombre.addView(cabeceraVPN)
        binding.TLVisitanteNombre.addView(cabeceraVN)

        val cabeceraVP = LayoutInflater.from(binding.root.context).inflate(R.layout.row_cabecera_principal, null, false)
        val cabeceraV = LayoutInflater.from(binding.root.context).inflate(R.layout.row_cabecera, null, false)
        binding.TLVisitante.addView(cabeceraVP)
        binding.TLVisitante.addView(cabeceraV)

        val jugadoresOrdenados = jugadores.sortedWith(compareBy<Map<String?, Any?>> { it["equipo"] as? String }.thenBy {
            (it["dorsal"] as? String)?.toIntOrNull() ?: Int.MAX_VALUE
        })

        for (j in 0..<jugadoresOrdenados.count()) {

            val jugador = jugadoresOrdenados[j]

            val registro = LayoutInflater.from(binding.root.context).inflate(R.layout.row_estadistica, null, false)

            val registroN = LayoutInflater.from(binding.root.context).inflate(R.layout.row_estadistica_nombre, null, false)

            if (j % 2 != 0) {
                registro.findViewById<TableRow>(R.id.columnaJugador).setBackgroundColor(
                    Color.parseColor("#FFE4E4E4")
                )
                registroN.findViewById<TableRow>(R.id.columnaJugador).setBackgroundColor(
                    Color.parseColor("#FFE4E4E4")
                )
            }

            registroN.findViewById<TextView>(R.id.txtENombre).text =
                if ((jugador["dorsal"].toString() + " " + jugador["nombre"].toString()).length > 19) {
                    "${
                        " " + (jugador["dorsal"].toString() + " " + jugador["nombre"].toString().toUpperCase()).substring(
                            0, 16
                        )
                    }..."
                } else {
                    " " + jugador["dorsal"].toString() + " " + jugador["nombre"].toString().toUpperCase()
                }
            registroN.tag = jugador["dorsal"].toString()
            registro.tag = jugador["dorsal"].toString()

            registro.findViewById<TextView>(R.id.txtEPuntos).text = jugador["puntos"].toString()
            registro.findViewById<TextView>(R.id.txtETC2P1).text =
                jugador["tc2pA"].toString() + "/" + (jugador["tc2pA"].toString().toInt() + jugador["tc2pF"].toString().toInt())
            registro.findViewById<TextView>(R.id.txtETC2P2).text =
                ((jugador["tc2pA"].toString().toDouble() / (jugador["tc2pA"].toString().toDouble() + jugador["tc2pF"].toString()
                    .toDouble())) * 100).toInt().toString()
            registro.findViewById<TextView>(R.id.txtETC3P1).text =
                jugador["tc3pA"].toString() + "/" + (jugador["tc3pA"].toString().toInt() + jugador["tc3pF"].toString().toInt())
            registro.findViewById<TextView>(R.id.txtETC3P2).text =
                ((jugador["tc3pA"].toString().toDouble() / (jugador["tc3pA"].toString().toDouble() + jugador["tc3pF"].toString()
                    .toDouble())) * 100).toInt().toString()
            registro.findViewById<TextView>(R.id.txtETL1).text =
                jugador["tlA"].toString() + "/" + (jugador["tlA"].toString().toInt() + jugador["tlF"].toString().toInt())
            registro.findViewById<TextView>(R.id.txtETL2).text =
                ((jugador["tlA"].toString().toDouble() / (jugador["tlA"].toString().toDouble() + jugador["tlF"].toString().toDouble())) * 100).toInt()
                    .toString()
            registro.findViewById<TextView>(R.id.txtERebO).text = jugador["rebO"].toString()
            registro.findViewById<TextView>(R.id.txtERebD).text = jugador["rebD"].toString()
            registro.findViewById<TextView>(R.id.txtERebT).text = (jugador["rebD"].toString().toInt() + jugador["rebO"].toString().toInt()).toString()
            registro.findViewById<TextView>(R.id.txtEAsi).text = jugador["asi"].toString()
            registro.findViewById<TextView>(R.id.txtERec).text = jugador["recu"].toString()
            registro.findViewById<TextView>(R.id.txtEPer).text = jugador["per"].toString()
            registro.findViewById<TextView>(R.id.txtETapC).text = jugador["taCom"].toString()
            registro.findViewById<TextView>(R.id.txtETapR).text = jugador["taRec"].toString()
            registro.findViewById<TextView>(R.id.txtEFalC).text = jugador["falC"].toString()
            registro.findViewById<TextView>(R.id.txtEFalR).text = jugador["falR"].toString()
            registro.findViewById<TextView>(R.id.txtEVal).text = jugador["val"].toString()

            if (jugador["equipo"] == "Local") {
                binding.TLLocal.addView(registro)
                binding.TLLocalNombre.addView(registroN)

                ptsL += jugador["puntos"].toString().toInt()
                tc2AL += jugador["tc2pA"].toString().toInt()
                tc2FL += jugador["tc2pF"].toString().toInt()
                tc3AL += jugador["tc3pA"].toString().toInt()
                tc3FL += jugador["tc3pF"].toString().toInt()
                tlAL += jugador["tlA"].toString().toInt()
                tlFL += jugador["tlF"].toString().toInt()
                rebDL += jugador["rebD"].toString().toInt()
                rebOL += jugador["rebO"].toString().toInt()
                asiL += jugador["asi"].toString().toInt()
                recL += jugador["recu"].toString().toInt()
                perL += jugador["per"].toString().toInt()
                tapCL += jugador["taCom"].toString().toInt()
                tapRL += jugador["taRec"].toString().toInt()
                falCL += jugador["falC"].toString().toInt()
                falRL += jugador["falR"].toString().toInt()
                valL += jugador["val"].toString().toInt()
            } else {
                binding.TLVisitante.addView(registro)
                binding.TLVisitanteNombre.addView(registroN)

                ptsV += jugador["puntos"].toString().toInt()
                tc2AV += jugador["tc2pA"].toString().toInt()
                tc2FV += jugador["tc2pF"].toString().toInt()
                tc3AV += jugador["tc3pA"].toString().toInt()
                tc3FV += jugador["tc3pF"].toString().toInt()
                tlAV += jugador["tlA"].toString().toInt()
                tlFV += jugador["tlF"].toString().toInt()
                rebDV += jugador["rebD"].toString().toInt()
                rebOV += jugador["rebO"].toString().toInt()
                asiV += jugador["asi"].toString().toInt()
                recV += jugador["recu"].toString().toInt()
                perV += jugador["per"].toString().toInt()
                tapCV += jugador["taCom"].toString().toInt()
                tapRV += jugador["taRec"].toString().toInt()
                falCV += jugador["falC"].toString().toInt()
                falRV += jugador["falR"].toString().toInt()
                valV += jugador["val"].toString().toInt()
            }
        }

        val registroNTotalL = LayoutInflater.from(binding.root.context).inflate(R.layout.row_estadistica_total, null, false)
        binding.TLLocalNombre.addView(registroNTotalL)

        val registroL = LayoutInflater.from(binding.root.context).inflate(R.layout.row_estadistica_total_datos, null, false)
        registroL.findViewById<TextView>(R.id.txtEPuntos).text = ptsL.toString()
        registroL.findViewById<TextView>(R.id.txtETC2P1).text = tc2AL.toString() + "/" + (tc2AL + tc2FL).toString()
        registroL.findViewById<TextView>(R.id.txtETC2P2).text = ((tc2AL.toDouble() / (tc2AL.toDouble() + tc2FL.toDouble())) * 100).toInt().toString()
        registroL.findViewById<TextView>(R.id.txtETC3P1).text = tc3AL.toString() + "/" + (tc3AL + tc3FL).toString()
        registroL.findViewById<TextView>(R.id.txtETC3P2).text = ((tc3AL.toDouble() / (tc3AL.toDouble() + tc3FL.toDouble())) * 100).toInt().toString()
        registroL.findViewById<TextView>(R.id.txtETL1).text = tlAL.toString() + "/" + (tlAL + tlFL).toString()
        registroL.findViewById<TextView>(R.id.txtETL2).text = ((tlAL.toDouble() / (tlAL.toDouble() + tlFL.toDouble())) * 100).toInt().toString()
        registroL.findViewById<TextView>(R.id.txtERebO).text = rebOL.toString()
        registroL.findViewById<TextView>(R.id.txtERebD).text = rebDL.toString()
        registroL.findViewById<TextView>(R.id.txtERebT).text = (rebOL + rebDL).toString()
        registroL.findViewById<TextView>(R.id.txtEAsi).text = asiL.toString()
        registroL.findViewById<TextView>(R.id.txtERec).text = recL.toString()
        registroL.findViewById<TextView>(R.id.txtEPer).text = perL.toString()
        registroL.findViewById<TextView>(R.id.txtETapC).text = tapCL.toString()
        registroL.findViewById<TextView>(R.id.txtETapR).text = tapRL.toString()
        registroL.findViewById<TextView>(R.id.txtEFalC).text = falCL.toString()
        registroL.findViewById<TextView>(R.id.txtEFalR).text = falRL.toString()
        registroL.findViewById<TextView>(R.id.txtEVal).text = valL.toString()
        binding.TLLocal.addView(registroL)


        val registroNTotalV = LayoutInflater.from(binding.root.context).inflate(R.layout.row_estadistica_total, null, false)
        binding.TLVisitanteNombre.addView(registroNTotalV)

        val registroV = LayoutInflater.from(binding.root.context).inflate(R.layout.row_estadistica_total_datos, null, false)
        registroV.findViewById<TextView>(R.id.txtEPuntos).text = ptsV.toString()
        registroV.findViewById<TextView>(R.id.txtETC2P1).text = tc2AV.toString() + "/" + (tc2AV + tc2FV).toString()
        registroV.findViewById<TextView>(R.id.txtETC2P2).text = ((tc2AV.toDouble() / (tc2AV.toDouble() + tc2FV.toDouble())) * 100).toInt().toString()
        registroV.findViewById<TextView>(R.id.txtETC3P1).text = tc3AV.toString() + "/" + (tc3AV + tc3FV).toString()
        registroV.findViewById<TextView>(R.id.txtETC3P2).text = ((tc3AV.toDouble() / (tc3AV.toDouble() + tc3FV.toDouble())) * 100).toInt().toString()
        registroV.findViewById<TextView>(R.id.txtETL1).text = tlAV.toString() + "/" + (tlAV + tlFV).toString()
        registroV.findViewById<TextView>(R.id.txtETL2).text = ((tlAV.toDouble() / (tlAV.toDouble() + tlFV.toDouble())) * 100).toInt().toString()
        registroV.findViewById<TextView>(R.id.txtERebO).text = rebOV.toString()
        registroV.findViewById<TextView>(R.id.txtERebD).text = rebDV.toString()
        registroV.findViewById<TextView>(R.id.txtERebT).text = (rebOV + rebDV).toString()
        registroV.findViewById<TextView>(R.id.txtEAsi).text = asiV.toString()
        registroV.findViewById<TextView>(R.id.txtERec).text = recV.toString()
        registroV.findViewById<TextView>(R.id.txtEPer).text = perV.toString()
        registroV.findViewById<TextView>(R.id.txtETapC).text = tapCV.toString()
        registroV.findViewById<TextView>(R.id.txtETapR).text = tapRV.toString()
        registroV.findViewById<TextView>(R.id.txtEFalC).text = falCV.toString()
        registroV.findViewById<TextView>(R.id.txtEFalR).text = falRV.toString()
        registroV.findViewById<TextView>(R.id.txtEVal).text = valV.toString()
        binding.TLVisitante.addView(registroV)

        mostrarEstadisticaJugador(binding.TLLocal, "Local")
        mostrarEstadisticaJugador(binding.TLLocalNombre, "Local")
        mostrarEstadisticaJugador(binding.TLVisitante, "Visitante")
        mostrarEstadisticaJugador(binding.TLVisitanteNombre, "Visitante")

    }

    @SuppressLint("SetTextI18n")
    private fun mostrarEstadisticaJugador(tableLayout: TableLayout, equipo: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()

        for (i in 0 until tableLayout.childCount) {
            if (i != 0 && i != 1 && i != tableLayout.childCount - 1) {
                val rowView = tableLayout.getChildAt(i)
                rowView.setOnClickListener {
                    db.collection("Estadisticas").document(idPartido).get().addOnSuccessListener { esta ->
                        val listJugador = esta.get("ListadoJugadores") as ArrayList<String>
                        for (j in 0..<listJugador.count()) {
                            val jugador = esta.get(listJugador[j]) as Map<String?, Any?>
                            if (jugador["dorsal"].toString() == rowView.tag.toString() && jugador["equipo"] == equipo) {
                                val builder = AlertDialog.Builder(binding.root.context)
                                val view = layoutInflater.inflate(R.layout.estadistica_jugador_item, null)

                                db.collection("Jugadores").document(listJugador[j]).get().addOnSuccessListener { p ->
                                    if (p.get("UrlFoto") != "") {
                                        Picasso.get().load(p.get("UrlFoto").toString()).placeholder(R.drawable.jugador_blanco)
                                            .error(R.drawable.jugador_blanco).into(view.findViewById<ImageView>(R.id.imgMVPJugador))
                                    }
                                }

                                db.collection("Partidos").document(idPartido).get().addOnSuccessListener { partido ->
                                    val e: String = if (equipo == "Local") partido.get("EquipoLocal").toString()
                                    else partido.get("EquipoVisitante").toString()

                                    db.collection("Equipos").document(e).get().addOnSuccessListener {
                                        if (it.get("UrlFoto") != "") {
                                            Picasso.get().load(it.get("UrlFoto").toString()).placeholder(R.drawable.escudo_equipo)
                                                .error(R.drawable.escudo_equipo).into(view.findViewById<ImageView>(R.id.imgMVPEquipo))
                                        }
                                    }
                                }

                                view.findViewById<TextView>(R.id.txtMVPNombre2).text = jugador["nombre"].toString().toUpperCase(Locale.ROOT)
                                view.findViewById<TextView>(R.id.txtMVPDorsal).text = jugador["dorsal"].toString()
                                view.findViewById<TextView>(R.id.txtMVPPuntos).text = jugador["puntos"].toString()
                                view.findViewById<TextView>(R.id.txtMVPRebotes).text =
                                    (jugador["rebO"].toString().toInt() + jugador["rebD"].toString().toInt()).toString()
                                view.findViewById<TextView>(R.id.txtMVPAsistencias).text = jugador["asi"].toString()
                                view.findViewById<TextView>(R.id.txtMVPFaltas).text = jugador["falC"].toString()
                                view.findViewById<TextView>(R.id.txtMVPRecuperaciones).text = jugador["recu"].toString()
                                view.findViewById<TextView>(R.id.txtMVPPerdidas).text = jugador["per"].toString()
                                view.findViewById<TextView>(R.id.txtMVPTapones).text = jugador["taCom"].toString()
                                view.findViewById<TextView>(R.id.txtMVPValoracion).text = jugador["val"].toString()

                                builder.setView(view)
                                val dialog = builder.create()
                                dialog.show()
                            }
                        }
                    }
                }
            }

        }
    }

    private fun actualizaDatosEstadistica(
        newJugadores: ArrayList<Map<String?, Any?>>
    ) {
        // Actualizar los datos subyacentes
        jugadores.clear()
        jugadores.addAll(newJugadores)

        // Llamar a recuperaDatosEstadistica para reflejar los cambios en la UI
        recuperaDatosEstadistica(jugadores)
    }

    private fun cargaEscudos(eLocal: String, eVisitante: String) {

        db.collection("Equipos").document(eLocal).get().addOnSuccessListener {
            if (it.get("UrlFoto") != "") {
                Picasso.get().load(it.get("UrlFoto").toString()).placeholder(R.drawable.escudo_equipo).error(R.drawable.escudo_equipo)
                    .into(binding.imageLocalPartido)
                Picasso.get().load(it.get("UrlFoto").toString()).placeholder(R.drawable.escudo_equipo).error(R.drawable.escudo_equipo)
                    .into(binding.imageLocalPartidoEsta)
            }
        }.addOnFailureListener { exception ->
            Log.w(ContentValues.TAG, "Error getting documents.", exception)
        }

        db.collection("Equipos").document(eVisitante).get().addOnSuccessListener {
            if (it.get("UrlFoto") != "") {
                Picasso.get().load(it.get("UrlFoto").toString()).placeholder(R.drawable.escudo_equipo).error(R.drawable.escudo_equipo)
                    .into(binding.imageVisitantePartido)
                Picasso.get().load(it.get("UrlFoto").toString()).placeholder(R.drawable.escudo_equipo).error(R.drawable.escudo_equipo)
                    .into(binding.imageVisitantePartidoEsta)
            }
        }.addOnFailureListener { exception ->
            Log.w(ContentValues.TAG, "Error getting documents.", exception)
        }
    }
}