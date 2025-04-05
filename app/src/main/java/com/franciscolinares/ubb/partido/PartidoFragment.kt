package com.franciscolinares.ubb.partido

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.graphics.Color
import android.graphics.RectF
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RadioButton
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentPartidoBinding
import com.franciscolinares.ubb.estadistica.ListViewEstadistica.AdaptadorMinuto
import com.franciscolinares.ubb.estadistica.ListViewEstadistica.MinutoAMinuto
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

class PartidoFragment : Fragment() {

    private var _binding: FragmentPartidoBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore

    private var tiempo = "10:00"
    private var resultado = "0 - 0"
    private var cuarto = 1
    private var falL = 0
    private var falV = 0
    private var tmL = 2
    private var tmV = 2
    private var quintetoL: ArrayList<String> = arrayListOf<String>()
    private var quintetoV: ArrayList<String> = arrayListOf<String>()
    private var estado = ""
    private var isPlay = false
    private var pauseOffSet: Long = 10 * 60 * 1000
    private var pauseOffSetProrroga: Long = 5 * 60 * 1000

    companion object {
        fun newInstance() = PartidoFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n", "CutPasteId", "ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPartidoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()
        val equipoLocal = prefs.getString("EquipoLocal", "")
        val equipoVisitante = prefs.getString("EquipoVisitante", "")

        binding.TiempoCuarto.isCountDown = true

        db.collection("MinutoaMinuto").document(idPartido).get()
            .addOnSuccessListener {
                val registro = it.get("registro") as ArrayList<Map<String?, Any?>>
                if (registro.size > 10) {
                    recuperaInfo()
                } else {
                    binding.TiempoCuarto.text = tiempo
                    binding.TiempoCuarto.base = SystemClock.elapsedRealtime() + pauseOffSet
                    val listRegistro = ArrayList<Map<String?, Any?>>()
                    db.collection("MinutoaMinuto")
                        .document(idPartido)
                        .set(
                            hashMapOf(
                                "registro" to listRegistro,
                            ) as Map<String, Any>
                        )

                    quinteto("Visitante")
                    quinteto("Local")
                }
            }

        cargaEscudos(equipoLocal.toString(), equipoVisitante.toString())

        //Cambios Local
        binding.TBLocal1.setOnLongClickListener(View.OnLongClickListener {
            hacerCambiosLocal(llenarListToggleLocal(), binding.TBLocal1)
            return@OnLongClickListener true
        })
        binding.TBLocal2.setOnLongClickListener(View.OnLongClickListener {
            hacerCambiosLocal(llenarListToggleLocal(), binding.TBLocal2)
            return@OnLongClickListener true
        })
        binding.TBLocal3.setOnLongClickListener(View.OnLongClickListener {
            hacerCambiosLocal(llenarListToggleLocal(), binding.TBLocal3)
            return@OnLongClickListener true
        })
        binding.TBLocal4.setOnLongClickListener(View.OnLongClickListener {
            hacerCambiosLocal(llenarListToggleLocal(), binding.TBLocal4)
            return@OnLongClickListener true
        })
        binding.TBLocal5.setOnLongClickListener(View.OnLongClickListener {
            hacerCambiosLocal(llenarListToggleLocal(), binding.TBLocal5)
            return@OnLongClickListener true
        })

        //Cambios Visitante
        binding.TBVisitante1.setOnLongClickListener(View.OnLongClickListener {
            hacerCambiosVisitante(llenarListToggleVisitante(), binding.TBVisitante1)
            return@OnLongClickListener true
        })
        binding.TBVisitante2.setOnLongClickListener(View.OnLongClickListener {
            hacerCambiosVisitante(llenarListToggleVisitante(), binding.TBVisitante2)
            return@OnLongClickListener true
        })
        binding.TBVisitante3.setOnLongClickListener(View.OnLongClickListener {
            hacerCambiosVisitante(llenarListToggleVisitante(), binding.TBVisitante3)
            return@OnLongClickListener true
        })
        binding.TBVisitante4.setOnLongClickListener(View.OnLongClickListener {
            hacerCambiosVisitante(llenarListToggleVisitante(), binding.TBVisitante4)
            return@OnLongClickListener true
        })
        binding.TBVisitante5.setOnLongClickListener(View.OnLongClickListener {
            hacerCambiosVisitante(llenarListToggleVisitante(), binding.TBVisitante5)
            return@OnLongClickListener true
        })

        //Cronometro
        binding.TiempoCuarto.setOnChronometerTickListener {
            gestionCrononometro(it)
        }
        binding.TiempoCuarto.setOnClickListener {
            play()
        }

        //Pulsar boton Local
        binding.TBLocal1.setOnCheckedChangeListener { _, _ ->
            if (binding.TBLocal1.isChecked && binding.TBLocal1.text != " ") {
                vaciarToggle(llenarListToggle())
                binding.TBLocal1.isChecked = true
                binding.TBLocal1.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonlocalactivado))
            } else {
                binding.TBLocal1.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonlocaldesactivado))
            }
        }
        binding.TBLocal2.setOnCheckedChangeListener { _, _ ->
            if (binding.TBLocal2.isChecked && binding.TBLocal2.text != " ") {
                vaciarToggle(llenarListToggle())
                binding.TBLocal2.isChecked = true
                binding.TBLocal2.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonlocalactivado))
            } else {
                binding.TBLocal2.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonlocaldesactivado))
            }
        }
        binding.TBLocal3.setOnCheckedChangeListener { _, _ ->
            if (binding.TBLocal3.isChecked && binding.TBLocal3.text != " ") {
                vaciarToggle(llenarListToggle())
                binding.TBLocal3.isChecked = true
                binding.TBLocal3.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonlocalactivado))
            } else {
                binding.TBLocal3.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonlocaldesactivado))
            }
        }
        binding.TBLocal4.setOnCheckedChangeListener { _, _ ->
            if (binding.TBLocal4.isChecked && binding.TBLocal4.text != " ") {
                vaciarToggle(llenarListToggle())
                binding.TBLocal4.isChecked = true
                binding.TBLocal4.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonlocalactivado))
            } else {
                binding.TBLocal4.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonlocaldesactivado))
            }
        }
        binding.TBLocal5.setOnCheckedChangeListener { _, _ ->
            if (binding.TBLocal5.isChecked && binding.TBLocal5.text != " ") {
                vaciarToggle(llenarListToggle())
                binding.TBLocal5.isChecked = true
                binding.TBLocal5.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonlocalactivado))
            } else {
                binding.TBLocal5.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonlocaldesactivado))
            }
        }

        //Pulsar boton Visitante
        binding.TBVisitante1.setOnCheckedChangeListener { _, _ ->
            if (binding.TBVisitante1.isChecked && binding.TBVisitante1.text != " ") {
                vaciarToggle(llenarListToggle())
                binding.TBVisitante1.isChecked = true
                binding.TBVisitante1.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonvisitanteactivo))
            } else {
                binding.TBVisitante1.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonvisitantedesactivado))
            }
        }
        binding.TBVisitante2.setOnCheckedChangeListener { _, _ ->
            if (binding.TBVisitante2.isChecked && binding.TBVisitante2.text != " ") {
                vaciarToggle(llenarListToggle())
                binding.TBVisitante2.isChecked = true
                binding.TBVisitante2.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonvisitanteactivo))
            } else {
                binding.TBVisitante2.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonvisitantedesactivado))
            }
        }
        binding.TBVisitante3.setOnCheckedChangeListener { _, _ ->
            if (binding.TBVisitante3.isChecked && binding.TBVisitante3.text != " ") {
                vaciarToggle(llenarListToggle())
                binding.TBVisitante3.isChecked = true
                binding.TBVisitante3.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonvisitanteactivo))
            } else {
                binding.TBVisitante3.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonvisitantedesactivado))
            }
        }
        binding.TBVisitante4.setOnCheckedChangeListener { _, _ ->
            if (binding.TBVisitante4.isChecked && binding.TBVisitante4.text != " ") {
                vaciarToggle(llenarListToggle())
                binding.TBVisitante4.isChecked = true
                binding.TBVisitante4.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonvisitanteactivo))
            } else {
                binding.TBVisitante4.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonvisitantedesactivado))
            }
        }
        binding.TBVisitante5.setOnCheckedChangeListener { _, _ ->
            if (binding.TBVisitante5.isChecked && binding.TBVisitante5.text != " ") {
                vaciarToggle(llenarListToggle())
                binding.TBVisitante5.isChecked = true
                binding.TBVisitante5.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonvisitanteactivo))
            } else {
                binding.TBVisitante5.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonvisitantedesactivado))
            }
        }

        //Pulsar boton Falta
        binding.imageFaltaL.setOnClickListener {
            mostrarFaltasEquipo("Local")
        }
        binding.imageFaltaV.setOnClickListener {
            mostrarFaltasEquipo("Visitante")
        }

        //Pulsar boton Historial
        binding.btnHistorialJugadas.setOnClickListener {
            val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
            val idPartido = prefs.getString("idPartido", "").toString()

            paraCronometro()

            val builder = AlertDialog.Builder(binding.root.context)
            val view = layoutInflater.inflate(R.layout.ver_historial, null)
            builder.setView(view)

            val listview = view.findViewById<ListView>(R.id.LVHistorial)

            db.collection("MinutoaMinuto").document(idPartido).get().addOnSuccessListener {
                val listaminuto = mutableListOf<MinutoAMinuto>()
                val lista = it.get("registro") as ArrayList<Map<String?, Any?>>
                for (i in (lista.count() - 1) downTo 0) {
                    val minutoMap = lista[i]

                    val coordenadaX = (minutoMap["coordenada_x"] as? Double)
                    val coordenadaY = (minutoMap["coordenada_y"] as? Double)

                    val minuto = MinutoAMinuto(
                        minutoMap["cuarto"].toString(),
                        minutoMap["dorsal"].toString(),
                        minutoMap["nombre"].toString(),
                        minutoMap["equipo"].toString(),
                        minutoMap["frase"].toString(),
                        minutoMap["resultado"].toString(),
                        minutoMap["tiempo"].toString(),
                        minutoMap["tipoFrase"].toString(),
                        minutoMap["tipoImg"].toString(),
                        coordenadaX,
                        coordenadaY
                    )
                    listaminuto.add(minuto)
                }
                val myAdapter = AdaptadorMinuto(view.context, listaminuto)
                listview.adapter = myAdapter
                listview.onItemLongClickListener = AdapterView.OnItemLongClickListener { arg0, arg1, pos, id ->

                    val builder2 = AlertDialog.Builder(binding.root.context)
                    val view2 = layoutInflater.inflate(R.layout.borrardialog, null)
                    builder2.setView(view2)
                    view2.findViewById<TextView>(R.id.txtIdBorrar).text = listaminuto[pos].frase + " #" + listaminuto[pos].dorsal
                    val dialog2 = builder2.create()
                    dialog2.show()

                    view2.findViewById<Button>(R.id.btnSi).setOnClickListener {
                        borraJugada(listaminuto[pos])
                        listaminuto.removeAt(pos)
                        myAdapter.updateData(listaminuto)
                        db.collection("MinutoaMinuto").document(idPartido).get().addOnSuccessListener {
                            val lista2 = it.get("registro") as ArrayList<Map<String?, Any?>>
                            lista2.removeAt((lista2.size - 1) - pos)
                            db.collection("MinutoaMinuto").document(idPartido).update(
                                hashMapOf(
                                    "registro" to lista2
                                ) as Map<String, Any>
                            ).addOnSuccessListener {
                                actualizaJugadaReciente()
                                dialog2.hide()
                            }
                        }
                    }
                    view2.findViewById<Button>(R.id.btnNo).setOnClickListener {
                        dialog2.hide()
                    }

                    true
                }
            }

            val dialog = builder.create()
            dialog.show()
        }

        //Acciones Partido
        binding.imageTML.setOnClickListener {
            val tml = binding.txtTiemposMLocal.text.toString()
            if (tml.toInt() > 0 && estado != "Finalizado") {
                tiempoMuerto("Local")
                tmL -= 1
                binding.txtTiemposMLocal.text = tmL.toString()
                db.collection("Partidos").document(idPartido).update(
                    hashMapOf(
                        "TiempoML" to tmL
                    ) as Map<String?, Any?>
                )
            }
        }
        binding.imageTMV.setOnClickListener {
            val tmv = binding.txtTiemposMVisitante.text.toString()
            if (tmv.toInt() > 0 && estado != "Finalizado") {
                tiempoMuerto("Visitante")
                tmV -= 1
                binding.txtTiemposMVisitante.text = tmV.toString()
                db.collection("Partidos").document(idPartido).update(
                    hashMapOf(
                        "TiempoMV" to tmV
                    ) as Map<String?, Any?>
                )
            }
        }

        binding.btnFalta.setOnClickListener {
            if (estado != "Finalizado") {
                val builder = AlertDialog.Builder(binding.root.context)
                val view = layoutInflater.inflate(R.layout.accion_partido, null)
                builder.setView(view)
                view.findViewById<Button>(R.id.btnAnotar).text = "RECIBIDA"
                view.findViewById<Button>(R.id.btnFallar).text = "COMETIDA"
                val dialog = builder.create()
                dialog.show()
                paraCronometro()

                falL = binding.txtFaltasLocal.text.toString().toInt()
                falV = binding.txtFaltasVisitante.text.toString().toInt()

                view.findViewById<Button>(R.id.btnAnotar).setOnClickListener {
                    val lista = llenarListToggle()
                    manejarFalta("FALTA RECIBIDA", comprobarEquipoJugador(llenarListToggle()), lista, idPartido)
                    vaciarToggle(lista)
                    dialog.hide()
                }

                view.findViewById<Button>(R.id.btnFallar).setOnClickListener {
                    val lista = llenarListToggle()
                    manejarFalta("FALTA COMETIDA",comprobarEquipoJugador(llenarListToggle()), lista, idPartido)
                    vaciarToggle(lista)
                    dialog.hide()
                }

                actualizaTiempo()
            }
        }
        binding.btnFalEspecial.setOnClickListener {
            if (estado != "Finalizado") {
                val builder = AlertDialog.Builder(binding.root.context)
                val view = layoutInflater.inflate(R.layout.accion_partido, null)
                builder.setView(view)
                view.findViewById<Button>(R.id.btnAnotar).text = "TÉCNICA"
                view.findViewById<Button>(R.id.btnFallar).text = "ANTIDEPORTIVA"
                val dialog = builder.create()
                dialog.show()
                paraCronometro()

                falL = binding.txtFaltasLocal.text.toString().toInt()
                falV = binding.txtFaltasVisitante.text.toString().toInt()

                view.findViewById<Button>(R.id.btnAnotar).setOnClickListener {
                    val lista = llenarListToggle()
                    manejarFalta("FALTA TÉCNICA",comprobarEquipoJugador(llenarListToggle()), lista, idPartido)
                    vaciarToggle(lista)
                    dialog.dismiss()
                }

                view.findViewById<Button>(R.id.btnFallar).setOnClickListener {
                    val lista = llenarListToggle()
                    manejarFalta("FALTA ANTIDEPORTIVA",comprobarEquipoJugador(llenarListToggle()), lista, idPartido)
                    vaciarToggle(lista)
                    dialog.dismiss()
                }

                actualizaTiempo()
            }
        }
        binding.btnTL.setOnClickListener {
            if (estado != "Finalizado") {
                val builder = AlertDialog.Builder(binding.root.context)
                val view = layoutInflater.inflate(R.layout.accion_tl, null)
                builder.setView(view)
                val dialog = builder.create()
                dialog.show()
                paraCronometro()

                view.findViewById<Button>(R.id.btnADGuardarTL).setOnClickListener {

                    val lista = llenarListToggle()
                    for (i in 0..<lista.count()) {
                        if (lista[i].isChecked) {
                            if (lista[i].id == R.id.TBLocal1 || lista[i].id == R.id.TBLocal2 || lista[i].id == R.id.TBLocal3 || lista[i].id == R.id.TBLocal4 || lista[i].id == R.id.TBLocal5) {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        var countTL = 0
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()
                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Local") {

                                                if (view.findViewById<RadioButton>(R.id.rb1tlA).isChecked || view.findViewById<RadioButton>(
                                                        R.id.rb1tlF
                                                    ).isChecked
                                                )
                                                    countTL++
                                                if (view.findViewById<RadioButton>(R.id.rb2tlA).isChecked || view.findViewById<RadioButton>(
                                                        R.id.rb2tlF
                                                    ).isChecked
                                                )
                                                    countTL++
                                                if (view.findViewById<RadioButton>(R.id.rb3tlA).isChecked || view.findViewById<RadioButton>(
                                                        R.id.rb3tlF
                                                    ).isChecked
                                                )
                                                    countTL++

                                                db.collection("MinutoaMinuto")
                                                    .document(idPartido).get()
                                                    .addOnSuccessListener { it2 ->
                                                        val listRegistros =
                                                            it2.get("registro") as ArrayList<Map<String?, Any?>>
                                                        val registro = hashMapOf(
                                                            "cuarto" to cuarto,
                                                            "dorsal" to lista[i].text,
                                                            "nombre" to jugador["nombre"],
                                                            "frase" to "$countTL TIROS LIBRES PARA ",
                                                            "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                            "tiempo" to binding.TiempoCuarto.text.toString(),
                                                            "equipo" to "Local",
                                                            "tipoFrase" to "3",
                                                            "tipoImg" to "4"
                                                        ) as Map<String?, Any?>
                                                        listRegistros.add(registro)

                                                        if (view.findViewById<RadioButton>(R.id.rb1tlA).isChecked) {
                                                            jugador["tlA"] =
                                                                jugador["tlA"].toString().toInt() + 1
                                                            jugador["puntos"] =
                                                                jugador["puntos"].toString().toInt() + 1
                                                            binding.txtPuntosLocal.text =
                                                                (binding.txtPuntosLocal.text.toString()
                                                                    .toInt() + 1).toString()

                                                            actualizaResultado()

                                                            Toast.makeText(
                                                                binding.root.context,
                                                                "Tiro Libre del jugador " + lista[i].textOn + " anotado",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            val registro = hashMapOf(
                                                                "cuarto" to cuarto,
                                                                "dorsal" to lista[i].text,
                                                                "nombre" to jugador["nombre"],
                                                                "frase" to "TIRO LIBRE ANOTADO",
                                                                "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                                "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                "equipo" to "Local",
                                                                "tipoFrase" to "1",
                                                                "tipoImg" to "5"
                                                            ) as Map<String?, Any?>
                                                            listRegistros.add(registro)

                                                        } else if (view.findViewById<RadioButton>(R.id.rb1tlF).isChecked) {

                                                            jugador["tlF"] =
                                                                jugador["tlF"].toString().toInt() + 1
                                                            Toast.makeText(
                                                                binding.root.context,
                                                                "Tiro Libre del jugador " + lista[i].textOn + " fallado",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            val registro = hashMapOf(
                                                                "cuarto" to cuarto,
                                                                "dorsal" to lista[i].text,
                                                                "nombre" to jugador["nombre"],
                                                                "frase" to "TIRO LIBRE FALLADO",
                                                                "resultado" to "",
                                                                "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                "equipo" to "Local",
                                                                "tipoFrase" to "3",
                                                                "tipoImg" to "6"
                                                            ) as Map<String?, Any?>
                                                            listRegistros.add(registro)

                                                        }

                                                        if (view.findViewById<RadioButton>(R.id.rb2tlA).isChecked) {

                                                            jugador["tlA"] =
                                                                jugador["tlA"].toString().toInt() + 1
                                                            jugador["puntos"] =
                                                                jugador["puntos"].toString().toInt() + 1
                                                            binding.txtPuntosLocal.text =
                                                                (binding.txtPuntosLocal.text.toString()
                                                                    .toInt() + 1).toString()

                                                            actualizaResultado()

                                                            Toast.makeText(
                                                                binding.root.context,
                                                                "Tiro Libre del jugador " + lista[i].textOn + " anotado",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            val registro = hashMapOf(
                                                                "cuarto" to cuarto,
                                                                "dorsal" to lista[i].text,
                                                                "nombre" to jugador["nombre"],
                                                                "frase" to "TIRO LIBRE ANOTADO",
                                                                "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                                "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                "equipo" to "Local",
                                                                "tipoFrase" to "1",
                                                                "tipoImg" to "5"
                                                            ) as Map<String?, Any?>
                                                            listRegistros.add(registro)

                                                        } else if (view.findViewById<RadioButton>(R.id.rb2tlF).isChecked) {

                                                            jugador["tlF"] =
                                                                jugador["tlF"].toString().toInt() + 1
                                                            Toast.makeText(
                                                                binding.root.context,
                                                                "Tiro Libre del jugador " + lista[i].textOn + " fallado",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            val registro = hashMapOf(
                                                                "cuarto" to cuarto,
                                                                "dorsal" to lista[i].text,
                                                                "nombre" to jugador["nombre"],
                                                                "frase" to "TIRO LIBRE FALLADO",
                                                                "resultado" to "",
                                                                "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                "equipo" to "Local",
                                                                "tipoFrase" to "3",
                                                                "tipoImg" to "6"
                                                            ) as Map<String?, Any?>
                                                            listRegistros.add(registro)

                                                        }

                                                        if (view.findViewById<RadioButton>(R.id.rb3tlA).isChecked) {

                                                            jugador["tlA"] =
                                                                jugador["tlA"].toString().toInt() + 1
                                                            jugador["puntos"] =
                                                                jugador["puntos"].toString().toInt() + 1
                                                            binding.txtPuntosLocal.text =
                                                                (binding.txtPuntosLocal.text.toString()
                                                                    .toInt() + 1).toString()

                                                            actualizaResultado()

                                                            Toast.makeText(
                                                                binding.root.context,
                                                                "Tiro Libre del jugador " + lista[i].textOn + " anotado",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            val registro = hashMapOf(
                                                                "cuarto" to cuarto,
                                                                "dorsal" to lista[i].text,
                                                                "nombre" to jugador["nombre"],
                                                                "frase" to "TIRO LIBRE ANOTADO",
                                                                "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                                "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                "equipo" to "Local",
                                                                "tipoFrase" to "1",
                                                                "tipoImg" to "5"
                                                            ) as Map<String?, Any?>
                                                            listRegistros.add(registro)

                                                        } else if (view.findViewById<RadioButton>(R.id.rb3tlF).isChecked) {

                                                            jugador["tlF"] =
                                                                jugador["tlF"].toString().toInt() + 1
                                                            Toast.makeText(
                                                                binding.root.context,
                                                                "Tiro Libre del jugador " + lista[i].textOn + " fallado",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            val registro = hashMapOf(
                                                                "cuarto" to cuarto,
                                                                "dorsal" to lista[i].text,
                                                                "nombre" to jugador["nombre"],
                                                                "frase" to "TIRO LIBRE FALLADO",
                                                                "resultado" to "",
                                                                "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                "equipo" to "Local",
                                                                "tipoFrase" to "3",
                                                                "tipoImg" to "6"
                                                            ) as Map<String?, Any?>
                                                            listRegistros.add(registro)

                                                        }

                                                        db.collection("Estadisticas")
                                                            .document(idPartido)
                                                            .update(
                                                                hashMapOf(
                                                                    listJugador[j] to jugador
                                                                ) as Map<String, Any>
                                                            ).addOnSuccessListener {
                                                                calcularVal(listJugador[j], jugador)
                                                            }
                                                        db.collection("MinutoaMinuto")
                                                            .document(idPartido)
                                                            .update(
                                                                hashMapOf(
                                                                    "registro" to listRegistros,
                                                                ) as Map<String?, Any?>
                                                            ).addOnSuccessListener {
                                                                actualizaJugadaReciente()
                                                            }
                                                    }

                                            }
                                        }
                                    }
                            } else {

                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        var countTL = 0
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()
                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Visitante") {

                                                if (view.findViewById<RadioButton>(R.id.rb1tlA).isChecked || view.findViewById<RadioButton>(
                                                        R.id.rb1tlF
                                                    ).isChecked
                                                )
                                                    countTL++
                                                if (view.findViewById<RadioButton>(R.id.rb2tlA).isChecked || view.findViewById<RadioButton>(
                                                        R.id.rb2tlF
                                                    ).isChecked
                                                )
                                                    countTL++
                                                if (view.findViewById<RadioButton>(R.id.rb3tlA).isChecked || view.findViewById<RadioButton>(
                                                        R.id.rb3tlF
                                                    ).isChecked
                                                )
                                                    countTL++

                                                db.collection("MinutoaMinuto")
                                                    .document(idPartido).get()
                                                    .addOnSuccessListener { it2 ->
                                                        val listRegistros =
                                                            it2.get("registro") as ArrayList<Map<String?, Any?>>
                                                        val registro = hashMapOf(
                                                            "cuarto" to cuarto,
                                                            "dorsal" to lista[i].text,
                                                            "nombre" to jugador["nombre"],
                                                            "frase" to "$countTL TIROS LIBRES PARA",
                                                            "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                            "tiempo" to binding.TiempoCuarto.text.toString(),
                                                            "equipo" to "Visitante",
                                                            "tipoFrase" to "3",
                                                            "tipoImg" to "4",
                                                        ) as Map<String?, Any?>
                                                        listRegistros.add(registro)

                                                        if (view.findViewById<RadioButton>(R.id.rb1tlA).isChecked) {
                                                            jugador["tlA"] =
                                                                jugador["tlA"].toString().toInt() + 1
                                                            jugador["puntos"] =
                                                                jugador["puntos"].toString().toInt() + 1
                                                            binding.txtPuntosVisitante.text =
                                                                (binding.txtPuntosVisitante.text.toString()
                                                                    .toInt() + 1).toString()

                                                            actualizaResultado()

                                                            Toast.makeText(
                                                                binding.root.context,
                                                                "Tiro Libre del jugador " + lista[i].textOn + " anotado",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            val registro = hashMapOf(
                                                                "cuarto" to cuarto,
                                                                "dorsal" to lista[i].text,
                                                                "nombre" to jugador["nombre"],
                                                                "frase" to "TIRO LIBRE ANOTADO",
                                                                "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                                "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                "equipo" to "Visitante",
                                                                "tipoFrase" to "1",
                                                                "tipoImg" to "5"
                                                            ) as Map<String?, Any?>
                                                            listRegistros.add(registro)

                                                        } else if (view.findViewById<RadioButton>(R.id.rb1tlF).isChecked) {

                                                            jugador["tlF"] =
                                                                jugador["tlF"].toString().toInt() + 1
                                                            Toast.makeText(
                                                                binding.root.context,
                                                                "Tiro Libre del jugador " + lista[i].textOn + " fallado",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            val registro = hashMapOf(
                                                                "cuarto" to cuarto,
                                                                "dorsal" to lista[i].text,
                                                                "nombre" to jugador["nombre"],
                                                                "frase" to "TIRO LIBRE FALLADO",
                                                                "resultado" to "",
                                                                "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                "equipo" to "Visitante",
                                                                "tipoFrase" to "3",
                                                                "tipoImg" to "6"
                                                            ) as Map<String?, Any?>
                                                            listRegistros.add(registro)

                                                        }

                                                        if (view.findViewById<RadioButton>(R.id.rb2tlA).isChecked) {

                                                            jugador["tlA"] =
                                                                jugador["tlA"].toString().toInt() + 1
                                                            jugador["puntos"] =
                                                                jugador["puntos"].toString().toInt() + 1
                                                            binding.txtPuntosVisitante.text =
                                                                (binding.txtPuntosVisitante.text.toString()
                                                                    .toInt() + 1).toString()

                                                            actualizaResultado()

                                                            Toast.makeText(
                                                                binding.root.context,
                                                                "Tiro Libre del jugador " + lista[i].textOn + " anotado",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            val registro = hashMapOf(
                                                                "cuarto" to cuarto,
                                                                "dorsal" to lista[i].text,
                                                                "nombre" to jugador["nombre"],
                                                                "frase" to "TIRO LIBRE ANOTADO",
                                                                "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                                "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                "equipo" to "Visitante",
                                                                "tipoFrase" to "1",
                                                                "tipoImg" to "5"
                                                            ) as Map<String?, Any?>
                                                            listRegistros.add(registro)

                                                        } else if (view.findViewById<RadioButton>(R.id.rb2tlF).isChecked) {

                                                            jugador["tlF"] =
                                                                jugador["tlF"].toString().toInt() + 1
                                                            Toast.makeText(
                                                                binding.root.context,
                                                                "Tiro Libre del jugador " + lista[i].textOn + " fallado",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            val registro = hashMapOf(
                                                                "cuarto" to cuarto,
                                                                "dorsal" to lista[i].text,
                                                                "nombre" to jugador["nombre"],
                                                                "frase" to "TIRO LIBRE FALLADO",
                                                                "resultado" to "",
                                                                "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                "equipo" to "Visitante",
                                                                "tipoFrase" to "3",
                                                                "tipoImg" to "6"
                                                            ) as Map<String?, Any?>
                                                            listRegistros.add(registro)

                                                        }

                                                        if (view.findViewById<RadioButton>(R.id.rb3tlA).isChecked) {

                                                            jugador["tlA"] =
                                                                jugador["tlA"].toString().toInt() + 1
                                                            jugador["puntos"] =
                                                                jugador["puntos"].toString().toInt() + 1
                                                            binding.txtPuntosVisitante.text =
                                                                (binding.txtPuntosVisitante.text.toString()
                                                                    .toInt() + 1).toString()

                                                            actualizaResultado()

                                                            Toast.makeText(
                                                                binding.root.context,
                                                                "Tiro Libre del jugador " + lista[i].textOn + " anotado",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            val registro = hashMapOf(
                                                                "cuarto" to cuarto,
                                                                "dorsal" to lista[i].text,
                                                                "nombre" to jugador["nombre"],
                                                                "frase" to "TIRO LIBRE ANOTADO",
                                                                "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                                "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                "equipo" to "Visitante",
                                                                "tipoFrase" to "1",
                                                                "tipoImg" to "5"
                                                            ) as Map<String?, Any?>
                                                            listRegistros.add(registro)

                                                        } else if (view.findViewById<RadioButton>(R.id.rb3tlF).isChecked) {

                                                            jugador["tlF"] =
                                                                jugador["tlF"].toString().toInt() + 1
                                                            Toast.makeText(
                                                                binding.root.context,
                                                                "Tiro Libre del jugador " + lista[i].textOn + " fallado",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            val registro = hashMapOf(
                                                                "cuarto" to cuarto,
                                                                "dorsal" to lista[i].text,
                                                                "nombre" to jugador["nombre"],
                                                                "frase" to "TIRO LIBRE FALLADO",
                                                                "resultado" to "",
                                                                "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                "equipo" to "Visitante",
                                                                "tipoFrase" to "3",
                                                                "tipoImg" to "6"
                                                            ) as Map<String?, Any?>
                                                            listRegistros.add(registro)

                                                        }

                                                        db.collection("Estadisticas")
                                                            .document(idPartido)
                                                            .update(
                                                                hashMapOf(
                                                                    listJugador[j] to jugador
                                                                ) as Map<String, Any>
                                                            ).addOnSuccessListener {
                                                                calcularVal(listJugador[j], jugador)
                                                            }
                                                        db.collection("MinutoaMinuto")
                                                            .document(idPartido)
                                                            .update(
                                                                hashMapOf(
                                                                    "registro" to listRegistros,
                                                                ) as Map<String?, Any?>
                                                            ).addOnSuccessListener {
                                                                actualizaJugadaReciente()
                                                            }
                                                    }

                                            }
                                        }
                                    }

                            }
                        }
                    }
                    dialog.hide()
                    vaciarToggle(lista)
                }
                actualizaTiempo()
            }
        }
        binding.btnAsistencia.setOnClickListener {
            if (estado != "Finalizado") {
                val lista = llenarListToggle()
                for (i in 0..<lista.count()) {
                    if (lista[i].isChecked) {
                        if (lista[i].id == R.id.TBLocal1 || lista[i].id == R.id.TBLocal2 || lista[i].id == R.id.TBLocal3 || lista[i].id == R.id.TBLocal4 || lista[i].id == R.id.TBLocal5) {
                            db.collection("Estadisticas").document(idPartido).get()
                                .addOnSuccessListener {
                                    val listJugador =
                                        it.get("ListadoJugadores") as ArrayList<String>
                                    for (j in 0..<listJugador.count()) {
                                        val jugador =
                                            (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                        if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Local") {
                                            jugador["asi"] = jugador["asi"].toString().toInt() + 1
                                            db.collection("Estadisticas")
                                                .document(idPartido)
                                                .update(
                                                    hashMapOf(
                                                        listJugador[j] to jugador
                                                    ) as Map<String, Any>
                                                ).addOnSuccessListener {
                                                    Toast.makeText(
                                                        binding.root.context,
                                                        "Asistencia del jugador " + lista[i].textOn,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    calcularVal(listJugador[j], jugador)
                                                }

                                            db.collection("MinutoaMinuto").document(idPartido).get()
                                                .addOnSuccessListener { min ->
                                                    val listRegistros =
                                                        min.get("registro") as ArrayList<Map<String?, Any?>>
                                                    val registro = hashMapOf(
                                                        "cuarto" to cuarto,
                                                        "dorsal" to lista[i].text,
                                                        "nombre" to jugador["nombre"],
                                                        "frase" to "ASISTENCIA",
                                                        "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                        "tiempo" to binding.TiempoCuarto.text.toString(),
                                                        "equipo" to "Local",
                                                        "tipoFrase" to "3",
                                                        "tipoImg" to "9"
                                                    ) as Map<String?, Any?>
                                                    listRegistros.add(registro)
                                                    db.collection("MinutoaMinuto").document(idPartido)
                                                        .update(
                                                            hashMapOf(
                                                                "registro" to listRegistros,
                                                            ) as Map<String?, Any?>
                                                        ).addOnSuccessListener {
                                                            actualizaJugadaReciente()
                                                        }
                                                }
                                        }
                                    }

                                }

                        } else {
                            db.collection("Estadisticas").document(idPartido).get()
                                .addOnSuccessListener {
                                    val listJugador =
                                        it.get("ListadoJugadores") as ArrayList<String>
                                    for (j in 0..<listJugador.count()) {
                                        val jugador =
                                            (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                        if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Visitante") {
                                            jugador["asi"] = jugador["asi"].toString().toInt() + 1
                                            db.collection("Estadisticas")
                                                .document(idPartido)
                                                .update(
                                                    hashMapOf(
                                                        listJugador[j] to jugador
                                                    ) as Map<String, Any>
                                                ).addOnSuccessListener {
                                                    Toast.makeText(
                                                        binding.root.context,
                                                        "Asistencia del jugador " + lista[i].textOn,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    calcularVal(listJugador[j], jugador)
                                                }

                                            db.collection("MinutoaMinuto").document(idPartido).get()
                                                .addOnSuccessListener { min ->
                                                    val listRegistros =
                                                        min.get("registro") as ArrayList<Map<String?, Any?>>
                                                    val registro = hashMapOf(
                                                        "cuarto" to cuarto,
                                                        "dorsal" to lista[i].text,
                                                        "nombre" to jugador["nombre"],
                                                        "frase" to "ASISTENCIA",
                                                        "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                        "tiempo" to binding.TiempoCuarto.text.toString(),
                                                        "equipo" to "Visitante",
                                                        "tipoFrase" to "3",
                                                        "tipoImg" to "9"
                                                    ) as Map<String?, Any?>
                                                    listRegistros.add(registro)
                                                    db.collection("MinutoaMinuto").document(idPartido)
                                                        .update(
                                                            hashMapOf(
                                                                "registro" to listRegistros,
                                                            ) as Map<String?, Any?>
                                                        ).addOnSuccessListener {
                                                            actualizaJugadaReciente()
                                                        }
                                                }
                                        }
                                    }

                                }

                        }

                    }
                }
                vaciarToggle(lista)
                actualizaTiempo()
            }
        }
        binding.btnT2p.setOnClickListener {
            if (estado != "Finalizado") {
                val builder = AlertDialog.Builder(binding.root.context)
                val view = layoutInflater.inflate(R.layout.accion_partido, null)
                builder.setView(view)
                val dialog = builder.create()
                dialog.show()

                view.findViewById<Button>(R.id.btnAnotar).setOnClickListener {
                    val lista = llenarListToggle()
                    for (i in 0..<lista.count()) {
                        if (lista[i].isChecked) {
                            if (lista[i].id == R.id.TBLocal1 || lista[i].id == R.id.TBLocal2 || lista[i].id == R.id.TBLocal3 || lista[i].id == R.id.TBLocal4 || lista[i].id == R.id.TBLocal5) {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {

                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Local") {

                                                val builder2 = AlertDialog.Builder(binding.root.context)
                                                val view2 = layoutInflater.inflate(R.layout.accion_tiro, null)
                                                builder2.setView(view2)
                                                val dialog2 = builder2.create()
                                                dialog2.show()
                                                dialog.hide()

                                                view2.findViewById<ImageView>(R.id.pistaBaloncesto).viewTreeObserver.addOnGlobalLayoutListener {
                                                    if (view2.findViewById<ImageView>(R.id.pistaBaloncesto).width > 0 && view2.findViewById<ImageView>(
                                                            R.id.pistaBaloncesto
                                                        ).height > 0
                                                    ) {
                                                        view2.findViewById<ImageView>(R.id.pistaBaloncesto).setOnTouchListener { v, event ->
                                                            if (event.action == MotionEvent.ACTION_DOWN) {
                                                                convertirCoordenadas(
                                                                    event.x,
                                                                    event.y,
                                                                    view2.findViewById(R.id.pistaBaloncesto)
                                                                )?.let { (x, y) ->
                                                                    view2.findViewById<TiroView>(R.id.tiroView)
                                                                        .agregarTiro(x, y, true, jugador["equipo"].toString())

                                                                    val listTiros = jugador["tiros"] as ArrayList<Map<String, Any>>
                                                                    listTiros.add(
                                                                        hashMapOf(
                                                                            "x" to x,
                                                                            "y" to y,
                                                                            "cuarto" to cuarto,
                                                                            "encestado" to true
                                                                        )
                                                                    )
                                                                    jugador["tiros"] = listTiros
                                                                    jugador["tc2pA"] =
                                                                        jugador["tc2pA"].toString().toInt() + 1
                                                                    jugador["puntos"] =
                                                                        jugador["puntos"].toString().toInt() + 2
                                                                    binding.txtPuntosLocal.text =
                                                                        (binding.txtPuntosLocal.text.toString()
                                                                            .toInt() + 2).toString()

                                                                    actualizaResultado()

                                                                    db.collection("Estadisticas")
                                                                        .document(idPartido)
                                                                        .update(
                                                                            hashMapOf(
                                                                                listJugador[j] to jugador
                                                                            ) as Map<String, Any>
                                                                        ).addOnSuccessListener {
                                                                            Toast.makeText(
                                                                                binding.root.context,
                                                                                "Canasta del jugador " + lista[i].textOn + " de 2p",
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                            calcularVal(listJugador[j], jugador)
                                                                            db.collection("MinutoaMinuto")
                                                                                .document(idPartido).get()
                                                                                .addOnSuccessListener { it2 ->
                                                                                    val listRegistros =
                                                                                        it2.get("registro") as ArrayList<Map<String?, Any?>>
                                                                                    val registro = hashMapOf(
                                                                                        "cuarto" to cuarto,
                                                                                        "dorsal" to lista[i].text,
                                                                                        "nombre" to jugador["nombre"],
                                                                                        "frase" to "CANASTA DE 2 PUNTOS",
                                                                                        "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                                                        "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                                        "equipo" to "Local",
                                                                                        "tipoFrase" to "1",
                                                                                        "tipoImg" to "7",
                                                                                        "coordenada_x" to x,
                                                                                        "coordenada_y" to y
                                                                                    ) as Map<String?, Any?>
                                                                                    listRegistros.add(registro)
                                                                                    db.collection("MinutoaMinuto")
                                                                                        .document(idPartido)
                                                                                        .update(
                                                                                            hashMapOf(
                                                                                                "registro" to listRegistros,
                                                                                            ) as Map<String?, Any?>
                                                                                        ).addOnSuccessListener {
                                                                                            actualizaJugadaReciente()
                                                                                            dialog2.hide()
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                            }
                                                            true
                                                        }
                                                    }
                                                }

                                            }
                                        }

                                    }


                            } else {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Visitante") {

                                                val builder2 = AlertDialog.Builder(binding.root.context)
                                                val view2 = layoutInflater.inflate(R.layout.accion_tiro, null)
                                                builder2.setView(view2)
                                                val dialog2 = builder2.create()
                                                dialog2.show()
                                                dialog.hide()

                                                view2.findViewById<ImageView>(R.id.pistaBaloncesto).viewTreeObserver.addOnGlobalLayoutListener {
                                                    if (view2.findViewById<ImageView>(R.id.pistaBaloncesto).width > 0 && view2.findViewById<ImageView>(
                                                            R.id.pistaBaloncesto
                                                        ).height > 0
                                                    ) {
                                                        view2.findViewById<ImageView>(R.id.pistaBaloncesto).setOnTouchListener { v, event ->
                                                            if (event.action == MotionEvent.ACTION_DOWN) {
                                                                convertirCoordenadas(
                                                                    event.x,
                                                                    event.y,
                                                                    view2.findViewById(R.id.pistaBaloncesto)
                                                                )?.let { (x, y) ->
                                                                    view2.findViewById<TiroView>(R.id.tiroView)
                                                                        .agregarTiro(x, y, true, jugador["equipo"].toString())

                                                                    val listTiros = jugador["tiros"] as ArrayList<Map<String, Any>>
                                                                    listTiros.add(
                                                                        hashMapOf(
                                                                            "x" to x,
                                                                            "y" to y,
                                                                            "cuarto" to cuarto,
                                                                            "encestado" to true
                                                                        )
                                                                    )
                                                                    jugador["tiros"] = listTiros
                                                                    jugador["tc2pA"] =
                                                                        jugador["tc2pA"].toString().toInt() + 1
                                                                    jugador["puntos"] =
                                                                        jugador["puntos"].toString().toInt() + 2
                                                                    binding.txtPuntosVisitante.text =
                                                                        (binding.txtPuntosVisitante.text.toString()
                                                                            .toInt() + 2).toString()

                                                                    actualizaResultado()

                                                                    db.collection("Estadisticas")
                                                                        .document(idPartido)
                                                                        .update(
                                                                            hashMapOf(
                                                                                listJugador[j] to jugador
                                                                            ) as Map<String, Any>
                                                                        ).addOnSuccessListener {
                                                                            Toast.makeText(
                                                                                binding.root.context,
                                                                                "Canasta del jugador " + lista[i].textOn + " de 2p",
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                            calcularVal(listJugador[j], jugador)
                                                                            db.collection("MinutoaMinuto")
                                                                                .document(idPartido).get()
                                                                                .addOnSuccessListener { it2 ->
                                                                                    val listRegistros =
                                                                                        it2.get("registro") as ArrayList<Map<String?, Any?>>
                                                                                    val registro = hashMapOf(
                                                                                        "cuarto" to cuarto,
                                                                                        "dorsal" to lista[i].text,
                                                                                        "nombre" to jugador["nombre"],
                                                                                        "frase" to "CANASTA DE 2 PUNTOS",
                                                                                        "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                                                        "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                                        "equipo" to "Visitante",
                                                                                        "tipoFrase" to "1",
                                                                                        "tipoImg" to "7",
                                                                                        "coordenada_x" to x,
                                                                                        "coordenada_y" to y
                                                                                    ) as Map<String?, Any?>
                                                                                    listRegistros.add(registro)
                                                                                    db.collection("MinutoaMinuto")
                                                                                        .document(idPartido)
                                                                                        .update(
                                                                                            hashMapOf(
                                                                                                "registro" to listRegistros,
                                                                                            ) as Map<String?, Any?>
                                                                                        ).addOnSuccessListener {
                                                                                            actualizaJugadaReciente()
                                                                                            dialog2.hide()
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                            }
                                                            true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                            }

                        }
                    }
                    vaciarToggle(lista)
                }

                view.findViewById<Button>(R.id.btnFallar).setOnClickListener {
                    val lista = llenarListToggle()
                    for (i in 0..<lista.count()) {
                        if (lista[i].isChecked) {
                            if (lista[i].id == R.id.TBLocal1 || lista[i].id == R.id.TBLocal2 || lista[i].id == R.id.TBLocal3 || lista[i].id == R.id.TBLocal4 || lista[i].id == R.id.TBLocal5) {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Local") {

                                                val builder2 = AlertDialog.Builder(binding.root.context)
                                                val view2 = layoutInflater.inflate(R.layout.accion_tiro, null)
                                                builder2.setView(view2)
                                                val dialog2 = builder2.create()
                                                dialog2.show()
                                                dialog.hide()

                                                view2.findViewById<ImageView>(R.id.pistaBaloncesto).viewTreeObserver.addOnGlobalLayoutListener {
                                                    if (view2.findViewById<ImageView>(R.id.pistaBaloncesto).width > 0 && view2.findViewById<ImageView>(
                                                            R.id.pistaBaloncesto
                                                        ).height > 0
                                                    ) {
                                                        view2.findViewById<ImageView>(R.id.pistaBaloncesto).setOnTouchListener { v, event ->
                                                            if (event.action == MotionEvent.ACTION_DOWN) {
                                                                convertirCoordenadas(
                                                                    event.x,
                                                                    event.y,
                                                                    view2.findViewById(R.id.pistaBaloncesto)
                                                                )?.let { (x, y) ->
                                                                    view2.findViewById<TiroView>(R.id.tiroView)
                                                                        .agregarTiro(x, y, false, jugador["equipo"].toString())

                                                                    val listTiros = jugador["tiros"] as ArrayList<Map<String, Any>>
                                                                    listTiros.add(
                                                                        hashMapOf(
                                                                            "x" to x,
                                                                            "y" to y,
                                                                            "cuarto" to cuarto,
                                                                            "encestado" to false
                                                                        )
                                                                    )
                                                                    jugador["tiros"] = listTiros
                                                                    jugador["tc2pF"] =
                                                                        jugador["tc2pF"].toString().toInt() + 1
                                                                    db.collection("Estadisticas")
                                                                        .document(idPartido)
                                                                        .update(
                                                                            hashMapOf(
                                                                                listJugador[j] to jugador
                                                                            ) as Map<String, Any>
                                                                        ).addOnSuccessListener {
                                                                            Toast.makeText(
                                                                                binding.root.context,
                                                                                "Canasta fallada del jugador " + lista[i].textOn + " de 2p",
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                            calcularVal(listJugador[j], jugador)
                                                                            db.collection("MinutoaMinuto")
                                                                                .document(idPartido).get()
                                                                                .addOnSuccessListener { it2 ->
                                                                                    val listRegistros =
                                                                                        it2.get("registro") as ArrayList<Map<String?, Any?>>
                                                                                    val registro = hashMapOf(
                                                                                        "cuarto" to cuarto,
                                                                                        "dorsal" to lista[i].text,
                                                                                        "nombre" to jugador["nombre"],
                                                                                        "frase" to "TIRO DE 2 FALLADO",
                                                                                        "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                                                        "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                                        "equipo" to "Local",
                                                                                        "tipoFrase" to "3",
                                                                                        "tipoImg" to "6",
                                                                                        "coordenada_x" to x,
                                                                                        "coordenada_y" to y
                                                                                    ) as Map<String?, Any?>
                                                                                    listRegistros.add(registro)
                                                                                    db.collection("MinutoaMinuto")
                                                                                        .document(idPartido)
                                                                                        .update(
                                                                                            hashMapOf(
                                                                                                "registro" to listRegistros,
                                                                                            ) as Map<String?, Any?>
                                                                                        ).addOnSuccessListener {
                                                                                            actualizaJugadaReciente()
                                                                                            dialog2.hide()
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                            }
                                                            true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }


                            } else {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Visitante") {

                                                val builder2 = AlertDialog.Builder(binding.root.context)
                                                val view2 = layoutInflater.inflate(R.layout.accion_tiro, null)
                                                builder2.setView(view2)
                                                val dialog2 = builder2.create()
                                                dialog2.show()
                                                dialog.hide()

                                                view2.findViewById<ImageView>(R.id.pistaBaloncesto).viewTreeObserver.addOnGlobalLayoutListener {
                                                    if (view2.findViewById<ImageView>(R.id.pistaBaloncesto).width > 0 && view2.findViewById<ImageView>(
                                                            R.id.pistaBaloncesto
                                                        ).height > 0
                                                    ) {
                                                        view2.findViewById<ImageView>(R.id.pistaBaloncesto).setOnTouchListener { v, event ->
                                                            if (event.action == MotionEvent.ACTION_DOWN) {
                                                                convertirCoordenadas(
                                                                    event.x,
                                                                    event.y,
                                                                    view2.findViewById(R.id.pistaBaloncesto)
                                                                )?.let { (x, y) ->
                                                                    view2.findViewById<TiroView>(R.id.tiroView)
                                                                        .agregarTiro(x, y, false, jugador["equipo"].toString())

                                                                    val listTiros = jugador["tiros"] as ArrayList<Map<String, Any>>
                                                                    listTiros.add(
                                                                        hashMapOf(
                                                                            "x" to x,
                                                                            "y" to y,
                                                                            "cuarto" to cuarto,
                                                                            "encestado" to false
                                                                        )
                                                                    )
                                                                    jugador["tiros"] = listTiros
                                                                    jugador["tc2pF"] =
                                                                        jugador["tc2pF"].toString().toInt() + 1
                                                                    db.collection("Estadisticas")
                                                                        .document(idPartido)
                                                                        .update(
                                                                            hashMapOf(
                                                                                listJugador[j] to jugador
                                                                            ) as Map<String, Any>
                                                                        ).addOnSuccessListener {
                                                                            Toast.makeText(
                                                                                binding.root.context,
                                                                                "Canasta fallada del jugador " + lista[i].textOn + " de 2p",
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                            calcularVal(listJugador[j], jugador)
                                                                            db.collection("MinutoaMinuto")
                                                                                .document(idPartido).get()
                                                                                .addOnSuccessListener { it2 ->
                                                                                    val listRegistros =
                                                                                        it2.get("registro") as ArrayList<Map<String?, Any?>>
                                                                                    val registro = hashMapOf(
                                                                                        "cuarto" to cuarto,
                                                                                        "dorsal" to lista[i].text,
                                                                                        "nombre" to jugador["nombre"],
                                                                                        "frase" to "TIRO DE 2 FALLADO",
                                                                                        "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                                                        "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                                        "equipo" to "Visitante",
                                                                                        "tipoFrase" to "3",
                                                                                        "tipoImg" to "6",
                                                                                        "coordenada_x" to x,
                                                                                        "coordenada_y" to y
                                                                                    ) as Map<String?, Any?>
                                                                                    listRegistros.add(registro)
                                                                                    db.collection("MinutoaMinuto")
                                                                                        .document(idPartido)
                                                                                        .update(
                                                                                            hashMapOf(
                                                                                                "registro" to listRegistros,
                                                                                            ) as Map<String?, Any?>
                                                                                        ).addOnSuccessListener {
                                                                                            actualizaJugadaReciente()
                                                                                            dialog2.hide()
                                                                                        }
                                                                                }
                                                                        }

                                                                }
                                                            }
                                                            true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                            }

                        }
                    }
                    vaciarToggle(lista)
                }

                actualizaTiempo()
            }

        }
        binding.btnT3p.setOnClickListener {
            if (estado != "Finalizado") {
                val builder = AlertDialog.Builder(binding.root.context)
                val view = layoutInflater.inflate(R.layout.accion_partido, null)
                builder.setView(view)
                val dialog = builder.create()
                dialog.show()

                view.findViewById<Button>(R.id.btnAnotar).setOnClickListener {

                    val lista = llenarListToggle()
                    for (i in 0..<lista.count()) {
                        if (lista[i].isChecked) {
                            if (lista[i].id == R.id.TBLocal1 || lista[i].id == R.id.TBLocal2 || lista[i].id == R.id.TBLocal3 || lista[i].id == R.id.TBLocal4 || lista[i].id == R.id.TBLocal5) {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Local") {

                                                val builder2 = AlertDialog.Builder(binding.root.context)
                                                val view2 = layoutInflater.inflate(R.layout.accion_tiro, null)
                                                builder2.setView(view2)
                                                val dialog2 = builder2.create()
                                                dialog2.show()
                                                dialog.hide()

                                                view2.findViewById<ImageView>(R.id.pistaBaloncesto).viewTreeObserver.addOnGlobalLayoutListener {
                                                    if (view2.findViewById<ImageView>(R.id.pistaBaloncesto).width > 0 && view2.findViewById<ImageView>(
                                                            R.id.pistaBaloncesto
                                                        ).height > 0
                                                    ) {
                                                        view2.findViewById<ImageView>(R.id.pistaBaloncesto).setOnTouchListener { v, event ->
                                                            if (event.action == MotionEvent.ACTION_DOWN) {
                                                                convertirCoordenadas(
                                                                    event.x,
                                                                    event.y,
                                                                    view2.findViewById(R.id.pistaBaloncesto)
                                                                )?.let { (x, y) ->
                                                                    view2.findViewById<TiroView>(R.id.tiroView)
                                                                        .agregarTiro(x, y, true, jugador["equipo"].toString())

                                                                    val listTiros = jugador["tiros"] as ArrayList<Map<String, Any>>
                                                                    listTiros.add(
                                                                        hashMapOf(
                                                                            "x" to x,
                                                                            "y" to y,
                                                                            "cuarto" to cuarto,
                                                                            "encestado" to true
                                                                        )
                                                                    )
                                                                    jugador["tiros"] = listTiros
                                                                    jugador["tc3pA"] =
                                                                        jugador["tc3pA"].toString().toInt() + 1
                                                                    jugador["puntos"] =
                                                                        jugador["puntos"].toString().toInt() + 3
                                                                    binding.txtPuntosLocal.text =
                                                                        (binding.txtPuntosLocal.text.toString()
                                                                            .toInt() + 3).toString()

                                                                    actualizaResultado()

                                                                    db.collection("Estadisticas")
                                                                        .document(idPartido)
                                                                        .update(
                                                                            hashMapOf(
                                                                                listJugador[j] to jugador
                                                                            ) as Map<String, Any>
                                                                        ).addOnSuccessListener {
                                                                            Toast.makeText(
                                                                                binding.root.context,
                                                                                "Canasta del jugador " + lista[i].textOn + " de 3p",
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                            calcularVal(listJugador[j], jugador)
                                                                            db.collection("MinutoaMinuto")
                                                                                .document(idPartido).get()
                                                                                .addOnSuccessListener { it2 ->
                                                                                    val listRegistros =
                                                                                        it2.get("registro") as ArrayList<Map<String?, Any?>>
                                                                                    val registro = hashMapOf(
                                                                                        "cuarto" to cuarto,
                                                                                        "dorsal" to lista[i].text,
                                                                                        "nombre" to jugador["nombre"],
                                                                                        "frase" to "TRIPLE",
                                                                                        "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                                                        "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                                        "equipo" to "Local",
                                                                                        "tipoFrase" to "1",
                                                                                        "tipoImg" to "8",
                                                                                        "coordenada_x" to x,
                                                                                        "coordenada_y" to y
                                                                                    ) as Map<String?, Any?>
                                                                                    listRegistros.add(registro)
                                                                                    db.collection("MinutoaMinuto")
                                                                                        .document(idPartido)
                                                                                        .update(
                                                                                            hashMapOf(
                                                                                                "registro" to listRegistros,
                                                                                            ) as Map<String?, Any?>
                                                                                        ).addOnSuccessListener {
                                                                                            actualizaJugadaReciente()
                                                                                            dialog2.hide()
                                                                                        }
                                                                                }
                                                                        }

                                                                }
                                                            }
                                                            true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                            } else {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Visitante") {

                                                val builder2 = AlertDialog.Builder(binding.root.context)
                                                val view2 = layoutInflater.inflate(R.layout.accion_tiro, null)
                                                builder2.setView(view2)
                                                val dialog2 = builder2.create()
                                                dialog2.show()
                                                dialog.hide()

                                                view2.findViewById<ImageView>(R.id.pistaBaloncesto).viewTreeObserver.addOnGlobalLayoutListener {
                                                    if (view2.findViewById<ImageView>(R.id.pistaBaloncesto).width > 0 && view2.findViewById<ImageView>(
                                                            R.id.pistaBaloncesto
                                                        ).height > 0
                                                    ) {
                                                        view2.findViewById<ImageView>(R.id.pistaBaloncesto).setOnTouchListener { v, event ->
                                                            if (event.action == MotionEvent.ACTION_DOWN) {
                                                                convertirCoordenadas(
                                                                    event.x,
                                                                    event.y,
                                                                    view2.findViewById(R.id.pistaBaloncesto)
                                                                )?.let { (x, y) ->
                                                                    view2.findViewById<TiroView>(R.id.tiroView)
                                                                        .agregarTiro(x, y, true, jugador["equipo"].toString())

                                                                    val listTiros = jugador["tiros"] as ArrayList<Map<String, Any>>
                                                                    listTiros.add(
                                                                        hashMapOf(
                                                                            "x" to x,
                                                                            "y" to y,
                                                                            "cuarto" to cuarto,
                                                                            "encestado" to true
                                                                        )
                                                                    )
                                                                    jugador["tiros"] = listTiros
                                                                    jugador["tc3pA"] =
                                                                        jugador["tc3pA"].toString().toInt() + 1
                                                                    jugador["puntos"] =
                                                                        jugador["puntos"].toString().toInt() + 3
                                                                    binding.txtPuntosVisitante.text =
                                                                        (binding.txtPuntosVisitante.text.toString()
                                                                            .toInt() + 3).toString()

                                                                    actualizaResultado()

                                                                    db.collection("Estadisticas")
                                                                        .document(idPartido)
                                                                        .update(
                                                                            hashMapOf(
                                                                                listJugador[j] to jugador
                                                                            ) as Map<String, Any>
                                                                        ).addOnSuccessListener {
                                                                            Toast.makeText(
                                                                                binding.root.context,
                                                                                "Canasta del jugador " + lista[i].textOn + " de 3p",
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                            calcularVal(listJugador[j], jugador)
                                                                            db.collection("MinutoaMinuto")
                                                                                .document(idPartido).get()
                                                                                .addOnSuccessListener { it2 ->
                                                                                    val listRegistros =
                                                                                        it2.get("registro") as ArrayList<Map<String?, Any?>>
                                                                                    val registro = hashMapOf(
                                                                                        "cuarto" to cuarto,
                                                                                        "dorsal" to lista[i].text,
                                                                                        "nombre" to jugador["nombre"],
                                                                                        "frase" to "TRIPLE",
                                                                                        "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                                                        "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                                        "equipo" to "Visitante",
                                                                                        "tipoFrase" to "1",
                                                                                        "tipoImg" to "8",
                                                                                        "coordenada_x" to x,
                                                                                        "coordenada_y" to y
                                                                                    ) as Map<String?, Any?>
                                                                                    listRegistros.add(registro)
                                                                                    db.collection("MinutoaMinuto")
                                                                                        .document(idPartido)
                                                                                        .update(
                                                                                            hashMapOf(
                                                                                                "registro" to listRegistros,
                                                                                            ) as Map<String?, Any?>
                                                                                        ).addOnSuccessListener {
                                                                                            actualizaJugadaReciente()
                                                                                            dialog2.hide()
                                                                                        }
                                                                                }
                                                                        }

                                                                }
                                                            }
                                                            true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                            }

                        }
                    }
                    vaciarToggle(lista)
                }

                view.findViewById<Button>(R.id.btnFallar).setOnClickListener {

                    val lista = llenarListToggle()
                    for (i in 0..<lista.count()) {
                        if (lista[i].isChecked) {
                            if (lista[i].id == R.id.TBLocal1 || lista[i].id == R.id.TBLocal2 || lista[i].id == R.id.TBLocal3 || lista[i].id == R.id.TBLocal4 || lista[i].id == R.id.TBLocal5) {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Local") {

                                                val builder2 = AlertDialog.Builder(binding.root.context)
                                                val view2 = layoutInflater.inflate(R.layout.accion_tiro, null)
                                                builder2.setView(view2)
                                                val dialog2 = builder2.create()
                                                dialog2.show()
                                                dialog.hide()

                                                view2.findViewById<ImageView>(R.id.pistaBaloncesto).viewTreeObserver.addOnGlobalLayoutListener {
                                                    if (view2.findViewById<ImageView>(R.id.pistaBaloncesto).width > 0 && view2.findViewById<ImageView>(
                                                            R.id.pistaBaloncesto
                                                        ).height > 0
                                                    ) {
                                                        view2.findViewById<ImageView>(R.id.pistaBaloncesto).setOnTouchListener { v, event ->
                                                            if (event.action == MotionEvent.ACTION_DOWN) {
                                                                convertirCoordenadas(
                                                                    event.x,
                                                                    event.y,
                                                                    view2.findViewById(R.id.pistaBaloncesto)
                                                                )?.let { (x, y) ->
                                                                    view2.findViewById<TiroView>(R.id.tiroView)
                                                                        .agregarTiro(x, y, false, jugador["equipo"].toString())

                                                                    val listTiros = jugador["tiros"] as ArrayList<Map<String, Any>>
                                                                    listTiros.add(
                                                                        hashMapOf(
                                                                            "x" to x,
                                                                            "y" to y,
                                                                            "cuarto" to cuarto,
                                                                            "encestado" to false
                                                                        )
                                                                    )
                                                                    jugador["tiros"] = listTiros
                                                                    jugador["tc3pF"] =
                                                                        jugador["tc3pF"].toString().toInt() + 1
                                                                    db.collection("Estadisticas")
                                                                        .document(idPartido)
                                                                        .update(
                                                                            hashMapOf(
                                                                                listJugador[j] to jugador
                                                                            ) as Map<String, Any>
                                                                        ).addOnSuccessListener {
                                                                            Toast.makeText(
                                                                                binding.root.context,
                                                                                "Canasta fallada del jugador " + lista[i].textOn + " de 3p",
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                            calcularVal(listJugador[j], jugador)
                                                                        }

                                                                    db.collection("MinutoaMinuto").document(idPartido).get()
                                                                        .addOnSuccessListener { min ->
                                                                            val listRegistros =
                                                                                min.get("registro") as ArrayList<Map<String?, Any?>>
                                                                            val registro = hashMapOf(
                                                                                "cuarto" to cuarto,
                                                                                "dorsal" to lista[i].text,
                                                                                "nombre" to jugador["nombre"],
                                                                                "frase" to "TIRO DE 3 FALLADO",
                                                                                "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                                                "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                                "equipo" to "Local",
                                                                                "tipoFrase" to "3",
                                                                                "tipoImg" to "6",
                                                                                "coordenada_x" to x,
                                                                                "coordenada_y" to y
                                                                            ) as Map<String?, Any?>
                                                                            listRegistros.add(registro)
                                                                            db.collection("MinutoaMinuto")
                                                                                .document(idPartido)
                                                                                .update(
                                                                                    hashMapOf(
                                                                                        "registro" to listRegistros,
                                                                                    ) as Map<String?, Any?>
                                                                                ).addOnSuccessListener {
                                                                                    actualizaJugadaReciente()
                                                                                    dialog2.hide()
                                                                                }
                                                                        }
                                                                }
                                                            }
                                                            true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                            } else {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Visitante") {

                                                val builder2 = AlertDialog.Builder(binding.root.context)
                                                val view2 = layoutInflater.inflate(R.layout.accion_tiro, null)
                                                builder2.setView(view2)
                                                val dialog2 = builder2.create()
                                                dialog2.show()
                                                dialog.hide()

                                                view2.findViewById<ImageView>(R.id.pistaBaloncesto).viewTreeObserver.addOnGlobalLayoutListener {
                                                    if (view2.findViewById<ImageView>(R.id.pistaBaloncesto).width > 0 && view2.findViewById<ImageView>(
                                                            R.id.pistaBaloncesto
                                                        ).height > 0
                                                    ) {
                                                        view2.findViewById<ImageView>(R.id.pistaBaloncesto).setOnTouchListener { v, event ->
                                                            if (event.action == MotionEvent.ACTION_DOWN) {
                                                                convertirCoordenadas(
                                                                    event.x,
                                                                    event.y,
                                                                    view2.findViewById(R.id.pistaBaloncesto)
                                                                )?.let { (x, y) ->
                                                                    view2.findViewById<TiroView>(R.id.tiroView)
                                                                        .agregarTiro(x, y, false, jugador["equipo"].toString())

                                                                    val listTiros = jugador["tiros"] as ArrayList<Map<String, Any>>
                                                                    listTiros.add(
                                                                        hashMapOf(
                                                                            "x" to x,
                                                                            "y" to y,
                                                                            "cuarto" to cuarto,
                                                                            "encestado" to false
                                                                        )
                                                                    )
                                                                    jugador["tiros"] = listTiros
                                                                    jugador["tc3pF"] =
                                                                        jugador["tc3pF"].toString().toInt() + 1
                                                                    db.collection("Estadisticas")
                                                                        .document(idPartido)
                                                                        .update(
                                                                            hashMapOf(
                                                                                listJugador[j] to jugador
                                                                            ) as Map<String, Any>
                                                                        ).addOnSuccessListener {
                                                                            Toast.makeText(
                                                                                binding.root.context,
                                                                                "Canasta fallada del jugador " + lista[i].textOn + " de 3p",
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                            calcularVal(listJugador[j], jugador)
                                                                        }

                                                                    db.collection("MinutoaMinuto").document(idPartido).get()
                                                                        .addOnSuccessListener { min ->
                                                                            val listRegistros =
                                                                                min.get("registro") as ArrayList<Map<String?, Any?>>
                                                                            val registro = hashMapOf(
                                                                                "cuarto" to cuarto,
                                                                                "dorsal" to lista[i].text,
                                                                                "nombre" to jugador["nombre"],
                                                                                "frase" to "TIRO DE 3 FALLADO",
                                                                                "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                                                "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                                "equipo" to "Visitante",
                                                                                "tipoFrase" to "3",
                                                                                "tipoImg" to "6",
                                                                                "coordenada_x" to x,
                                                                                "coordenada_y" to y
                                                                            ) as Map<String?, Any?>
                                                                            listRegistros.add(registro)
                                                                            db.collection("MinutoaMinuto")
                                                                                .document(idPartido)
                                                                                .update(
                                                                                    hashMapOf(
                                                                                        "registro" to listRegistros,
                                                                                    ) as Map<String?, Any?>
                                                                                ).addOnSuccessListener {
                                                                                    actualizaJugadaReciente()
                                                                                    dialog2.hide()
                                                                                }
                                                                        }
                                                                }
                                                            }
                                                            true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                            }

                        }
                    }
                    vaciarToggle(lista)
                }

                actualizaTiempo()
            }
        }
        binding.btnPerdida.setOnClickListener {
            if (estado != "Finalizado") {
                val lista = llenarListToggle()
                for (i in 0..<lista.count()) {
                    if (lista[i].isChecked) {
                        if (lista[i].id == R.id.TBLocal1 || lista[i].id == R.id.TBLocal2 || lista[i].id == R.id.TBLocal3 || lista[i].id == R.id.TBLocal4 || lista[i].id == R.id.TBLocal5) {
                            db.collection("Estadisticas").document(idPartido).get()
                                .addOnSuccessListener {
                                    val listJugador =
                                        it.get("ListadoJugadores") as ArrayList<String>
                                    for (j in 0..<listJugador.count()) {
                                        val jugador =
                                            (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                        if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Local") {
                                            jugador["per"] = jugador["per"].toString().toInt() + 1
                                            db.collection("Estadisticas")
                                                .document(idPartido)
                                                .update(
                                                    hashMapOf(
                                                        listJugador[j] to jugador
                                                    ) as Map<String, Any>
                                                ).addOnSuccessListener {
                                                    Toast.makeText(
                                                        binding.root.context,
                                                        "Pérdida del jugador " + lista[i].textOn,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    calcularVal(listJugador[j], jugador)
                                                }

                                            db.collection("MinutoaMinuto").document(idPartido).get()
                                                .addOnSuccessListener { min ->
                                                    val listRegistros =
                                                        min.get("registro") as ArrayList<Map<String?, Any?>>
                                                    val registro = hashMapOf(
                                                        "cuarto" to cuarto,
                                                        "dorsal" to lista[i].text,
                                                        "nombre" to jugador["nombre"],
                                                        "frase" to "PÉRDIDA",
                                                        "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                        "tiempo" to binding.TiempoCuarto.text.toString(),
                                                        "equipo" to "Local",
                                                        "tipoFrase" to "3",
                                                        "tipoImg" to "10"
                                                    ) as Map<String?, Any?>
                                                    listRegistros.add(registro)
                                                    db.collection("MinutoaMinuto").document(idPartido)
                                                        .update(
                                                            hashMapOf(
                                                                "registro" to listRegistros,
                                                            ) as Map<String?, Any?>
                                                        ).addOnSuccessListener {
                                                            actualizaJugadaReciente()
                                                        }
                                                }
                                        }
                                    }

                                }


                        } else {
                            db.collection("Estadisticas").document(idPartido).get()
                                .addOnSuccessListener {
                                    val listJugador =
                                        it.get("ListadoJugadores") as ArrayList<String>
                                    for (j in 0..<listJugador.count()) {
                                        val jugador =
                                            (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                        if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Visitante") {
                                            jugador["per"] = jugador["per"].toString().toInt() + 1
                                            db.collection("Estadisticas")
                                                .document(idPartido)
                                                .update(
                                                    hashMapOf(
                                                        listJugador[j] to jugador
                                                    ) as Map<String, Any>
                                                ).addOnSuccessListener {
                                                    Toast.makeText(
                                                        binding.root.context,
                                                        "Pérdida del jugador " + lista[i].textOn,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    calcularVal(listJugador[j], jugador)
                                                }

                                            db.collection("MinutoaMinuto").document(idPartido).get()
                                                .addOnSuccessListener { min ->
                                                    val listRegistros =
                                                        min.get("registro") as ArrayList<Map<String?, Any?>>
                                                    val registro = hashMapOf(
                                                        "cuarto" to cuarto,
                                                        "dorsal" to lista[i].text,
                                                        "nombre" to jugador["nombre"],
                                                        "frase" to "PÉRDIDA",
                                                        "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                        "tiempo" to binding.TiempoCuarto.text.toString(),
                                                        "equipo" to "Visitante",
                                                        "tipoFrase" to "3",
                                                        "tipoImg" to "10"
                                                    ) as Map<String?, Any?>
                                                    listRegistros.add(registro)
                                                    db.collection("MinutoaMinuto").document(idPartido)
                                                        .update(
                                                            hashMapOf(
                                                                "registro" to listRegistros,
                                                            ) as Map<String?, Any?>
                                                        ).addOnSuccessListener {
                                                            actualizaJugadaReciente()
                                                        }
                                                }
                                        }
                                    }

                                }

                        }

                    }
                }
                vaciarToggle(lista)
                actualizaTiempo()
            }
        }
        binding.btnRecuperacion.setOnClickListener {
            if (estado != "Finalizado") {
                val lista = llenarListToggle()
                for (i in 0..<lista.count()) {
                    if (lista[i].isChecked) {
                        if (lista[i].id == R.id.TBLocal1 || lista[i].id == R.id.TBLocal2 || lista[i].id == R.id.TBLocal3 || lista[i].id == R.id.TBLocal4 || lista[i].id == R.id.TBLocal5) {
                            db.collection("Estadisticas").document(idPartido).get()
                                .addOnSuccessListener {
                                    val listJugador =
                                        it.get("ListadoJugadores") as ArrayList<String>
                                    for (j in 0..<listJugador.count()) {
                                        val jugador =
                                            (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                        if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Local") {
                                            jugador["recu"] = jugador["recu"].toString().toInt() + 1
                                            db.collection("Estadisticas")
                                                .document(idPartido)
                                                .update(
                                                    hashMapOf(
                                                        listJugador[j] to jugador
                                                    ) as Map<String, Any>
                                                ).addOnSuccessListener {
                                                    Toast.makeText(
                                                        binding.root.context,
                                                        "Recuperación del jugador " + lista[i].textOn,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    calcularVal(listJugador[j], jugador)
                                                }

                                            db.collection("MinutoaMinuto").document(idPartido).get()
                                                .addOnSuccessListener { min ->
                                                    val listRegistros =
                                                        min.get("registro") as ArrayList<Map<String?, Any?>>
                                                    val registro = hashMapOf(
                                                        "cuarto" to cuarto,
                                                        "dorsal" to lista[i].text,
                                                        "nombre" to jugador["nombre"],
                                                        "frase" to "RECUPERACIÓN",
                                                        "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                        "tiempo" to binding.TiempoCuarto.text.toString(),
                                                        "equipo" to "Local",
                                                        "tipoFrase" to "3",
                                                        "tipoImg" to "11"
                                                    ) as Map<String?, Any?>
                                                    listRegistros.add(registro)
                                                    db.collection("MinutoaMinuto").document(idPartido)
                                                        .update(
                                                            hashMapOf(
                                                                "registro" to listRegistros,
                                                            ) as Map<String?, Any?>
                                                        ).addOnSuccessListener {
                                                            actualizaJugadaReciente()
                                                        }
                                                }
                                        }
                                    }

                                }

                        } else {
                            db.collection("Estadisticas").document(idPartido).get()
                                .addOnSuccessListener {
                                    val listJugador =
                                        it.get("ListadoJugadores") as ArrayList<String>
                                    for (j in 0..<listJugador.count()) {
                                        val jugador =
                                            (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                        if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Visitante") {
                                            jugador["recu"] = jugador["recu"].toString().toInt() + 1
                                            db.collection("Estadisticas")
                                                .document(idPartido)
                                                .update(
                                                    hashMapOf(
                                                        listJugador[j] to jugador
                                                    ) as Map<String, Any>
                                                ).addOnSuccessListener {
                                                    Toast.makeText(
                                                        binding.root.context,
                                                        "Recuperación del jugador " + lista[i].textOn,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    calcularVal(listJugador[j], jugador)
                                                }

                                            db.collection("MinutoaMinuto").document(idPartido).get()
                                                .addOnSuccessListener { min ->
                                                    val listRegistros =
                                                        min.get("registro") as ArrayList<Map<String?, Any?>>
                                                    val registro = hashMapOf(
                                                        "cuarto" to cuarto,
                                                        "dorsal" to lista[i].text,
                                                        "nombre" to jugador["nombre"],
                                                        "frase" to "RECUPERACIÓN",
                                                        "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                        "tiempo" to binding.TiempoCuarto.text.toString(),
                                                        "equipo" to "Visitante",
                                                        "tipoFrase" to "3",
                                                        "tipoImg" to "11"
                                                    ) as Map<String?, Any?>
                                                    listRegistros.add(registro)
                                                    db.collection("MinutoaMinuto").document(idPartido)
                                                        .update(
                                                            hashMapOf(
                                                                "registro" to listRegistros,
                                                            ) as Map<String?, Any?>
                                                        ).addOnSuccessListener {
                                                            actualizaJugadaReciente()
                                                        }
                                                }
                                        }
                                    }

                                }

                        }

                    }
                }
                vaciarToggle(lista)
                actualizaTiempo()
            }
        }
        binding.btnTapon.setOnClickListener {
            if (estado != "Finalizado") {
                val builder = AlertDialog.Builder(binding.root.context)
                val view = layoutInflater.inflate(R.layout.accion_partido, null)
                builder.setView(view)
                view.findViewById<Button>(R.id.btnAnotar).text = "Recibido"
                view.findViewById<Button>(R.id.btnFallar).text = "Cometido"
                val dialog = builder.create()
                dialog.show()

                view.findViewById<Button>(R.id.btnAnotar).setOnClickListener {
                    val lista = llenarListToggle()
                    for (i in 0..<lista.count()) {
                        if (lista[i].isChecked) {
                            if (lista[i].id == R.id.TBLocal1 || lista[i].id == R.id.TBLocal2 || lista[i].id == R.id.TBLocal3 || lista[i].id == R.id.TBLocal4 || lista[i].id == R.id.TBLocal5) {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Local") {
                                                jugador["taRec"] =
                                                    jugador["taRec"].toString().toInt() + 1
                                                db.collection("Estadisticas")
                                                    .document(idPartido)
                                                    .update(
                                                        hashMapOf(
                                                            listJugador[j] to jugador
                                                        ) as Map<String, Any>
                                                    ).addOnSuccessListener {
                                                        Toast.makeText(
                                                            binding.root.context,
                                                            "Tapón Recibido del jugador " + lista[i].textOn,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        calcularVal(listJugador[j], jugador)
                                                    }

                                                db.collection("MinutoaMinuto").document(idPartido).get()
                                                    .addOnSuccessListener { min ->
                                                        val listRegistros =
                                                            min.get("registro") as ArrayList<Map<String?, Any?>>
                                                        val registro = hashMapOf(
                                                            "cuarto" to cuarto,
                                                            "dorsal" to lista[i].text,
                                                            "nombre" to jugador["nombre"],
                                                            "frase" to "TAPÓN RECIBIDO",
                                                            "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                            "tiempo" to binding.TiempoCuarto.text.toString(),
                                                            "equipo" to "Local",
                                                            "tipoFrase" to "3",
                                                            "tipoImg" to "12"
                                                        ) as Map<String?, Any?>
                                                        listRegistros.add(registro)
                                                        db.collection("MinutoaMinuto")
                                                            .document(idPartido)
                                                            .update(
                                                                hashMapOf(
                                                                    "registro" to listRegistros,
                                                                ) as Map<String?, Any?>
                                                            ).addOnSuccessListener {
                                                                actualizaJugadaReciente()
                                                            }
                                                    }
                                            }
                                        }

                                    }

                            } else {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Visitante") {
                                                jugador["taRec"] =
                                                    jugador["taRec"].toString().toInt() + 1
                                                db.collection("Estadisticas")
                                                    .document(idPartido)
                                                    .update(
                                                        hashMapOf(
                                                            listJugador[j] to jugador
                                                        ) as Map<String, Any>
                                                    ).addOnSuccessListener {
                                                        Toast.makeText(
                                                            binding.root.context,
                                                            "Tapón Recibido del jugador " + lista[i].textOn,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        calcularVal(listJugador[j], jugador)
                                                    }

                                                db.collection("MinutoaMinuto").document(idPartido).get()
                                                    .addOnSuccessListener { min ->
                                                        val listRegistros =
                                                            min.get("registro") as ArrayList<Map<String?, Any?>>
                                                        val registro = hashMapOf(
                                                            "cuarto" to cuarto,
                                                            "dorsal" to lista[i].text,
                                                            "nombre" to jugador["nombre"],
                                                            "frase" to "TAPÓN RECIBIDO",
                                                            "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                            "tiempo" to binding.TiempoCuarto.text.toString(),
                                                            "equipo" to "Visitante",
                                                            "tipoFrase" to "3",
                                                            "tipoImg" to "12"
                                                        ) as Map<String?, Any?>
                                                        listRegistros.add(registro)
                                                        db.collection("MinutoaMinuto")
                                                            .document(idPartido)
                                                            .update(
                                                                hashMapOf(
                                                                    "registro" to listRegistros,
                                                                ) as Map<String?, Any?>
                                                            ).addOnSuccessListener {
                                                                actualizaJugadaReciente()
                                                            }
                                                    }
                                            }
                                        }

                                    }

                            }

                        }
                    }
                    vaciarToggle(lista)
                    dialog.hide()
                }

                view.findViewById<Button>(R.id.btnFallar).setOnClickListener {
                    val lista = llenarListToggle()
                    for (i in 0..<lista.count()) {
                        if (lista[i].isChecked) {
                            if (lista[i].id == R.id.TBLocal1 || lista[i].id == R.id.TBLocal2 || lista[i].id == R.id.TBLocal3 || lista[i].id == R.id.TBLocal4 || lista[i].id == R.id.TBLocal5) {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Local") {
                                                jugador["taCom"] =
                                                    jugador["taCom"].toString().toInt() + 1
                                                db.collection("Estadisticas")
                                                    .document(idPartido)
                                                    .update(
                                                        hashMapOf(
                                                            listJugador[j] to jugador
                                                        ) as Map<String, Any>
                                                    ).addOnSuccessListener {
                                                        Toast.makeText(
                                                            binding.root.context,
                                                            "Tapón Cometido del jugador " + lista[i].textOn,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        calcularVal(listJugador[j], jugador)
                                                    }

                                                db.collection("MinutoaMinuto").document(idPartido).get()
                                                    .addOnSuccessListener { min ->
                                                        val listRegistros =
                                                            min.get("registro") as ArrayList<Map<String?, Any?>>
                                                        val registro = hashMapOf(
                                                            "cuarto" to cuarto,
                                                            "dorsal" to lista[i].text,
                                                            "nombre" to jugador["nombre"],
                                                            "frase" to "TAPÓN COMETIDO",
                                                            "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                            "tiempo" to binding.TiempoCuarto.text.toString(),
                                                            "equipo" to "Local",
                                                            "tipoFrase" to "3",
                                                            "tipoImg" to "13"
                                                        ) as Map<String?, Any?>
                                                        listRegistros.add(registro)
                                                        db.collection("MinutoaMinuto")
                                                            .document(idPartido)
                                                            .update(
                                                                hashMapOf(
                                                                    "registro" to listRegistros,
                                                                ) as Map<String?, Any?>
                                                            ).addOnSuccessListener {
                                                                actualizaJugadaReciente()
                                                            }
                                                    }
                                            }
                                        }

                                    }

                            } else {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Visitante") {
                                                jugador["taCom"] =
                                                    jugador["taCom"].toString().toInt() + 1
                                                db.collection("Estadisticas")
                                                    .document(idPartido)
                                                    .update(
                                                        hashMapOf(
                                                            listJugador[j] to jugador
                                                        ) as Map<String, Any>
                                                    ).addOnSuccessListener {
                                                        Toast.makeText(
                                                            binding.root.context,
                                                            "Tapón Cometido del jugador " + lista[i].textOn,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        calcularVal(listJugador[j], jugador)
                                                    }

                                                db.collection("MinutoaMinuto").document(idPartido).get()
                                                    .addOnSuccessListener { min ->
                                                        val listRegistros =
                                                            min.get("registro") as ArrayList<Map<String?, Any?>>
                                                        val registro = hashMapOf(
                                                            "cuarto" to cuarto,
                                                            "dorsal" to lista[i].text,
                                                            "nombre" to jugador["nombre"],
                                                            "frase" to "TAPÓN COMETIDO",
                                                            "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                            "tiempo" to binding.TiempoCuarto.text.toString(),
                                                            "equipo" to "Visitante",
                                                            "tipoFrase" to "3",
                                                            "tipoImg" to "13"
                                                        ) as Map<String?, Any?>
                                                        listRegistros.add(registro)
                                                        db.collection("MinutoaMinuto")
                                                            .document(idPartido)
                                                            .update(
                                                                hashMapOf(
                                                                    "registro" to listRegistros,
                                                                ) as Map<String?, Any?>
                                                            ).addOnSuccessListener {
                                                                actualizaJugadaReciente()
                                                            }
                                                    }
                                            }
                                        }

                                    }

                            }

                        }
                    }
                    vaciarToggle(lista)
                    dialog.hide()
                }
                actualizaTiempo()
            }

        }
        binding.btnRebote.setOnClickListener {
            if (estado != "Finalizado") {
                val builder = AlertDialog.Builder(binding.root.context)
                val view = layoutInflater.inflate(R.layout.accion_partido, null)
                builder.setView(view)
                view.findViewById<Button>(R.id.btnAnotar).text = "Ofensivo"
                view.findViewById<Button>(R.id.btnFallar).text = "Defensivo"
                val dialog = builder.create()
                dialog.show()

                view.findViewById<Button>(R.id.btnAnotar).setOnClickListener {

                    val lista = llenarListToggle()
                    for (i in 0..<lista.count()) {
                        if (lista[i].isChecked) {
                            if (lista[i].id == R.id.TBLocal1 || lista[i].id == R.id.TBLocal2 || lista[i].id == R.id.TBLocal3 || lista[i].id == R.id.TBLocal4 || lista[i].id == R.id.TBLocal5) {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Local") {
                                                jugador["rebO"] =
                                                    jugador["rebO"].toString().toInt() + 1
                                                db.collection("Estadisticas")
                                                    .document(idPartido)
                                                    .update(
                                                        hashMapOf(
                                                            listJugador[j] to jugador
                                                        ) as Map<String, Any>
                                                    ).addOnSuccessListener {
                                                        Toast.makeText(
                                                            binding.root.context,
                                                            "Rebote Ofensivo del jugador " + lista[i].textOn,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        calcularVal(listJugador[j], jugador)
                                                    }

                                                db.collection("MinutoaMinuto").document(idPartido).get()
                                                    .addOnSuccessListener { min ->
                                                        val listRegistros =
                                                            min.get("registro") as ArrayList<Map<String?, Any?>>
                                                        val registro = hashMapOf(
                                                            "cuarto" to cuarto,
                                                            "dorsal" to lista[i].text,
                                                            "nombre" to jugador["nombre"],
                                                            "frase" to "REBOTE OFENSIVO",
                                                            "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                            "tiempo" to binding.TiempoCuarto.text.toString(),
                                                            "equipo" to "Local",
                                                            "tipoFrase" to "3",
                                                            "tipoImg" to "14"
                                                        ) as Map<String?, Any?>
                                                        listRegistros.add(registro)
                                                        db.collection("MinutoaMinuto")
                                                            .document(idPartido)
                                                            .update(
                                                                hashMapOf(
                                                                    "registro" to listRegistros,
                                                                ) as Map<String?, Any?>
                                                            ).addOnSuccessListener {
                                                                actualizaJugadaReciente()
                                                            }
                                                    }
                                            }
                                        }

                                    }

                            } else {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Visitante") {
                                                jugador["rebO"] =
                                                    jugador["rebO"].toString().toInt() + 1
                                                db.collection("Estadisticas")
                                                    .document(idPartido)
                                                    .update(
                                                        hashMapOf(
                                                            listJugador[j] to jugador
                                                        ) as Map<String, Any>
                                                    ).addOnSuccessListener {
                                                        Toast.makeText(
                                                            binding.root.context,
                                                            "Rebote Ofensivo del jugador " + lista[i].textOn,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        calcularVal(listJugador[j], jugador)
                                                    }

                                                db.collection("MinutoaMinuto").document(idPartido).get()
                                                    .addOnSuccessListener { min ->
                                                        val listRegistros =
                                                            min.get("registro") as ArrayList<Map<String?, Any?>>
                                                        val registro = hashMapOf(
                                                            "cuarto" to cuarto,
                                                            "dorsal" to lista[i].text,
                                                            "nombre" to jugador["nombre"],
                                                            "frase" to "REBOTE OFENSIVO",
                                                            "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                            "tiempo" to binding.TiempoCuarto.text.toString(),
                                                            "equipo" to "Visitante",
                                                            "tipoFrase" to "3",
                                                            "tipoImg" to "14"
                                                        ) as Map<String?, Any?>
                                                        listRegistros.add(registro)
                                                        db.collection("MinutoaMinuto")
                                                            .document(idPartido)
                                                            .update(
                                                                hashMapOf(
                                                                    "registro" to listRegistros,
                                                                ) as Map<String?, Any?>
                                                            ).addOnSuccessListener {
                                                                actualizaJugadaReciente()
                                                            }
                                                    }
                                            }
                                        }

                                    }

                            }

                        }
                    }
                    vaciarToggle(lista)
                    dialog.hide()
                }

                view.findViewById<Button>(R.id.btnFallar).setOnClickListener {

                    val lista = llenarListToggle()
                    for (i in 0..<lista.count()) {
                        if (lista[i].isChecked) {
                            if (lista[i].id == R.id.TBLocal1 || lista[i].id == R.id.TBLocal2 || lista[i].id == R.id.TBLocal3 || lista[i].id == R.id.TBLocal4 || lista[i].id == R.id.TBLocal5) {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Local") {
                                                jugador["rebD"] =
                                                    jugador["rebD"].toString().toInt() + 1
                                                db.collection("Estadisticas")
                                                    .document(idPartido)
                                                    .update(
                                                        hashMapOf(
                                                            listJugador[j] to jugador
                                                        ) as Map<String, Any>
                                                    ).addOnSuccessListener {
                                                        Toast.makeText(
                                                            binding.root.context,
                                                            "Rebote Defensivo del jugador " + lista[i].textOn,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        calcularVal(listJugador[j], jugador)
                                                    }

                                                db.collection("MinutoaMinuto").document(idPartido).get()
                                                    .addOnSuccessListener { min ->
                                                        val listRegistros =
                                                            min.get("registro") as ArrayList<Map<String?, Any?>>
                                                        val registro = hashMapOf(
                                                            "cuarto" to cuarto,
                                                            "dorsal" to lista[i].text,
                                                            "nombre" to jugador["nombre"],
                                                            "frase" to "REBOTE DEFENSIVO",
                                                            "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                            "tiempo" to binding.TiempoCuarto.text.toString(),
                                                            "equipo" to "Local",
                                                            "tipoFrase" to "3",
                                                            "tipoImg" to "14"
                                                        ) as Map<String?, Any?>
                                                        listRegistros.add(registro)
                                                        db.collection("MinutoaMinuto")
                                                            .document(idPartido)
                                                            .update(
                                                                hashMapOf(
                                                                    "registro" to listRegistros,
                                                                ) as Map<String?, Any?>
                                                            ).addOnSuccessListener {
                                                                actualizaJugadaReciente()
                                                            }
                                                    }
                                            }
                                        }

                                    }

                            } else {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Visitante") {
                                                jugador["rebD"] =
                                                    jugador["rebD"].toString().toInt() + 1
                                                db.collection("Estadisticas")
                                                    .document(idPartido)
                                                    .update(
                                                        hashMapOf(
                                                            listJugador[j] to jugador
                                                        ) as Map<String, Any>
                                                    ).addOnSuccessListener {
                                                        Toast.makeText(
                                                            binding.root.context,
                                                            "Rebote Defensivo del jugador " + lista[i].textOn,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        calcularVal(listJugador[j], jugador)
                                                    }

                                                db.collection("MinutoaMinuto").document(idPartido).get()
                                                    .addOnSuccessListener { min ->
                                                        val listRegistros =
                                                            min.get("registro") as ArrayList<Map<String?, Any?>>
                                                        val registro = hashMapOf(
                                                            "cuarto" to cuarto,
                                                            "dorsal" to lista[i].text,
                                                            "nombre" to jugador["nombre"],
                                                            "frase" to "REBOTE DEFENSIVO",
                                                            "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                            "tiempo" to binding.TiempoCuarto.text.toString(),
                                                            "equipo" to "Visitante",
                                                            "tipoFrase" to "3",
                                                            "tipoImg" to "14"
                                                        ) as Map<String?, Any?>
                                                        listRegistros.add(registro)
                                                        db.collection("MinutoaMinuto")
                                                            .document(idPartido)
                                                            .update(
                                                                hashMapOf(
                                                                    "registro" to listRegistros,
                                                                ) as Map<String?, Any?>
                                                            ).addOnSuccessListener {
                                                                actualizaJugadaReciente()
                                                            }
                                                    }
                                            }
                                        }

                                    }

                            }

                        }
                    }
                    vaciarToggle(lista)
                    dialog.hide()
                }
                actualizaTiempo()
            }
        }

        return root
    }

    fun calcularDimensionesImagen(imageView: ImageView): RectF {
        val drawable = imageView.drawable ?: return RectF(0f, 0f, 0f, 0f)

        val imageWidth = drawable.intrinsicWidth.toFloat()
        val imageHeight = drawable.intrinsicHeight.toFloat()
        val viewWidth = imageView.width.toFloat()
        val viewHeight = imageView.height.toFloat()

        if (imageWidth == 0f || imageHeight == 0f || viewWidth == 0f || viewHeight == 0f) {
            return RectF(0f, 0f, 0f, 0f)
        }

        val scale = max(viewWidth / imageWidth, viewHeight / imageHeight)
        val scaledWidth = imageWidth * scale
        val scaledHeight = imageHeight * scale

        val offsetX = (viewWidth - scaledWidth) / 2
        val offsetY = (viewHeight - scaledHeight) / 2

        return RectF(offsetX, offsetY, offsetX + scaledWidth, offsetY + scaledHeight)
    }

    private fun convertirCoordenadas(x: Float, y: Float, imageView: ImageView): Pair<Float, Float>? {
        val rect = calcularDimensionesImagen(imageView)
        if (rect.width() == 0f || rect.height() == 0f) {
            return null
        }
        if (x < rect.left || x > rect.right || y < rect.top || y > rect.bottom) {
            return null
        }
        val escalaX = (x - rect.left) / rect.width()
        val escalaY = (y - rect.top) / rect.height()

        return Pair(escalaX, escalaY)
    }

    @SuppressLint("SetTextI18n")
    private fun recuperaInfo() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()

        db.collection("Partidos").document(idPartido).get()
            .addOnSuccessListener {
                tiempo = it.get("Tiempo").toString()
                cuarto = it.get("Cuarto").toString().toInt()
                falL = it.get("FaltaL").toString().toInt()
                falV = it.get("FaltaV").toString().toInt()
                tmL = it.get("TiempoML").toString().toInt()
                tmV = it.get("TiempoMV").toString().toInt()
                estado = it.get("Estado").toString()
                resultado = it.get("Resultado").toString()
                quintetoL = it.get("QuintetoL") as ArrayList<String>
                quintetoV = it.get("QuintetoV") as ArrayList<String>

                binding.txtFaltasLocal.text = falL.toString()
                binding.txtFaltasVisitante.text = falV.toString()
                binding.txtCuartoPartido.text = "Cuarto $cuarto"
                binding.txtTiemposMLocal.text = tmL.toString()
                binding.txtTiemposMVisitante.text = tmV.toString()
                binding.TiempoCuarto.text = tiempo
                pauseOffSet = tiempo.split(":")[0].toLong() * 60 * 1000
                pauseOffSet += tiempo.split(":")[1].toLong() * 1000
                binding.TiempoCuarto.base = SystemClock.elapsedRealtime() + pauseOffSet
                binding.txtPuntosLocal.text = resultado.split(" - ")[0]
                binding.txtPuntosVisitante.text = resultado.split(" - ")[1]
                colocarQuinteto("Local", llenarListToggleLocal())
                colocarQuinteto("Visitante", llenarListToggleVisitante())
                actualizaJugadaReciente()
            }
    }

    private fun actualizaTiempo() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()
        tiempo = binding.TiempoCuarto.text.toString()
        db.collection("Partidos")
            .document(idPartido)
            .update(
                hashMapOf(
                    "Tiempo" to tiempo
                ) as Map<String, Any>
            )
    }

    private fun actualizarMinutosJugadores() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()
        val minutoActual = tiempo // Ahora es un String en formato MM:SS

        db.collection("Estadisticas").document(idPartido).get()
            .addOnSuccessListener { documentSnapshot ->
                val listJugador = documentSnapshot.get("ListadoJugadores") as ArrayList<String>
                for (j in listJugador) {
                    val jugador = (documentSnapshot.get(j) as Map<String?, Any?>).toMutableMap()

                    val equipo = jugador["equipo"].toString()
                    val quinteto = if (equipo == "Local") quintetoL else quintetoV

                    if (jugador["dorsal"].toString() in quinteto) {
                        val minutoEntrada = jugador["minutoEntrada"].toString() // Formato MM:SS
                        val totalMinutos = jugador["minutos"].toString()
                        val cuartoEntrada = jugador["cuartoEntrada"].toString().toInt()

                        // Calcular tiempo jugado
                        val tiempoJugado = if (cuarto == cuartoEntrada) {
                            restarTiempo(minutoEntrada, minutoActual) // Nueva función para restar tiempos
                        } else {
                            minutoEntrada // Si cambió el cuarto, cuenta to do el tiempo hasta 00:00
                        }

                        // Sumar tiempo jugado al total
                        jugador["minutos"] = sumarTiempos(totalMinutos, tiempoJugado)

                        // Guardar cambios en Firestore
                        db.collection("Estadisticas").document(idPartido)
                            .update(j, jugador)
                            .addOnSuccessListener {
                                // Actualizar minuto y cuarto de entrada para el siguiente cálculo
                                jugador["minutoEntrada"] = minutoActual
                                jugador["cuartoEntrada"] = cuarto
                                db.collection("Estadisticas").document(idPartido)
                                    .update(j, jugador)
                            }
                    }
                }
            }
    }

    // Función para restar tiempos en formato MM:SS
    private fun restarTiempo(inicio: String, fin: String): String {
        val (minI, segI) = inicio.split(":").map { it.toInt() }
        val (minF, segF) = fin.split(":").map { it.toInt() }

        val totalSegundosI = minI * 60 + segI
        val totalSegundosF = minF * 60 + segF

        val diferencia = maxOf(totalSegundosI - totalSegundosF, 0)
        val minutos = diferencia / 60
        val segundos = diferencia % 60

        return String.format("%02d:%02d", minutos, segundos)
    }

    // Función para sumar tiempos en formato MM:SS
    private fun sumarTiempos(tiempo1: String, tiempo2: String): String {
        val (min1, seg1) = tiempo1.split(":").map { it.toInt() }
        val (min2, seg2) = tiempo2.split(":").map { it.toInt() }

        val totalSegundos = (min1 * 60 + seg1) + (min2 * 60 + seg2)
        val minutos = totalSegundos / 60
        val segundos = totalSegundos % 60

        return String.format("%02d:%02d", minutos, segundos)
    }

    private fun actualizaTiempoMuertos() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()

        binding.txtTiemposMLocal.text = tmL.toString()
        binding.txtTiemposMVisitante.text = tmV.toString()
        db.collection("Partidos").document(idPartido).update(
            hashMapOf(
                "TiempoML" to tmL,
                "TiempoMV" to tmV
            ) as Map<String?, Any?>
        )
    }

    private fun actualizaResultado() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()
        db.collection("Partidos")
            .document(idPartido)
            .update(
                hashMapOf(
                    "Resultado" to binding.txtPuntosLocal.text.toString() + " - " + binding.txtPuntosVisitante.text.toString(),
                ) as Map<String, Any>
            )
    }

    private fun actualizaFaltaEquipo(equipo: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()
        if (equipo == "FaltaL") {
            binding.txtFaltasLocal.text = falL.toString()
            db.collection("Partidos")
                .document(idPartido)
                .update(
                    hashMapOf(
                        equipo to falL,
                    ) as Map<String, Any>
                )
        } else {
            binding.txtFaltasVisitante.text = falV.toString()
            db.collection("Partidos")
                .document(idPartido)
                .update(
                    hashMapOf(
                        equipo to falV,
                    ) as Map<String, Any>
                )
        }

    }

    private fun play() {
        if (!isPlay && estado != "Finalizado") {
            binding.TiempoCuarto.base = SystemClock.elapsedRealtime() + pauseOffSet
            binding.TiempoCuarto.setTextColor(Color.BLACK)
            binding.TiempoCuarto.start()
            isPlay = true
        } else {
            val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
            val idPartido = prefs.getString("idPartido", "").toString()
            binding.TiempoCuarto.stop()
            binding.TiempoCuarto.setTextColor(Color.RED)
            pauseOffSet = -1 * (SystemClock.elapsedRealtime() - binding.TiempoCuarto.base)
            isPlay = false

            tiempo = binding.TiempoCuarto.text.toString()
            db.collection("Partidos").document(idPartido).update(
                hashMapOf(
                    "Tiempo" to tiempo
                ) as Map<String?, Any?>
            )
            actualizarMinutosJugadores()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun gestionCrononometro(chronometer: Chronometer) {
        if (chronometer.text.toString() == "00:00" && estado != "Finalizado") {
            val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
            val idPartido = prefs.getString("idPartido", "").toString()

            val horaActual = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))

            if (cuarto == 1 || cuarto == 2 || cuarto == 3) {
                falL = 0
                falV = 0
                if (cuarto == 2) {
                    tmL = 3
                    tmV = 3
                    binding.txtTiemposMLocal.text = tmL.toString()
                    binding.txtTiemposMVisitante.text = tmV.toString()
                }
                db.collection("MinutoaMinuto").document(idPartido).get()
                    .addOnSuccessListener { it2 ->
                        val listRegistros =
                            it2.get("registro") as ArrayList<Map<String?, Any?>>

                        val registro = hashMapOf(
                            "cuarto" to cuarto,
                            "dorsal" to "",
                            "frase" to "FIN DEL PERIODO",
                            "resultado" to "",
                            "tiempo" to horaActual.toString(),
                            "equipo" to "",
                            "tipoFrase" to "4"
                        ) as Map<String?, Any?>
                        listRegistros.add(registro)

                        val registro2 = hashMapOf(
                            "cuarto" to cuarto + 1,
                            "dorsal" to "",
                            "frase" to "INICIO DEL PERIODO",
                            "resultado" to "",
                            "tiempo" to horaActual.toString(),
                            "equipo" to "",
                            "tipoFrase" to "2"
                        ) as Map<String?, Any?>
                        listRegistros.add(registro2)

                        db.collection("MinutoaMinuto")
                            .document(idPartido)
                            .update(
                                hashMapOf(
                                    "registro" to listRegistros,
                                ) as Map<String?, Any?>
                            ).addOnSuccessListener {
                                binding.TiempoCuarto.stop()
                                cuarto++
                                binding.txtFaltasLocal.text = falL.toString()
                                binding.txtFaltasVisitante.text = falV.toString()
                                binding.txtCuartoPartido.text = "Cuarto $cuarto"
                                binding.TiempoCuarto.setTextColor(Color.RED)
                                tiempo = "10:00"
                                binding.TiempoCuarto.text = tiempo
                                isPlay = false
                                pauseOffSet = 10 * 60 * 1000
                                actualizarMinutosJugadores()
                                db.collection("Partidos").document(idPartido).update(
                                    hashMapOf(
                                        "TiempoML" to tmL,
                                        "TiempoMV" to tmV,
                                        "FaltaL" to falL,
                                        "FaltaV" to falV,
                                        "Cuarto" to cuarto,
                                        "Tiempo" to tiempo
                                    ) as Map<String?, Any?>
                                )
                                mostrarJugadoresCampo()
                            }
                    }

            } else if (cuarto == 4 && binding.txtPuntosLocal.text != binding.txtPuntosVisitante.text) {
                binding.TiempoCuarto.stop()

                db.collection("MinutoaMinuto").document(idPartido).get()
                    .addOnSuccessListener { it2 ->
                        val listRegistros = it2.get("registro") as ArrayList<Map<String?, Any?>>
                        val registro = hashMapOf(
                            "cuarto" to cuarto,
                            "dorsal" to "",
                            "frase" to "FIN DEL PERIODO",
                            "resultado" to "",
                            "tiempo" to horaActual.toString(),
                            "equipo" to "",
                            "tipoFrase" to "4"
                        ) as Map<String?, Any?>
                        listRegistros.add(registro)

                        val registro2 = hashMapOf(
                            "cuarto" to "",
                            "dorsal" to "",
                            "frase" to "FIN DEL PARTIDO",
                            "resultado" to "",
                            "tiempo" to horaActual.toString(),
                            "equipo" to "",
                            "tipoFrase" to "4"
                        ) as Map<String?, Any?>
                        listRegistros.add(registro2)

                        db.collection("MinutoaMinuto")
                            .document(idPartido)
                            .update(
                                hashMapOf(
                                    "registro" to listRegistros,
                                ) as Map<String?, Any?>
                            )
                        binding.TiempoCuarto.setTextColor(Color.RED)
                        tiempo = "00:00"
                        binding.TiempoCuarto.text = tiempo
                        isPlay = false
                        pauseOffSet = 0
                        actualizarMinutosJugadores()
                    }
                db.collection("Partidos").document(idPartido).update(
                    hashMapOf(
                        "Estado" to "Finalizado",
                    ) as Map<String?, Any?>
                )
            } else if (cuarto >= 4 && binding.txtPuntosLocal.text == binding.txtPuntosVisitante.text) {
                db.collection("MinutoaMinuto").document(idPartido).get()
                    .addOnSuccessListener { it2 ->
                        val listRegistros = it2.get("registro") as ArrayList<Map<String?, Any?>>

                        val registro = hashMapOf(
                            "cuarto" to cuarto,
                            "dorsal" to "",
                            "frase" to "FIN DEL PERIODO",
                            "resultado" to "",
                            "tiempo" to horaActual.toString(),
                            "equipo" to "",
                            "tipoFrase" to "4"
                        ) as Map<String?, Any?>
                        listRegistros.add(registro)

                        val registro2 = hashMapOf(
                            "cuarto" to cuarto + 1,
                            "dorsal" to "",
                            "frase" to "INICIO DEL PERIODO",
                            "resultado" to "",
                            "tiempo" to horaActual.toString(),
                            "equipo" to "",
                            "tipoFrase" to "2"
                        ) as Map<String?, Any?>
                        listRegistros.add(registro2)

                        db.collection("MinutoaMinuto")
                            .document(idPartido)
                            .update(
                                hashMapOf(
                                    "registro" to listRegistros,
                                ) as Map<String?, Any?>
                            ).addOnSuccessListener {
                                binding.TiempoCuarto.stop()
                                cuarto++
                                tmL = 1
                                tmV = 1
                                binding.txtTiemposMLocal.text = tmL.toString()
                                binding.txtTiemposMVisitante.text = tmV.toString()
                                binding.txtCuartoPartido.text = "Cuarto $cuarto"
                                binding.TiempoCuarto.setTextColor(Color.RED)
                                tiempo = "05:00"
                                binding.TiempoCuarto.text = tiempo
                                isPlay = false
                                pauseOffSet = pauseOffSetProrroga
                                actualizarMinutosJugadores()
                                db.collection("Partidos").document(idPartido).update(
                                    hashMapOf(
                                        "TiempoML" to tmL,
                                        "TiempoMV" to tmV,
                                        "FaltaL" to falL,
                                        "FaltaV" to falV,
                                        "Cuarto" to cuarto,
                                        "Tiempo" to tiempo
                                    ) as Map<String?, Any?>
                                )
                                mostrarJugadoresCampo()
                            }
                    }
            } else if (cuarto > 4 && binding.txtPuntosLocal.text != binding.txtPuntosVisitante.text) {
                binding.TiempoCuarto.stop()

                db.collection("MinutoaMinuto").document(idPartido).get()
                    .addOnSuccessListener { it2 ->
                        val listRegistros =
                            it2.get("registro") as ArrayList<Map<String?, Any?>>
                        val registro = hashMapOf(
                            "cuarto" to cuarto,
                            "dorsal" to "",
                            "frase" to "FIN DEL PERIODO",
                            "resultado" to "",
                            "tiempo" to horaActual.toString(),
                            "equipo" to "",
                            "tipoFrase" to "4"
                        ) as Map<String?, Any?>
                        listRegistros.add(registro)

                        val registro2 = hashMapOf(
                            "cuarto" to "",
                            "dorsal" to "",
                            "frase" to "FIN DEL PARTIDO",
                            "resultado" to "",
                            "tiempo" to horaActual.toString(),
                            "equipo" to "",
                            "tipoFrase" to "4"
                        ) as Map<String?, Any?>
                        listRegistros.add(registro2)

                        db.collection("MinutoaMinuto")
                            .document(idPartido)
                            .update(
                                hashMapOf(
                                    "registro" to listRegistros,
                                ) as Map<String?, Any?>
                            )
                    }
                db.collection("Partidos").document(idPartido).update(
                    hashMapOf(
                        "Estado" to "Finalizado",
                    ) as Map<String?, Any?>
                )
                tiempo = "00:00"
                binding.TiempoCuarto.text = tiempo
                binding.TiempoCuarto.setTextColor(Color.RED)
                isPlay = false
                pauseOffSet = 0
                actualizarMinutosJugadores()
            }
        }
    }

    private fun paraCronometro() {
        if (isPlay) {
            binding.TiempoCuarto.stop()
            binding.TiempoCuarto.setTextColor(Color.RED)
            pauseOffSet = -1 * (SystemClock.elapsedRealtime() - binding.TiempoCuarto.base)
            isPlay = false
            actualizaTiempo()
            actualizarMinutosJugadores()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun quinteto(equipo: String) {

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()
        var cont: Int = 0
        var quinteto: Int = 0

        val builder = AlertDialog.Builder(binding.root.context)
        val view = layoutInflater.inflate(R.layout.cambios_equipo, null)

        val horaActual = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        db.collection("Estadisticas").document(idPartido).get()
            .addOnSuccessListener { esta ->
                val listJugador = esta.get("ListadoJugadores") as ArrayList<String>
                val listToggleButton: ArrayList<ToggleButton> =
                    java.util.ArrayList<ToggleButton>()

                for (j in listJugador) {
                    val jugador = esta.get(j) as Map<String?, Any?>

                    if (jugador["equipo"].toString() == equipo) {
                        val toggleButton: ToggleButton = ToggleButton(view.context)
                        toggleButton.text = jugador["dorsal"].toString()
                        toggleButton.id = cont
                        toggleButton.textOff = jugador["dorsal"].toString()
                        toggleButton.textOn = jugador["dorsal"].toString()
                        toggleButton.setTextColor(Color.BLACK)

                        if (equipo == "Local")
                            toggleButton.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonlocaldesactivado))
                        else
                            toggleButton.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonvisitantedesactivado))

                        if (cont < 6)
                            view.findViewById<LinearLayout>(R.id.ContenedorCambios)
                                .addView(toggleButton)
                        else
                            view.findViewById<LinearLayout>(R.id.ContenedorCambios2)
                                .addView(toggleButton)

                        toggleButton.setOnCheckedChangeListener { _, _ ->

                            if (toggleButton.isChecked) {
                                if (quinteto < 5) {
                                    if (equipo == "Local")
                                        toggleButton.setBackgroundDrawable(
                                            resources.getDrawable(
                                                R.drawable.togglebuttonlocalactivado
                                            )
                                        )
                                    else
                                        toggleButton.setBackgroundDrawable(
                                            resources.getDrawable(
                                                R.drawable.togglebuttonvisitanteactivo
                                            )
                                        )
                                    quinteto++
                                } else {
                                    toggleButton.isChecked = false
                                    Toast.makeText(
                                        binding.root.context,
                                        "Solo se puede seleccionar 5 jugadores ",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                if (quinteto > 0) {
                                    if (equipo == "Local")
                                        toggleButton.setBackgroundDrawable(
                                            resources.getDrawable(
                                                R.drawable.togglebuttonlocaldesactivado
                                            )
                                        )
                                    else
                                        toggleButton.setBackgroundDrawable(
                                            resources.getDrawable(
                                                R.drawable.togglebuttonvisitantedesactivado
                                            )
                                        )
                                    quinteto--
                                }
                            }
                        }
                        listToggleButton.add(toggleButton)
                        cont++
                    }
                }

                builder.setView(view)
                val dialog = builder.create()
                dialog.show()

                view.findViewById<Button>(R.id.btnGuardarPlantillaCambios).setOnClickListener {
                    if (quinteto == 5) {
                        var cont = 0
                        db.collection("MinutoaMinuto").document(idPartido).get()
                            .addOnSuccessListener { it2 ->
                                val listRegistros =
                                    it2.get("registro") as ArrayList<Map<String?, Any?>>

                                if (equipo == "Local") {
                                    val registro = hashMapOf(
                                        "cuarto" to cuarto,
                                        "dorsal" to "",
                                        "frase" to "INICIO DEL PERIODO",
                                        "resultado" to "",
                                        "tiempo" to horaActual.toString(),
                                        "equipo" to "",
                                        "tipoFrase" to "2",
                                        "tipoImg" to ""
                                    ) as Map<String?, Any?>
                                    listRegistros.add(registro)
                                }

                                for (toggleButton in listToggleButton) {
                                    if (toggleButton.isChecked) {
                                        if (equipo == "Local") {
                                            when (cont) {
                                                0 -> {
                                                    binding.TBLocal1.text = toggleButton.text
                                                    binding.TBLocal1.textOff =
                                                        toggleButton.textOff
                                                    binding.TBLocal1.textOn =
                                                        toggleButton.textOn
                                                }

                                                1 -> {
                                                    binding.TBLocal2.text = toggleButton.text
                                                    binding.TBLocal2.textOff =
                                                        toggleButton.textOff
                                                    binding.TBLocal2.textOn =
                                                        toggleButton.textOn
                                                }

                                                2 -> {
                                                    binding.TBLocal3.text = toggleButton.text
                                                    binding.TBLocal3.textOff =
                                                        toggleButton.textOff
                                                    binding.TBLocal3.textOn =
                                                        toggleButton.textOn
                                                }

                                                3 -> {
                                                    binding.TBLocal4.text = toggleButton.text
                                                    binding.TBLocal4.textOff =
                                                        toggleButton.textOff
                                                    binding.TBLocal4.textOn =
                                                        toggleButton.textOn
                                                }

                                                4 -> {
                                                    binding.TBLocal5.text = toggleButton.text
                                                    binding.TBLocal5.textOff =
                                                        toggleButton.textOff
                                                    binding.TBLocal5.textOn =
                                                        toggleButton.textOn
                                                }
                                            }
                                            quintetoL.add(toggleButton.text.toString())
                                        } else {
                                            when (cont) {
                                                0 -> {
                                                    binding.TBVisitante1.text =
                                                        toggleButton.text
                                                    binding.TBVisitante1.textOff =
                                                        toggleButton.textOff
                                                    binding.TBVisitante1.textOn =
                                                        toggleButton.textOn
                                                }

                                                1 -> {
                                                    binding.TBVisitante2.text =
                                                        toggleButton.text
                                                    binding.TBVisitante2.textOff =
                                                        toggleButton.textOff
                                                    binding.TBVisitante2.textOn =
                                                        toggleButton.textOn
                                                }

                                                2 -> {
                                                    binding.TBVisitante3.text =
                                                        toggleButton.text
                                                    binding.TBVisitante3.textOff =
                                                        toggleButton.textOff
                                                    binding.TBVisitante3.textOn =
                                                        toggleButton.textOn
                                                }

                                                3 -> {
                                                    binding.TBVisitante4.text =
                                                        toggleButton.text
                                                    binding.TBVisitante4.textOff =
                                                        toggleButton.textOff
                                                    binding.TBVisitante4.textOn =
                                                        toggleButton.textOn
                                                }

                                                4 -> {
                                                    binding.TBVisitante5.text =
                                                        toggleButton.text
                                                    binding.TBVisitante5.textOff =
                                                        toggleButton.textOff
                                                    binding.TBVisitante5.textOn =
                                                        toggleButton.textOn
                                                }
                                            }
                                            quintetoV.add(toggleButton.text.toString())
                                        }

                                        for (j in listJugador) {
                                            val jugador = esta.get(j) as Map<String?, Any?>
                                            if (jugador["dorsal"].toString() == toggleButton.text && jugador["equipo"].toString() == equipo) {
                                                val registro = hashMapOf(
                                                    "cuarto" to cuarto,
                                                    "dorsal" to toggleButton.text,
                                                    "nombre" to jugador["nombre"],
                                                    "frase" to "ENTRA A LA PISTA EL ",
                                                    "resultado" to "",
                                                    "tiempo" to binding.TiempoCuarto.text.toString(),
                                                    "equipo" to equipo,
                                                    "tipoFrase" to "3",
                                                    "tipoImg" to "1"
                                                ) as Map<String?, Any?>
                                                listRegistros.add(registro)
                                                cont++
                                            }
                                        }
                                    }
                                }
                                if (equipo == "Local") {
                                    db.collection("Partidos")
                                        .document(idPartido)
                                        .update(
                                            hashMapOf(
                                                "QuintetoL" to quintetoL,
                                            ) as Map<String?, Any?>
                                        )
                                } else {
                                    db.collection("Partidos")
                                        .document(idPartido)
                                        .update(
                                            hashMapOf(
                                                "QuintetoV" to quintetoV,
                                            ) as Map<String?, Any?>
                                        )
                                }
                                db.collection("MinutoaMinuto")
                                    .document(idPartido)
                                    .update(
                                        hashMapOf(
                                            "registro" to listRegistros,
                                        ) as Map<String?, Any?>
                                    )
                            }
                        dialog.hide()
                        if (equipo == "Local") colocarQuinteto(equipo, llenarListToggleLocal()) else colocarQuinteto(
                            equipo,
                            llenarListToggleVisitante()
                        )
                        actualizarMinutosJugadores()
                    } else {
                        Toast.makeText(
                            binding.root.context,
                            "Seleccione a 5 jugadores para continuar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun hacerCambiosLocal(lista: ArrayList<ToggleButton>, toggleButtonSale: ToggleButton) {
        if (estado == "Finalizado") return

        val context = binding.root.context
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val idPartido = prefs.getString("idPartido", "").toString()
        val builder = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.cambios_equipo, null)
        val contenedor1 = view.findViewById<LinearLayout>(R.id.ContenedorCambios)
        val contenedor2 = view.findViewById<LinearLayout>(R.id.ContenedorCambios2)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardarPlantillaCambios)
        val listToggleButton = arrayListOf<ToggleButton>()
        var quinteto = 0

        paraCronometro()

        db.collection("Estadisticas").document(idPartido).get().addOnSuccessListener { esta ->
            val listJugador = esta.get("ListadoJugadores") as ArrayList<String>
            var contLoca = 0

            listJugador.forEach { j ->
                val jugador = esta.get(j) as Map<String, Any>
                val dorsal = jugador["dorsal"].toString()

                if (jugador["equipo"] == "Local" && jugador["falC"].toString() != "5" && jugador["fal_esp"].toString() != "2" && dorsal !in lista.map { it.text }) {
                    val toggleButton = ToggleButton(view.context).apply {
                        text = dorsal
                        textOff = dorsal
                        textOn = dorsal
                        id = contLoca++
                        setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonlocaldesactivado))
                        setTextColor(Color.BLACK)
                        setOnCheckedChangeListener { buttonView, isChecked ->
                            if (isChecked) {
                                if (quinteto < 1) {
                                    buttonView.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonlocalactivado))
                                    quinteto++
                                } else {
                                    buttonView.isChecked = false
                                    Toast.makeText(context, "Solo se puede seleccionar 1 jugador", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                buttonView.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonlocaldesactivado))
                                quinteto--
                            }
                        }
                    }
                    (if (contLoca <= 5) contenedor1 else contenedor2).addView(toggleButton)
                    listToggleButton.add(toggleButton)
                }
            }

            val dialog = builder.setView(view).create()
            dialog.show()

            btnGuardar.setOnClickListener {
                if (quinteto == 1) {
                    db.collection("MinutoaMinuto").document(idPartido).get().addOnSuccessListener { it2 ->
                        val listRegistros = (it2.get("registro") as? ArrayList<Map<String, Any>>)?.toMutableList() ?: mutableListOf()

                        listJugador.mapNotNull { j ->
                            val jugador = (esta.get(j) as Map<String?, Any?>).toMutableMap()
                            if (jugador["dorsal"].toString() == toggleButtonSale.text && jugador["equipo"] == "Local") {
                                quintetoL.remove(toggleButtonSale.text.toString())
                                listRegistros.add(crearRegistro(jugador, "ABANDONA LA PISTA EL ", "", "3", "1"))
                            }
                        }

                        val jugadorIn = listToggleButton.firstOrNull { it.isChecked }?.let { toggleButton ->
                            toggleButtonSale.text = toggleButton.text
                            listJugador.mapNotNull { j ->
                                val jugador = (esta.get(j) as Map<String?, Any?>).toMutableMap()
                                if (jugador["dorsal"].toString() == toggleButton.text && jugador["equipo"] == "Local") {
                                    quintetoL.add(toggleButton.text.toString())
                                    actualizaQuinteto("QuintetoL")
                                    jugador.apply {
                                        put("minutoEntrada", tiempo)
                                        put("cuartoEntrada", cuarto)
                                    }
                                    listRegistros.add(crearRegistro(jugador, "ENTRA A LA PISTA EL ", "", "3", "1"))
                                    j to jugador
                                } else null
                            }.toMap()
                        } ?: emptyMap()

                        db.collection("Estadisticas").document(idPartido).update(jugadorIn)
                        db.collection("MinutoaMinuto").document(idPartido).update("registro", listRegistros)
                            .addOnSuccessListener { actualizaJugadaReciente() }
                        dialog.dismiss()
                    }
                } else {
                    Toast.makeText(context, "Seleccione a 1 jugador para continuar", Toast.LENGTH_SHORT).show()
                }
            }
        }
        vaciarToggle(llenarListToggle())
    }

    private fun hacerCambiosVisitante(lista: ArrayList<ToggleButton>, toggleButtonSale: ToggleButton) {
        if (estado == "Finalizado") return

        val context = binding.root.context
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val idPartido = prefs.getString("idPartido", "").toString()
        val builder = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.cambios_equipo, null)
        val contenedor1 = view.findViewById<LinearLayout>(R.id.ContenedorCambios)
        val contenedor2 = view.findViewById<LinearLayout>(R.id.ContenedorCambios2)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardarPlantillaCambios)
        val listToggleButton = arrayListOf<ToggleButton>()
        var quinteto = 0

        paraCronometro()

        db.collection("Estadisticas").document(idPartido).get().addOnSuccessListener { esta ->
            val listJugador = esta.get("ListadoJugadores") as ArrayList<String>
            var contVisitante = 0

            listJugador.forEach { j ->
                val jugador = esta.get(j) as Map<String, Any>
                val dorsal = jugador["dorsal"].toString()

                if (jugador["equipo"] == "Visitante" && jugador["falC"].toString() != "5" && jugador["fal_esp"].toString() != "2" && dorsal !in lista.map { it.text }) {
                    val toggleButton = ToggleButton(view.context).apply {
                        text = dorsal
                        textOff = dorsal
                        textOn = dorsal
                        id = contVisitante++
                        setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonvisitantedesactivado))
                        setTextColor(Color.BLACK)
                        setOnCheckedChangeListener { buttonView, isChecked ->
                            if (isChecked) {
                                if (quinteto < 1) {
                                    buttonView.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonvisitanteactivo))
                                    quinteto++
                                } else {
                                    buttonView.isChecked = false
                                    Toast.makeText(context, "Solo se puede seleccionar 1 jugador", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                buttonView.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonvisitantedesactivado))
                                quinteto--
                            }
                        }
                    }
                    (if (contVisitante <= 5) contenedor1 else contenedor2).addView(toggleButton)
                    listToggleButton.add(toggleButton)
                }
            }

            val dialog = builder.setView(view).create()
            dialog.show()

            btnGuardar.setOnClickListener {
                if (quinteto == 1) {
                    db.collection("MinutoaMinuto").document(idPartido).get().addOnSuccessListener { it2 ->
                        val listRegistros = (it2.get("registro") as? ArrayList<Map<String, Any>>)?.toMutableList() ?: mutableListOf()

                        listJugador.mapNotNull { j ->
                            val jugador = (esta.get(j) as Map<String?, Any?>).toMutableMap()
                            if (jugador["dorsal"].toString() == toggleButtonSale.text && jugador["equipo"] == "Visitante") {
                                quintetoV.remove(toggleButtonSale.text.toString())
                                listRegistros.add(crearRegistro(jugador, "ABANDONA LA PISTA EL ", "", "3", "1"))
                            }
                        }

                        val jugadorIn = listToggleButton.firstOrNull { it.isChecked }?.let { toggleButton ->
                            toggleButtonSale.text = toggleButton.text
                            listJugador.mapNotNull { j ->
                                val jugador = (esta.get(j) as Map<String?, Any?>).toMutableMap()
                                if (jugador["dorsal"].toString() == toggleButton.text && jugador["equipo"] == "Visitante") {
                                    quintetoV.add(toggleButton.text.toString())
                                    actualizaQuinteto("QuintetoV")
                                    jugador.apply {
                                        put("minutoEntrada", tiempo)
                                        put("cuartoEntrada", cuarto)
                                    }
                                    listRegistros.add(crearRegistro(jugador, "ENTRA A LA PISTA EL ", "", "3", "1"))
                                    j to jugador
                                } else null
                            }.toMap()
                        } ?: emptyMap()

                        db.collection("Estadisticas").document(idPartido).update(jugadorIn)
                        db.collection("MinutoaMinuto").document(idPartido).update("registro", listRegistros)
                            .addOnSuccessListener { actualizaJugadaReciente() }
                        dialog.dismiss()
                    }
                } else {
                    Toast.makeText(context, "Seleccione a 1 jugador para continuar", Toast.LENGTH_SHORT).show()
                }
            }
        }
        vaciarToggle(llenarListToggle())
    }

    private fun crearRegistro(
        jugador: MutableMap<String?, Any?>,
        frase: String,
        resultado: String,
        tipoFrase: String,
        tipoImg: String
    ): Map<String, Any> {
        return mapOf(
            "cuarto" to cuarto,
            "dorsal" to jugador["dorsal"].toString(),
            "nombre" to jugador["nombre"].toString(),
            "frase" to frase,
            "resultado" to resultado,
            "tiempo" to binding.TiempoCuarto.text.toString(),
            "equipo" to jugador["equipo"].toString(),
            "tipoFrase" to tipoFrase,
            "tipoImg" to tipoImg
        )
    }

    private fun colocarQuinteto(equipo: String, listToggleButton: ArrayList<ToggleButton>) {
        // Ordenamos los dorsales teniendo en cuenta que "00" y "0" son distintos
        val quintetoOrdenado = if (equipo == "Local") {
            quintetoL.sortedWith { a, b ->
                val dorsalA = a.toIntOrNull() ?: Int.MAX_VALUE
                val dorsalB = b.toIntOrNull() ?: Int.MAX_VALUE
                when {
                    a == "00" -> -1 // "00" debe ir primero
                    b == "00" -> 1 // "00" debe ir primero
                    a == "0" -> -1 // "0" debe ir después de "00"
                    b == "0" -> 1 // "0" debe ir después de "00"
                    else -> dorsalA.compareTo(dorsalB) // Ordenamos como enteros
                }
            }
        } else {
            quintetoV.sortedWith { a, b ->
                val dorsalA = a.toIntOrNull() ?: Int.MAX_VALUE
                val dorsalB = b.toIntOrNull() ?: Int.MAX_VALUE
                when {
                    a == "00" -> -1 // "00" debe ir primero
                    b == "00" -> 1 // "00" debe ir primero
                    a == "0" -> -1 // "0" debe ir después de "00"
                    b == "0" -> 1 // "0" debe ir después de "00"
                    else -> dorsalA.compareTo(dorsalB) // Ordenamos como enteros
                }
            }
        }

        // Asignamos los dorsales ordenados a los ToggleButton
        for ((cont, quin) in quintetoOrdenado.withIndex()) {
            listToggleButton[cont].text = quin
            listToggleButton[cont].textOff = quin
            listToggleButton[cont].textOn = quin
        }
    }

    private fun actualizaQuinteto(equipo: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()
        val quinteto = if (equipo == "QuintetoL") quintetoL else quintetoV

        db.collection("Partidos")
            .document(idPartido)
            .update(equipo, quinteto)

        if (equipo == "QuintetoL") colocarQuinteto("Local", llenarListToggleLocal()) else colocarQuinteto("Visitante", llenarListToggleVisitante())
    }

    @SuppressLint("MissingInflatedId", "ResourceAsColor")
    private fun mostrarFaltasEquipo(equipo: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()

        paraCronometro()

        val builder = AlertDialog.Builder(binding.root.context)
        val view = layoutInflater.inflate(R.layout.mostrar_falta_dialog, null)
        builder.setView(view)

        val tlMuestraFalta = view.findViewById<TableLayout>(R.id.muestraFaltas)
        tlMuestraFalta.removeAllViews()

        // Agregar cabecera de la tabla
        val cabeceraFalta = LayoutInflater.from(binding.root.context).inflate(R.layout.row_cabecera_falta, null, false)
        tlMuestraFalta.addView(cabeceraFalta)

        // Obtener estadísticas
        db.collection("Estadisticas").document(idPartido).get()
            .addOnSuccessListener { result ->
                val listJugador = result.get("ListadoJugadores") as ArrayList<String>

                listJugador.filter {
                    val jugador = result.get(it) as Map<String?, Any?>
                    jugador["equipo"] == equipo
                }.forEachIndexed { index, jugadorKey ->
                    val jugador = result.get(jugadorKey) as Map<String?, Any?>
                    val filaFalta = LayoutInflater.from(binding.root.context).inflate(R.layout.row_falta, null, false)

                    // Colorear filas alternas
                    if (index % 2 != 0) {
                        filaFalta.findViewById<TableRow>(R.id.filaFalta).setBackgroundColor(Color.parseColor("#FFE4E4E4"))
                    }

                    // Rellenar los datos de la fila
                    filaFalta.findViewById<TextView>(R.id.txtFaltaDorsal).text = jugador["dorsal"].toString()
                    filaFalta.findViewById<TextView>(R.id.txtFaltaNombre).text = jugador["nombre"].toString().uppercase(Locale.ROOT)
                    val faltas = jugador["falC"].toString().toInt()
                    val faltasEspecial = jugador["fal_esp"].toString().toInt()
                    val txtFalta = filaFalta.findViewById<TextView>(R.id.txtFaltaFalta)
                    txtFalta.text = faltas.toString()

                    // Cambiar color según el número de faltas
                    txtFalta.setTextColor(
                        when {
                            faltasEspecial == 2 -> Color.RED
                            faltas == 5 -> Color.RED
                            faltas >= 3 -> Color.parseColor("#FBC02D")
                            else -> Color.BLACK
                        }
                    )

                    tlMuestraFalta.addView(filaFalta)
                }
            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun manejarFalta(
        tipoFalta: String,
        equipo: String,
        listaToggle: List<ToggleButton>,
        idPartido: String
    ) {
        for (i in listaToggle) {
            if (i.isChecked) {
                db.collection("Estadisticas").document(idPartido).get().addOnSuccessListener {
                    val listJugador = it.get("ListadoJugadores") as ArrayList<String>
                    for (j in listJugador) {
                        val jugador = (it.get(j) as Map<String?, Any?>).toMutableMap()
                        val jugadorDorsal = jugador["dorsal"].toString()

                        // Verificamos si el jugador está seleccionado
                        if (jugadorDorsal == i.text.toString() && jugador["equipo"] == equipo) {
                            // Actualizamos la cantidad de faltas dependiendo del equipo
                            if (jugador["equipo"] == "Local") {
                                if (tipoFalta == "FALTA COMETIDA") {
                                    jugador["falC"] = jugador["falC"].toString().toInt() + 1
                                    falL++
                                    binding.txtFaltasLocal.text = falL.toString()
                                } else if (tipoFalta == "FALTA RECIBIDA") {
                                    jugador["falR"] = jugador["falR"].toString().toInt() + 1
                                } else if (tipoFalta == "FALTA TÉCNICA" || tipoFalta == "FALTA ANTIDEPORTIVA") {
                                    jugador["fal_esp"] = jugador["fal_esp"].toString().toInt() + 1
                                    jugador["falC"] = jugador["falC"].toString().toInt() + 1
                                    falL++
                                    binding.txtFaltasLocal.text = falL.toString()
                                }
                            } else if (jugador["equipo"] == "Visitante") {
                                if (tipoFalta == "FALTA COMETIDA") {
                                    jugador["falC"] = jugador["falC"].toString().toInt() + 1
                                    falV++
                                    binding.txtFaltasVisitante.text = falV.toString()
                                } else if (tipoFalta == "FALTA RECIBIDA") {
                                    jugador["falR"] = jugador["falR"].toString().toInt() + 1
                                } else if (tipoFalta == "FALTA TÉCNICA" || tipoFalta == "FALTA ANTIDEPORTIVA") {
                                    jugador["fal_esp"] = jugador["fal_esp"].toString().toInt() + 1
                                    jugador["falC"] = jugador["falC"].toString().toInt() + 1
                                    falV++
                                    binding.txtFaltasVisitante.text = falV.toString()
                                }
                            }

                            // Actualiza la estadística en Firebase
                            db.collection("Estadisticas")
                                .document(idPartido)
                                .update(mapOf(j to jugador))
                                .addOnSuccessListener {
                                    calcularVal(j, jugador)

                                    if (jugador["equipo"] == "Local")
                                        actualizaFaltaEquipo("FaltaL")
                                    else
                                        actualizaFaltaEquipo("FaltaV")

                                    // Registra la jugada en Firebase
                                    db.collection("MinutoaMinuto").document(idPartido).get().addOnSuccessListener {
                                        val listRegistros = it.get("registro") as ArrayList<Map<String, Any>>
                                        listRegistros.add(
                                            crearRegistro(
                                                jugador,
                                                tipoFalta,
                                                "${binding.txtPuntosLocal.text}-${binding.txtPuntosVisitante.text}",
                                                "3",
                                                "3"
                                            )
                                        )
                                        db.collection("MinutoaMinuto").document(idPartido).update("registro", listRegistros).addOnSuccessListener {
                                            actualizaJugadaReciente()

                                            // Maneja la expulsión si el jugador tiene muchas faltas
                                            if (jugador["falC"].toString().toInt() == 5 || jugador["fal_esp"].toString().toInt() == 2) {
                                                manejarExpulsion(jugador["equipo"].toString(), llenarListToggleLocal(), i)
                                            }
                                        }
                                    }
                                }
                        }
                    }
                }
                break
            }
        }
    }


    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    private fun manejarExpulsion(equipo: String, lista: ArrayList<ToggleButton>, toggleButtonSale: ToggleButton) {
        val context = binding.root.context
        val builder = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.cambios_equipo, null)
        val txtQuinteto = view.findViewById<TextView>(R.id.txtQuinteto)
        txtQuinteto.text = "El jugador #${toggleButtonSale.text}, está expulsado."

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val idPartido = prefs.getString("idPartido", "").toString()
        val equipoClave = if (equipo == "Local") "QuintetoL" else "QuintetoV"

        db.collection("Estadisticas").document(idPartido).get().addOnSuccessListener { esta ->
            val listJugador = esta.get("ListadoJugadores") as ArrayList<String>
            val listToggleButton = ArrayList<ToggleButton>()
            var quinteto = 0

            listJugador.forEach { jugadorId ->
                val jugador = esta.get(jugadorId) as Map<String, Any>
                val dorsal = jugador["dorsal"].toString()
                if (jugador["equipo"] == equipo && jugador["falC"].toString() != "5" && jugador["fal_esp"].toString() != "2" && lista.none { it.text == dorsal }) {
                    val toggleButton = ToggleButton(view.context).apply {
                        text = dorsal
                        textOff = dorsal
                        textOn = dorsal
                        setBackgroundDrawable(resources.getDrawable(if (equipo == "Local") R.drawable.togglebuttonlocaldesactivado else R.drawable.togglebuttonvisitantedesactivado))
                        setTextColor(Color.BLACK)
                    }
                    view.findViewById<LinearLayout>(if (listToggleButton.size < 5) R.id.ContenedorCambios else R.id.ContenedorCambios2)
                        .addView(toggleButton)

                    toggleButton.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            if (quinteto < 1) {
                                toggleButton.setBackgroundDrawable(resources.getDrawable(if (equipo == "Local") R.drawable.togglebuttonlocalactivado else R.drawable.togglebuttonvisitanteactivo))
                                quinteto++
                            } else {
                                toggleButton.isChecked = false
                                Toast.makeText(context, "Solo se puede seleccionar 1 jugador", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            toggleButton.setBackgroundDrawable(resources.getDrawable(if (equipo == "Local") R.drawable.togglebuttonlocaldesactivado else R.drawable.togglebuttonvisitantedesactivado))
                            quinteto--
                        }
                    }
                    listToggleButton.add(toggleButton)
                }
            }

            builder.setView(view).setCancelable(false)
            val dialog = builder.create()
            dialog.show()

            view.findViewById<Button>(R.id.btnGuardarPlantillaCambios).setOnClickListener {
                if (quinteto == 1 && listToggleButton.isNotEmpty()) {
                    db.collection("MinutoaMinuto").document(idPartido).get().addOnSuccessListener { it2 ->
                        val listRegistros = it2.get("registro") as ArrayList<Map<String, Any>>

                        listJugador.mapNotNull { esta.get(it) as? Map<String?, Any?> }
                            .firstOrNull { it["dorsal"].toString() == toggleButtonSale.text && it["equipo"].toString() == equipo }
                            ?.let { jugador ->
                                listRegistros.add(crearRegistro(jugador.toMutableMap(), "ABANDONA LA PISTA EL ", "", "3", "1"))
                                if (equipo == "Local") quintetoL.remove(toggleButtonSale.text.toString()) else quintetoV.remove(toggleButtonSale.text.toString())
                                actualizaQuinteto(equipoClave)
                            }

                        listToggleButton.firstOrNull { it.isChecked }?.let { toggleButton ->
                            toggleButtonSale.text = toggleButton.text
                            listJugador.forEach { j ->
                                val jugador = (esta.get(j) as Map<String?, Any?>).toMutableMap()
                                if (jugador["dorsal"].toString() == toggleButton.text && jugador["equipo"].toString() == equipo) {
                                    listRegistros.add(crearRegistro(jugador, "ENTRA A LA PISTA EL ", "", "3", "1"))
                                    if (equipo == "Local") quintetoL.add(toggleButton.text.toString()) else quintetoV.add(toggleButton.text.toString())
                                    actualizaQuinteto(equipoClave)
                                    jugador.apply {
                                        put("minutoEntrada", tiempo)
                                        put("cuartoEntrada", cuarto)
                                    }
                                    db.collection("Estadisticas").document(idPartido).update(jugador)
                                }
                            }
                        }

                        db.collection("MinutoaMinuto").document(idPartido).update("registro", listRegistros).addOnSuccessListener {
                            actualizaJugadaReciente()
                        }
                    }
                    dialog.dismiss()
                } else if (quinteto == 0 && listToggleButton.isEmpty()) {
                    db.collection("MinutoaMinuto").document(idPartido).get().addOnSuccessListener { it2 ->
                        val listRegistros = it2.get("registro") as ArrayList<Map<String, Any>>

                        listJugador.firstOrNull { jugador ->
                            val datosJugador = esta.get(jugador) as Map<String?, Any?>
                            datosJugador["dorsal"].toString() == toggleButtonSale.text && datosJugador["equipo"].toString() == equipo
                        }?.let { jugadorEncontrado ->
                            val datosJugador = esta.get(jugadorEncontrado) as Map<String, Any>
                            listRegistros.add(crearRegistro(datosJugador.toMutableMap(), "ABANDONA LA PISTA EL ", "", "3", "1"))
                            if (equipo == "Local") {
                                quintetoL.remove(toggleButtonSale.text.toString())
                                quintetoL.add(" ")
                            } else {
                                quintetoV.remove(toggleButtonSale.text.toString())
                                quintetoV.add(" ")
                            }
                            actualizaQuinteto(equipoClave)
                        }

                        db.collection("MinutoaMinuto").document(idPartido)
                            .update("registro", listRegistros)
                            .addOnSuccessListener { actualizaJugadaReciente() }
                    }
                    dialog.dismiss()
                } else {
                    Toast.makeText(context, "Seleccione a 1 jugador para continuar", Toast.LENGTH_SHORT).show()
                }
            }
        }
        vaciarToggle(llenarListToggle())
        actualizaTiempo()
    }

    private fun mostrarJugadoresCampo() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()

        db.collection("Estadisticas").document(idPartido).get().addOnSuccessListener { estadisticas ->
            val listJugador = estadisticas.get("ListadoJugadores") as ArrayList<String>
            db.collection("MinutoaMinuto").document(idPartido).get().addOnSuccessListener { minutoAMinuto ->
                val listRegistros = minutoAMinuto.get("registro") as ArrayList<Map<String, Any>>

                listOf("Local" to quintetoL, "Visitante" to quintetoV).forEach { (equipo, quinteto) ->
                    quinteto.forEach { dorsal ->
                        listJugador.mapNotNull { estadisticas.get(it) as? Map<String?, Any?> }
                            .firstOrNull { it["dorsal"].toString() == dorsal && it["equipo"] == equipo }
                            ?.let { jugador ->
                                listRegistros.add(crearRegistro(jugador.toMutableMap(), "ENTRA A LA PISTA EL ", "", "3", "1"))
                            }
                    }
                }

                db.collection("MinutoaMinuto").document(idPartido).update("registro", listRegistros).addOnSuccessListener {
                    actualizaJugadaReciente()
                }
            }
        }
    }

    @SuppressLint("CutPasteId", "SetTextI18n")
    private fun tiempoMuerto(equipo: String) {

        paraCronometro()

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()

        val builder = AlertDialog.Builder(binding.root.context)
        val view = layoutInflater.inflate(R.layout.cronometro_tiempo_muerto, null)
        builder.setView(view)

        view.findViewById<Chronometer>(R.id.cronometroTM).isCountDown = true

        view.findViewById<Chronometer>(R.id.cronometroTM).base =
            SystemClock.elapsedRealtime() + 10 * 60 * 100

        if (equipo == "Local")
            view.findViewById<Chronometer>(R.id.cronometroTM)
                .setBackgroundResource(R.drawable.bg_round)
        else if (equipo == "Visitante")
            view.findViewById<Chronometer>(R.id.cronometroTM)
                .setBackgroundResource(R.drawable.bg_round_visitante)

        view.findViewById<Chronometer>(R.id.cronometroTM).start()

        val dialog = builder.create()
        dialog.show()

        db.collection("MinutoaMinuto")
            .document(idPartido).get()
            .addOnSuccessListener { it2 ->
                val listRegistros =
                    it2.get("registro") as ArrayList<Map<String?, Any?>>

                val registro = hashMapOf(
                    "cuarto" to cuarto,
                    "dorsal" to "",
                    "nombre" to "ENTRENADOR/A",
                    "frase" to "TIEMPO MUERTO SOLICITADO",
                    "resultado" to "",
                    "tiempo" to binding.TiempoCuarto.text.toString(),
                    "equipo" to equipo,
                    "tipoFrase" to "3",
                    "tipoImg" to "2"
                ) as Map<String?, Any?>
                listRegistros.add(registro)

                db.collection("MinutoaMinuto")
                    .document(idPartido)
                    .update(
                        hashMapOf(
                            "registro" to listRegistros,
                        ) as Map<String?, Any?>
                    ).addOnSuccessListener {
                        actualizaJugadaReciente()
                    }
            }

        view.findViewById<Chronometer>(R.id.cronometroTM).setOnChronometerTickListener {
            if (it.text.toString() == "00:00") {
                dialog.dismiss()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun actualizaJugadaReciente() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()

        db.collection("MinutoaMinuto").document(idPartido).get().addOnSuccessListener {
            val listRegistros = it.get("registro") as ArrayList<Map<String?, Any?>>

            val registro = listRegistros[listRegistros.size - 1]
            if (registro["dorsal"] != "")
                binding.txtJugadaReciente.text = registro["frase"].toString() + " #" + registro["dorsal"] + ", " + registro["equipo"].toString()
                    .uppercase(Locale.getDefault())
            else
                binding.txtJugadaReciente.text = registro["frase"].toString() + ", " + registro["nombre"] + " " + registro["equipo"].toString()
                    .uppercase(Locale.getDefault())
        }
    }

    @SuppressLint("SetTextI18n")
    private fun borraJugada(jugada: MinutoAMinuto) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()

        when (jugada.imgAccion) {
            //Cambios
            "1" -> {

            }
            //Tiempo Muerto
            "2" -> {
                if (jugada.equipo == "Local") {
                    tmL++
                } else {
                    tmV++
                }
                actualizaTiempoMuertos()
            }
            //Faltas
            "3" -> {
                db.collection("Estadisticas").document(idPartido).get()
                    .addOnSuccessListener {
                        val listJugador = it.get("ListadoJugadores") as ArrayList<String>
                        for (j in 0..<listJugador.count()) {
                            val jugador = (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()
                            if (jugador["dorsal"] == jugada.dorsal && jugador["equipo"] == jugada.equipo) {

                                if (jugada.frase == "FALTA RECIBIDA") {
                                    jugador["falR"] = jugador["falR"].toString().toInt() - 1
                                } else {
                                    jugador["falC"] = jugador["falC"].toString().toInt() - 1
                                    if (jugada.equipo == "Local") {
                                        falL--
                                        actualizaFaltaEquipo("FaltaL")
                                    } else {
                                        falV--
                                        actualizaFaltaEquipo("FaltaV")
                                    }
                                }

                                db.collection("Estadisticas")
                                    .document(idPartido)
                                    .update(
                                        hashMapOf(
                                            listJugador[j] to jugador
                                        ) as Map<String, Any>
                                    ).addOnSuccessListener {
                                        calcularVal(listJugador[j], jugador)
                                    }
                                break
                            }
                        }
                    }
            }
            //Tiros Libres
            "4" -> {

            }
            //Tiro Libre anotado
            "5" -> {
                db.collection("Estadisticas").document(idPartido).get()
                    .addOnSuccessListener {
                        val listJugador = it.get("ListadoJugadores") as ArrayList<String>
                        for (j in 0..<listJugador.count()) {
                            val jugador = (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()
                            if (jugador["dorsal"] == jugada.dorsal && jugador["equipo"] == jugada.equipo) {
                                jugador["tlA"] = jugador["tlA"].toString().toInt() - 1
                                jugador["puntos"] = jugador["puntos"].toString().toInt() - 1
                                if (jugada.equipo == "Local") {
                                    binding.txtPuntosLocal.text = "" + (binding.txtPuntosLocal.text.toString().toInt() - 1)
                                } else {
                                    binding.txtPuntosVisitante.text = "" + (binding.txtPuntosVisitante.text.toString().toInt() - 1)
                                }
                                actualizaResultado()
                                db.collection("Estadisticas")
                                    .document(idPartido)
                                    .update(
                                        hashMapOf(
                                            listJugador[j] to jugador
                                        ) as Map<String, Any>
                                    ).addOnSuccessListener {
                                        calcularVal(listJugador[j], jugador)
                                    }
                                break
                            }
                        }
                    }
            }
            //Tiro Fallado
            "6" -> {
                db.collection("Estadisticas").document(idPartido).get()
                    .addOnSuccessListener {
                        val listJugador = it.get("ListadoJugadores") as ArrayList<String>
                        for (j in 0..<listJugador.count()) {
                            val jugador = (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()
                            if (jugador["dorsal"] == jugada.dorsal && jugador["equipo"] == jugada.equipo) {

                                when (jugada.frase) {
                                    "TIRO LIBRE FALLADO" -> {
                                        jugador["tlF"] = jugador["tlF"].toString().toInt() - 1
                                    }

                                    "TIRO DE 2 FALLADO" -> {
                                        jugador["tc2pF"] = jugador["tc2pF"].toString().toInt() - 1
                                        val tiros = jugador["tiros"] as ArrayList<Map<String, Any>>
                                        tiros.removeIf { it["x"] == jugada.coordenada_x && it["y"] == jugada.coordenada_y }
                                        jugador["tiros"] = tiros
                                    }

                                    "TIRO DE 3 FALLADO" -> {
                                        jugador["tc3pF"] = jugador["tc3pF"].toString().toInt() - 1
                                        val tiros = jugador["tiros"] as ArrayList<Map<String, Any>>
                                        tiros.removeIf { it["x"] == jugada.coordenada_x && it["y"] == jugada.coordenada_y }
                                        jugador["tiros"] = tiros
                                    }
                                }

                                db.collection("Estadisticas")
                                    .document(idPartido)
                                    .update(
                                        hashMapOf(
                                            listJugador[j] to jugador
                                        ) as Map<String, Any>
                                    ).addOnSuccessListener {
                                        calcularVal(listJugador[j], jugador)
                                    }
                                break
                            }
                        }
                    }
            }
            //Tiro 2 anotado
            "7" -> {
                db.collection("Estadisticas").document(idPartido).get()
                    .addOnSuccessListener {
                        val listJugador = it.get("ListadoJugadores") as ArrayList<String>
                        for (j in 0..<listJugador.count()) {
                            val jugador = (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()
                            if (jugador["dorsal"] == jugada.dorsal && jugador["equipo"] == jugada.equipo) {
                                jugador["tc2pA"] = jugador["tc2pA"].toString().toInt() - 1
                                jugador["puntos"] = jugador["puntos"].toString().toInt() - 2
                                val tiros = jugador["tiros"] as ArrayList<Map<String, Any>>
                                tiros.removeIf { it["x"] == jugada.coordenada_x && it["y"] == jugada.coordenada_y }
                                jugador["tiros"] = tiros
                                if (jugada.equipo == "Local") {
                                    binding.txtPuntosLocal.text = "" + (binding.txtPuntosLocal.text.toString().toInt() - 2)
                                } else {
                                    binding.txtPuntosVisitante.text = "" + (binding.txtPuntosVisitante.text.toString().toInt() - 2)
                                }
                                actualizaResultado()
                                db.collection("Estadisticas")
                                    .document(idPartido)
                                    .update(
                                        hashMapOf(
                                            listJugador[j] to jugador
                                        ) as Map<String, Any>
                                    ).addOnSuccessListener {
                                        calcularVal(listJugador[j], jugador)
                                    }
                                break
                            }
                        }
                    }
            }
            //Tiro 3 anotado
            "8" -> {
                db.collection("Estadisticas").document(idPartido).get()
                    .addOnSuccessListener {
                        val listJugador = it.get("ListadoJugadores") as ArrayList<String>
                        for (j in 0..<listJugador.count()) {
                            val jugador = (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()
                            if (jugador["dorsal"] == jugada.dorsal && jugador["equipo"] == jugada.equipo) {
                                jugador["tc3pA"] = jugador["tc3pA"].toString().toInt() - 1
                                jugador["puntos"] = jugador["puntos"].toString().toInt() - 3
                                val tiros = jugador["tiros"] as ArrayList<Map<String, Any>>
                                tiros.removeIf { it["x"] == jugada.coordenada_x && it["y"] == jugada.coordenada_y }
                                jugador["tiros"] = tiros
                                if (jugada.equipo == "Local") {
                                    binding.txtPuntosLocal.text = "" + (binding.txtPuntosLocal.text.toString().toInt() - 3)
                                } else {
                                    binding.txtPuntosVisitante.text = "" + (binding.txtPuntosVisitante.text.toString().toInt() - 3)
                                }
                                actualizaResultado()
                                db.collection("Estadisticas")
                                    .document(idPartido)
                                    .update(
                                        hashMapOf(
                                            listJugador[j] to jugador
                                        ) as Map<String, Any>
                                    ).addOnSuccessListener {
                                        calcularVal(listJugador[j], jugador)
                                    }
                                break
                            }
                        }
                    }
            }
            //Asistencia
            "9" -> {
                db.collection("Estadisticas").document(idPartido).get()
                    .addOnSuccessListener {
                        val listJugador = it.get("ListadoJugadores") as ArrayList<String>
                        for (j in 0..<listJugador.count()) {
                            val jugador = (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()
                            if (jugador["dorsal"] == jugada.dorsal && jugador["equipo"] == jugada.equipo) {
                                jugador["asi"] = jugador["asi"].toString().toInt() - 1
                                db.collection("Estadisticas")
                                    .document(idPartido)
                                    .update(
                                        hashMapOf(
                                            listJugador[j] to jugador
                                        ) as Map<String, Any>
                                    ).addOnSuccessListener {
                                        calcularVal(listJugador[j], jugador)
                                    }
                                break
                            }
                        }
                    }
            }
            //Perdidas
            "10" -> {
                db.collection("Estadisticas").document(idPartido).get()
                    .addOnSuccessListener {
                        val listJugador = it.get("ListadoJugadores") as ArrayList<String>
                        for (j in 0..<listJugador.count()) {
                            val jugador = (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()
                            if (jugador["dorsal"] == jugada.dorsal && jugador["equipo"] == jugada.equipo) {
                                jugador["per"] = jugador["per"].toString().toInt() - 1
                                db.collection("Estadisticas")
                                    .document(idPartido)
                                    .update(
                                        hashMapOf(
                                            listJugador[j] to jugador
                                        ) as Map<String, Any>
                                    ).addOnSuccessListener {
                                        calcularVal(listJugador[j], jugador)
                                    }
                                break
                            }
                        }
                    }
            }
            //Recuperacion
            "11" -> {
                db.collection("Estadisticas").document(idPartido).get()
                    .addOnSuccessListener {
                        val listJugador = it.get("ListadoJugadores") as ArrayList<String>
                        for (j in 0..<listJugador.count()) {
                            val jugador = (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()
                            if (jugador["dorsal"] == jugada.dorsal && jugador["equipo"] == jugada.equipo) {
                                jugador["recu"] = jugador["recu"].toString().toInt() - 1
                                db.collection("Estadisticas")
                                    .document(idPartido)
                                    .update(
                                        hashMapOf(
                                            listJugador[j] to jugador
                                        ) as Map<String, Any>
                                    ).addOnSuccessListener {
                                        calcularVal(listJugador[j], jugador)
                                    }
                                break
                            }
                        }
                    }
            }
            //Tapon recibido
            "12" -> {
                db.collection("Estadisticas").document(idPartido).get()
                    .addOnSuccessListener {
                        val listJugador = it.get("ListadoJugadores") as ArrayList<String>
                        for (j in 0..<listJugador.count()) {
                            val jugador = (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()
                            if (jugador["dorsal"] == jugada.dorsal && jugador["equipo"] == jugada.equipo) {
                                jugador["taRec"] = jugador["taRec"].toString().toInt() - 1
                                db.collection("Estadisticas")
                                    .document(idPartido)
                                    .update(
                                        hashMapOf(
                                            listJugador[j] to jugador
                                        ) as Map<String, Any>
                                    ).addOnSuccessListener {
                                        calcularVal(listJugador[j], jugador)
                                    }
                                break
                            }
                        }
                    }
            }
            //Tapon cometido
            "13" -> {
                db.collection("Estadisticas").document(idPartido).get()
                    .addOnSuccessListener {
                        val listJugador = it.get("ListadoJugadores") as ArrayList<String>
                        for (j in 0..<listJugador.count()) {
                            val jugador = (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()
                            if (jugador["dorsal"] == jugada.dorsal && jugador["equipo"] == jugada.equipo) {
                                jugador["taCom"] = jugador["taCom"].toString().toInt() - 1
                                db.collection("Estadisticas")
                                    .document(idPartido)
                                    .update(
                                        hashMapOf(
                                            listJugador[j] to jugador
                                        ) as Map<String, Any>
                                    ).addOnSuccessListener {
                                        calcularVal(listJugador[j], jugador)
                                    }
                                break
                            }
                        }
                    }
            }
            //Rebotes
            "14" -> {
                db.collection("Estadisticas").document(idPartido).get()
                    .addOnSuccessListener {
                        val listJugador = it.get("ListadoJugadores") as ArrayList<String>
                        for (j in 0..<listJugador.count()) {
                            val jugador = (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()
                            if (jugador["dorsal"] == jugada.dorsal && jugador["equipo"] == jugada.equipo) {
                                if (jugada.frase == "REBOTE OFENSIVO") {
                                    jugador["rebO"] = jugador["rebO"].toString().toInt() - 1
                                } else {
                                    jugador["rebD"] = jugador["rebD"].toString().toInt() - 1
                                }
                                db.collection("Estadisticas")
                                    .document(idPartido)
                                    .update(
                                        hashMapOf(
                                            listJugador[j] to jugador
                                        ) as Map<String, Any>
                                    ).addOnSuccessListener {
                                        calcularVal(listJugador[j], jugador)
                                    }
                                break
                            }
                        }
                    }
            }
        }
    }

    private fun calcularVal(idJugador: String, jugador: MutableMap<String?, Any?>) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()
        val valoracion: Int = (
                jugador["puntos"].toString().toInt() + jugador["asi"].toString().toInt() + jugador["rebO"].toString()
                    .toInt() + jugador["rebD"].toString().toInt() + jugador["recu"].toString().toInt() + jugador["taCom"].toString()
                    .toInt() + jugador["falR"].toString().toInt()) - (jugador["falC"].toString().toInt() + jugador["tc2pF"].toString()
            .toInt() + jugador["tc3pF"].toString().toInt() + jugador["per"].toString().toInt() + jugador["taRec"].toString()
            .toInt() + jugador["tlF"].toString().toInt())
        jugador["val"] = valoracion
        db.collection("Estadisticas")
            .document(idPartido)
            .update(
                hashMapOf(
                    idJugador to jugador
                ) as Map<String, Any>
            )
    }

    private fun llenarListToggleLocal(): ArrayList<ToggleButton> {
        val lista: ArrayList<ToggleButton> = java.util.ArrayList<ToggleButton>()
        lista.add(binding.TBLocal1)
        lista.add(binding.TBLocal2)
        lista.add(binding.TBLocal3)
        lista.add(binding.TBLocal4)
        lista.add(binding.TBLocal5)
        return lista
    }

    private fun llenarListToggleVisitante(): ArrayList<ToggleButton> {
        val lista: ArrayList<ToggleButton> = java.util.ArrayList<ToggleButton>()
        lista.add(binding.TBVisitante1)
        lista.add(binding.TBVisitante2)
        lista.add(binding.TBVisitante3)
        lista.add(binding.TBVisitante4)
        lista.add(binding.TBVisitante5)
        return lista
    }

    private fun llenarListToggle(): ArrayList<ToggleButton> {
        val lista: ArrayList<ToggleButton> = java.util.ArrayList<ToggleButton>()
        lista.add(binding.TBLocal1)
        lista.add(binding.TBLocal2)
        lista.add(binding.TBLocal3)
        lista.add(binding.TBLocal4)
        lista.add(binding.TBLocal5)
        lista.add(binding.TBVisitante1)
        lista.add(binding.TBVisitante2)
        lista.add(binding.TBVisitante3)
        lista.add(binding.TBVisitante4)
        lista.add(binding.TBVisitante5)
        return lista
    }

    private fun comprobarEquipoJugador(toggleButtons: ArrayList<ToggleButton>): String {
        for (button in toggleButtons) {
            if (button.isChecked) {
                val index = toggleButtons.indexOf(button)
                return if (index < 5) "Local" else "Visitante"
            }
        }
        return "Ninguno"
    }

    private fun vaciarToggle(lista: ArrayList<ToggleButton>) {
        for (i in lista) {
            if (i.isChecked) {
                i.isChecked = false
            }
        }
    }

    private fun cargaEscudos(eLocal: String, eVisitante: String) {

        db.collection("Equipos").document(eLocal).get()
            .addOnSuccessListener {
                if (it.get("UrlFoto") != "") {
                    Picasso.get()
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.escudopredeterminado)
                        .error(R.drawable.escudopredeterminado)
                        .into(binding.escudoLocal)
                }
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }

        db.collection("Equipos").document(eVisitante).get()
            .addOnSuccessListener {
                if (it.get("UrlFoto") != "") {
                    Picasso.get()
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.escudopredeterminado)
                        .error(R.drawable.escudopredeterminado)
                        .into(binding.escudoVisitante)
                }
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }
}