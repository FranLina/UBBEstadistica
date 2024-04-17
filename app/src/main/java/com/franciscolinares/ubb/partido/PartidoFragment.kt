package com.franciscolinares.ubb.partido

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Chronometer
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
import java.util.Locale

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

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n", "CutPasteId")
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
                    val minuto = MinutoAMinuto(
                        minutoMap["cuarto"].toString(),
                        minutoMap["dorsal"].toString(),
                        minutoMap["nombre"].toString(),
                        minutoMap["equipo"].toString(),
                        minutoMap["frase"].toString(),
                        minutoMap["resultado"].toString(),
                        minutoMap["tiempo"].toString(),
                        minutoMap["tipoFrase"].toString(),
                        minutoMap["tipoImg"].toString()
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
                view.findViewById<Button>(R.id.btnAnotar).text = "Recibida"
                view.findViewById<Button>(R.id.btnFallar).text = "Cometida"
                val dialog = builder.create()
                dialog.show()
                paraCronometro()

                falL = binding.txtFaltasLocal.text.toString().toInt()
                falV = binding.txtFaltasVisitante.text.toString().toInt()

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
                                                jugador["falR"] = jugador["falR"].toString().toInt() + 1
                                                db.collection("Estadisticas")
                                                    .document(idPartido)
                                                    .update(
                                                        hashMapOf(
                                                            listJugador[j] to jugador
                                                        ) as Map<String, Any>
                                                    ).addOnSuccessListener {
                                                        Toast.makeText(
                                                            binding.root.context,
                                                            "Falta reciba por el jugador " + lista[i].textOn + ", lleva " + jugador["falR"] + " faltas recibidas.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        calcularVal(listJugador[j], jugador)

                                                        db.collection("MinutoaMinuto")
                                                            .document(idPartido).get()
                                                            .addOnSuccessListener {
                                                                val listRegistros =
                                                                    it.get("registro") as ArrayList<Map<String?, Any?>>
                                                                val registro = hashMapOf(
                                                                    "cuarto" to cuarto,
                                                                    "dorsal" to lista[i].text,
                                                                    "nombre" to jugador["nombre"],
                                                                    "frase" to "FALTA RECIBIDA",
                                                                    "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                                    "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                    "equipo" to "Local",
                                                                    "tipoFrase" to "3",
                                                                    "tipoImg" to "3"
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
                            } else {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Visitante") {
                                                jugador["falR"] = jugador["falR"].toString().toInt() + 1
                                                db.collection("Estadisticas")
                                                    .document(idPartido)
                                                    .update(
                                                        hashMapOf(
                                                            listJugador[j] to jugador
                                                        ) as Map<String, Any>
                                                    ).addOnSuccessListener {
                                                        Toast.makeText(
                                                            binding.root.context,
                                                            "Falta recibida por el jugador " + lista[i].textOn + ", lleva " + jugador["falR"] + " faltas recibidas.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        calcularVal(listJugador[j], jugador)

                                                        db.collection("MinutoaMinuto")
                                                            .document(idPartido).get()
                                                            .addOnSuccessListener {
                                                                val listRegistros =
                                                                    it.get("registro") as ArrayList<Map<String?, Any?>>
                                                                val registro = hashMapOf(
                                                                    "cuarto" to cuarto,
                                                                    "dorsal" to lista[i].text,
                                                                    "nombre" to jugador["nombre"],
                                                                    "frase" to "FALTA RECIBIDA",
                                                                    "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                                    "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                    "equipo" to "Visitante",
                                                                    "tipoFrase" to "3",
                                                                    "tipoImg" to "3"
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
                                                jugador["falC"] = jugador["falC"].toString().toInt() + 1
                                                binding.txtFaltasLocal.text = (falL + 1).toString()
                                                falL += 1
                                                db.collection("Estadisticas")
                                                    .document(idPartido)
                                                    .update(
                                                        hashMapOf(
                                                            listJugador[j] to jugador
                                                        ) as Map<String, Any>
                                                    ).addOnSuccessListener {
                                                        Toast.makeText(
                                                            binding.root.context,
                                                            "Falta del jugador " + lista[i].textOn + ", lleva " + jugador["falC"] + " faltas.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        calcularVal(listJugador[j], jugador)
                                                        actualizaFaltaEquipo("FaltaL")

                                                        db.collection("MinutoaMinuto")
                                                            .document(idPartido).get()
                                                            .addOnSuccessListener {
                                                                val listRegistros =
                                                                    it.get("registro") as ArrayList<Map<String?, Any?>>
                                                                val registro = hashMapOf(
                                                                    "cuarto" to cuarto,
                                                                    "dorsal" to lista[i].text,
                                                                    "nombre" to jugador["nombre"],
                                                                    "frase" to "FALTA COMETIDA",
                                                                    "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                                    "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                    "equipo" to "Local",
                                                                    "tipoFrase" to "3",
                                                                    "tipoImg" to "3"
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
                                                                        if (jugador["falC"].toString() == "5")
                                                                            faltas5Local(
                                                                                llenarListToggleLocal(),
                                                                                lista[i]
                                                                            )
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
                                                jugador["falC"] = jugador["falC"].toString().toInt() + 1
                                                binding.txtFaltasVisitante.text = (falV + 1).toString()
                                                falV += 1
                                                db.collection("Estadisticas")
                                                    .document(idPartido)
                                                    .update(
                                                        hashMapOf(
                                                            listJugador[j] to jugador
                                                        ) as Map<String, Any>
                                                    ).addOnSuccessListener {
                                                        Toast.makeText(
                                                            binding.root.context,
                                                            "Falta del jugador " + lista[i].textOn + ", lleva " + jugador["falC"] + " faltas.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        calcularVal(listJugador[j], jugador)
                                                        actualizaFaltaEquipo("FaltaV")

                                                        db.collection("MinutoaMinuto")
                                                            .document(idPartido).get()
                                                            .addOnSuccessListener {
                                                                val listRegistros =
                                                                    it.get("registro") as ArrayList<Map<String?, Any?>>
                                                                val registro = hashMapOf(
                                                                    "cuarto" to cuarto,
                                                                    "dorsal" to lista[i].text,
                                                                    "nombre" to jugador["nombre"],
                                                                    "frase" to "FALTA COMETIDA",
                                                                    "resultado" to binding.txtPuntosLocal.text.toString() + "-" + binding.txtPuntosVisitante.text.toString(),
                                                                    "tiempo" to binding.TiempoCuarto.text.toString(),
                                                                    "equipo" to "Visitante",
                                                                    "tipoFrase" to "3",
                                                                    "tipoImg" to "3"
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
                                                                        if (jugador["falC"].toString() == "5")
                                                                            faltas5Visitante(
                                                                                llenarListToggleVisitante(),
                                                                                lista[i]
                                                                            )
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
                    dialog.hide()
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
                                                                    "tipoImg" to "7"
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


                            } else {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Visitante") {
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
                                                                    "tipoImg" to "7"
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
                                                                    "tipoImg" to "6"
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


                            } else {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Visitante") {
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
                                                                    "tipoImg" to "6"
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
                    }
                    vaciarToggle(lista)
                    dialog.hide()
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
                                                                    "tipoImg" to "8"
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


                            } else {
                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        for (j in 0..<listJugador.count()) {
                                            val jugador =
                                                (it.get(listJugador[j]) as Map<String?, Any?>).toMutableMap()

                                            if (jugador["dorsal"] == lista[i].text && jugador["equipo"] == "Visitante") {
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
                                                                    "tipoImg" to "8"
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
                                                            "tipoImg" to "6"
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
                                                            "tipoImg" to "6"
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

    private fun actualizaQuinteto(equipo: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()
        if (equipo == "QuintetoL") {
            db.collection("Partidos")
                .document(idPartido)
                .update(
                    hashMapOf(
                        equipo to quintetoL,
                    ) as Map<String, Any>
                )
        } else {
            db.collection("Partidos")
                .document(idPartido)
                .update(
                    hashMapOf(
                        equipo to quintetoV,
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
        }
    }

    @SuppressLint("SetTextI18n")
    private fun gestionCrononometro(chronometer: Chronometer) {
        if (chronometer.text.toString() == "00:00" && estado != "Finalizado") {
            val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
            val idPartido = prefs.getString("idPartido", "").toString()

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
                            "tiempo" to "",
                            "equipo" to "",
                            "tipoFrase" to "4"
                        ) as Map<String?, Any?>
                        listRegistros.add(registro)

                        val registro2 = hashMapOf(
                            "cuarto" to cuarto + 1,
                            "dorsal" to "",
                            "frase" to "INICIO DEL PERIODO",
                            "resultado" to "",
                            "tiempo" to "",
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
                                tiempo = "10:00"
                                binding.TiempoCuarto.text = tiempo
                                isPlay = false
                                pauseOffSet = 10 * 60 * 1000
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
                            "tiempo" to "",
                            "equipo" to "",
                            "tipoFrase" to "4"
                        ) as Map<String?, Any?>
                        listRegistros.add(registro)

                        val registro2 = hashMapOf(
                            "cuarto" to "",
                            "dorsal" to "",
                            "frase" to "FIN DEL PARTIDO",
                            "resultado" to "",
                            "tiempo" to "",
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
            } else if (cuarto >= 4 && binding.txtPuntosLocal.text == binding.txtPuntosVisitante.text) {
                db.collection("MinutoaMinuto").document(idPartido).get()
                    .addOnSuccessListener { it2 ->
                        val listRegistros = it2.get("registro") as ArrayList<Map<String?, Any?>>

                        val registro = hashMapOf(
                            "cuarto" to cuarto,
                            "dorsal" to "",
                            "frase" to "FIN DEL PERIODO",
                            "resultado" to "",
                            "tiempo" to "",
                            "equipo" to "",
                            "tipoFrase" to "4"
                        ) as Map<String?, Any?>
                        listRegistros.add(registro)

                        val registro2 = hashMapOf(
                            "cuarto" to cuarto + 1,
                            "dorsal" to "",
                            "frase" to "INICIO DEL PERIODO",
                            "resultado" to "",
                            "tiempo" to "",
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
                                tiempo = "05:00"
                                binding.TiempoCuarto.text = tiempo
                                isPlay = false
                                pauseOffSet = pauseOffSetProrroga
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
                            "tiempo" to "",
                            "equipo" to "",
                            "tipoFrase" to "4"
                        ) as Map<String?, Any?>
                        listRegistros.add(registro)

                        val registro2 = hashMapOf(
                            "cuarto" to "",
                            "dorsal" to "",
                            "frase" to "FIN DEL PARTIDO",
                            "resultado" to "",
                            "tiempo" to "",
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
                                        "tiempo" to "",
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
        if (estado != "Finalizado") {
            val builder = AlertDialog.Builder(binding.root.context)
            val view = layoutInflater.inflate(R.layout.cambios_equipo, null)

            val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
            val idPartido = prefs.getString("idPartido", "").toString()
            var contLoca: Int = 0
            var quinteto: Int = 0

            db.collection("Estadisticas").document(idPartido).get()
                .addOnSuccessListener { esta ->
                    val listJugador = esta.get("ListadoJugadores") as ArrayList<String>

                    val listToggleButton: ArrayList<ToggleButton> =
                        java.util.ArrayList<ToggleButton>()

                    for (i in 0..<listJugador.count()) {
                        val jugador = esta.get(listJugador[i].toString()) as Map<String?, Any?>
                        if (jugador["equipo"].toString() == "Local" && jugador["falC"].toString() != "5") {
                            if (jugador["dorsal"] != lista[0].text && jugador["dorsal"] != lista[1].text && jugador["dorsal"] != lista[2].text && jugador["dorsal"] != lista[3].text && jugador["dorsal"] != lista[4].text) {

                                val toggleButton: ToggleButton = ToggleButton(view.context)
                                toggleButton.text = jugador["dorsal"].toString()
                                toggleButton.id = contLoca
                                toggleButton.textOff = jugador["dorsal"].toString()
                                toggleButton.textOn = jugador["dorsal"].toString()
                                toggleButton.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonlocaldesactivado))
                                toggleButton.setTextColor(Color.BLACK)

                                if (contLoca < 5)
                                    view.findViewById<LinearLayout>(R.id.ContenedorCambios)
                                        .addView(toggleButton)
                                else
                                    view.findViewById<LinearLayout>(R.id.ContenedorCambios2)
                                        .addView(toggleButton)

                                toggleButton.setOnCheckedChangeListener { _, _ ->

                                    if (toggleButton.isChecked) {
                                        if (quinteto < 1) {
                                            toggleButton.setBackgroundDrawable(
                                                resources.getDrawable(
                                                    R.drawable.togglebuttonlocalactivado
                                                )
                                            )
                                            quinteto++
                                        } else {
                                            toggleButton.isChecked = false
                                            Toast.makeText(
                                                binding.root.context,
                                                "Solo se puede seleccionar 1 jugador",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        if (quinteto > 0) {
                                            toggleButton.setBackgroundDrawable(
                                                resources.getDrawable(
                                                    R.drawable.togglebuttonlocaldesactivado
                                                )
                                            )
                                            quinteto--
                                        }
                                    }
                                }
                                listToggleButton.add(toggleButton)
                                contLoca++
                            }
                        }

                    }

                    builder.setView(view)

                    val dialog = builder.create()
                    dialog.show()

                    view.findViewById<Button>(R.id.btnGuardarPlantillaCambios).setOnClickListener {
                        if (quinteto == 1) {
                            var cont = 0
                            db.collection("MinutoaMinuto").document(idPartido).get()
                                .addOnSuccessListener { it2 ->
                                    val listRegistros =
                                        it2.get("registro") as ArrayList<Map<String?, Any?>>
                                    for (j in listJugador) {
                                        val jugador = esta.get(j) as Map<String?, Any?>
                                        if (jugador["dorsal"].toString() == toggleButtonSale.text && jugador["equipo"].toString() == "Local") {
                                            val registro = hashMapOf(
                                                "cuarto" to cuarto,
                                                "dorsal" to toggleButtonSale.text,
                                                "nombre" to jugador["nombre"],
                                                "frase" to "ABANDONA LA PISTA EL ",
                                                "resultado" to "",
                                                "tiempo" to binding.TiempoCuarto.text.toString(),
                                                "equipo" to "Local",
                                                "tipoFrase" to "3",
                                                "tipoImg" to "1"
                                            ) as Map<String?, Any?>
                                            listRegistros.add(registro)
                                            quintetoL.remove(toggleButtonSale.text.toString())
                                        }
                                    }

                                    for (i in 0..<listToggleButton.count()) {
                                        val toggleButton = listToggleButton[i]
                                        if (toggleButton.isChecked) {
                                            if (cont == 0) {
                                                toggleButtonSale.text = toggleButton.text
                                                toggleButtonSale.textOff = toggleButton.textOff
                                                toggleButtonSale.textOn = toggleButton.textOn
                                            }
                                            for (j in listJugador) {
                                                val jugador = esta.get(j) as Map<String?, Any?>
                                                if (jugador["dorsal"].toString() == toggleButton.text && jugador["equipo"].toString() == "Local") {
                                                    val registro = hashMapOf(
                                                        "cuarto" to cuarto,
                                                        "dorsal" to toggleButton.text,
                                                        "nombre" to jugador["nombre"],
                                                        "frase" to "ENTRA A LA PISTA EL ",
                                                        "resultado" to "",
                                                        "tiempo" to binding.TiempoCuarto.text.toString(),
                                                        "equipo" to "Local",
                                                        "tipoFrase" to "3",
                                                        "tipoImg" to "1"
                                                    ) as Map<String?, Any?>
                                                    listRegistros.add(registro)
                                                    cont++
                                                    quintetoL.add(toggleButton.text.toString())
                                                    actualizaQuinteto("QuintetoL")
                                                }
                                            }
                                        }
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
                            dialog.hide()

                        } else {
                            Toast.makeText(
                                binding.root.context,
                                "Seleccione a 1 jugador para continuar",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            vaciarToggle(llenarListToggle())
            paraCronometro()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun hacerCambiosVisitante(lista: ArrayList<ToggleButton>, toggleButtonSale: ToggleButton) {
        if (estado != "Finalizado") {
            val builder = AlertDialog.Builder(binding.root.context)
            val view = layoutInflater.inflate(R.layout.cambios_equipo, null)

            val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
            val idPartido = prefs.getString("idPartido", "").toString()
            var contLoca: Int = 0
            var quinteto: Int = 0

            db.collection("Estadisticas").document(idPartido).get()
                .addOnSuccessListener { esta ->
                    val listJugador = esta.get("ListadoJugadores") as ArrayList<String>

                    val listToggleButton: ArrayList<ToggleButton> =
                        java.util.ArrayList<ToggleButton>()

                    for (i in 0..<listJugador.count()) {
                        val jugador = esta.get(listJugador[i].toString()) as Map<String?, Any?>
                        if (jugador["equipo"].toString() == "Visitante" && jugador["falC"].toString() != "5") {
                            if (jugador["dorsal"] != lista[0].text && jugador["dorsal"] != lista[1].text && jugador["dorsal"] != lista[2].text && jugador["dorsal"] != lista[3].text && jugador["dorsal"] != lista[4].text) {

                                val toggleButton: ToggleButton = ToggleButton(view.context)
                                toggleButton.text = jugador["dorsal"].toString()
                                toggleButton.id = contLoca
                                toggleButton.textOff = jugador["dorsal"].toString()
                                toggleButton.textOn = jugador["dorsal"].toString()
                                toggleButton.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonvisitantedesactivado))
                                toggleButton.setTextColor(Color.BLACK)

                                if (contLoca < 5)
                                    view.findViewById<LinearLayout>(R.id.ContenedorCambios)
                                        .addView(toggleButton)
                                else
                                    view.findViewById<LinearLayout>(R.id.ContenedorCambios2)
                                        .addView(toggleButton)

                                toggleButton.setOnCheckedChangeListener { _, _ ->

                                    if (toggleButton.isChecked) {
                                        if (quinteto < 1) {
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
                                                "Solo se puede seleccionar 1 jugador",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        if (quinteto > 0) {
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
                                contLoca++
                            }
                        }

                    }

                    builder.setView(view)

                    val dialog = builder.create()
                    dialog.show()

                    view.findViewById<Button>(R.id.btnGuardarPlantillaCambios).setOnClickListener {
                        if (quinteto == 1) {
                            var cont = 0
                            db.collection("MinutoaMinuto").document(idPartido).get()
                                .addOnSuccessListener { it2 ->
                                    val listRegistros =
                                        it2.get("registro") as ArrayList<Map<String?, Any?>>

                                    for (j in listJugador) {
                                        val jugador = esta.get(j) as Map<String?, Any?>
                                        if (jugador["dorsal"].toString() == toggleButtonSale.text && jugador["equipo"].toString() == "Visitante") {
                                            val registro = hashMapOf(
                                                "cuarto" to cuarto,
                                                "dorsal" to toggleButtonSale.text,
                                                "nombre" to jugador["nombre"],
                                                "frase" to "ABANDONA LA PISTA EL ",
                                                "resultado" to "",
                                                "tiempo" to binding.TiempoCuarto.text.toString(),
                                                "equipo" to "Visitante",
                                                "tipoFrase" to "3",
                                                "tipoImg" to "1"
                                            ) as Map<String?, Any?>
                                            listRegistros.add(registro)
                                            quintetoV.remove(toggleButtonSale.text.toString())
                                        }
                                    }


                                    for (i in 0..<listToggleButton.count()) {
                                        val toggleButton = listToggleButton[i]
                                        if (toggleButton.isChecked) {
                                            if (cont == 0) {
                                                toggleButtonSale.text = toggleButton.text
                                                toggleButtonSale.textOff = toggleButton.textOff
                                                toggleButtonSale.textOn = toggleButton.textOn
                                            }

                                            for (j in listJugador) {
                                                val jugador = esta.get(j) as Map<String?, Any?>
                                                if (jugador["dorsal"] == toggleButton.text && jugador["equipo"] == "Visitante") {
                                                    val registro = hashMapOf(
                                                        "cuarto" to cuarto,
                                                        "dorsal" to toggleButton.text,
                                                        "nombre" to jugador["nombre"],
                                                        "frase" to "ENTRA A LA PISTA EL ",
                                                        "resultado" to "",
                                                        "tiempo" to binding.TiempoCuarto.text.toString(),
                                                        "equipo" to "Visitante",
                                                        "tipoFrase" to "3",
                                                        "tipoImg" to "1"
                                                    ) as Map<String?, Any?>
                                                    listRegistros.add(registro)
                                                    cont++
                                                    quintetoV.add(toggleButton.text.toString())
                                                    actualizaQuinteto("QuintetoV")
                                                }
                                            }
                                        }
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
                            dialog.hide()

                        } else {
                            Toast.makeText(
                                binding.root.context,
                                "Seleccione a 1 jugador para continuar",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            vaciarToggle(llenarListToggle())
            paraCronometro()
        }
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

        val cabeceraFalta = LayoutInflater.from(binding.root.context).inflate(R.layout.row_cabecera_falta, null, false)
        tlMuestraFalta.addView(cabeceraFalta)

        db.collection("Estadisticas").document(idPartido).get()
            .addOnSuccessListener {
                val listJugador = it.get("ListadoJugadores") as ArrayList<String>
                for (j in 0..<listJugador.count()) {
                    val jugador = it.get(listJugador[j]) as Map<String?, Any?>
                    if (jugador["equipo"] == equipo) {
                        val filaFalta = LayoutInflater.from(binding.root.context).inflate(R.layout.row_falta, null, false)

                        if (j % 2 != 0) {
                            filaFalta.findViewById<TableRow>(R.id.filaFalta).setBackgroundColor(
                                Color.parseColor("#FFE4E4E4")
                            )
                        }

                        filaFalta.findViewById<TextView>(R.id.txtFaltaDorsal).text = jugador["dorsal"].toString()
                        filaFalta.findViewById<TextView>(R.id.txtFaltaNombre).text = jugador["nombre"].toString().toUpperCase(Locale.ROOT)
                        filaFalta.findViewById<TextView>(R.id.txtFaltaFalta).text = jugador["falC"].toString()
                        if (jugador["falC"].toString().toInt() == 5)
                            filaFalta.findViewById<TextView>(R.id.txtFaltaFalta).setTextColor(Color.RED)
                        else if (jugador["falC"].toString().toInt() >= 3)
                            filaFalta.findViewById<TextView>(R.id.txtFaltaFalta).setTextColor(Color.parseColor("#FBC02D"))
                        tlMuestraFalta.addView(filaFalta)
                    }
                }
            }

        val dialog = builder.create()
        dialog.show()
    }

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    private fun faltas5Local(lista: ArrayList<ToggleButton>, toggleButtonSale: ToggleButton) {
        val builder = AlertDialog.Builder(binding.root.context)
        val view = layoutInflater.inflate(R.layout.cambios_equipo, null)
        view.findViewById<TextView>(R.id.txtQuinteto).text =
            "El jugador #" + toggleButtonSale.text + " lleva 5 faltas, por lo que esta expulsado."

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()
        var contLoca: Int = 0
        var quinteto: Int = 0

        db.collection("Estadisticas").document(idPartido).get()
            .addOnSuccessListener { esta ->
                val listJugador = esta.get("ListadoJugadores") as ArrayList<String>

                val listToggleButton: ArrayList<ToggleButton> =
                    java.util.ArrayList<ToggleButton>()

                for (i in 0..<listJugador.count()) {
                    val jugador = esta.get(listJugador[i].toString()) as Map<String?, Any?>
                    if (jugador["equipo"].toString() == "Local" && jugador["falC"].toString() != "5") {
                        if (jugador["dorsal"] != lista[0].text && jugador["dorsal"] != lista[1].text && jugador["dorsal"] != lista[2].text && jugador["dorsal"] != lista[3].text && jugador["dorsal"] != lista[4].text) {

                            val toggleButton: ToggleButton = ToggleButton(view.context)
                            toggleButton.text = jugador["dorsal"].toString()
                            toggleButton.id = contLoca
                            toggleButton.textOff = jugador["dorsal"].toString()
                            toggleButton.textOn = jugador["dorsal"].toString()
                            toggleButton.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonlocaldesactivado))
                            toggleButton.setTextColor(Color.BLACK)
                            if (contLoca < 5)
                                view.findViewById<LinearLayout>(R.id.ContenedorCambios)
                                    .addView(toggleButton)
                            else
                                view.findViewById<LinearLayout>(R.id.ContenedorCambios2)
                                    .addView(toggleButton)

                            toggleButton.setOnCheckedChangeListener { _, _ ->

                                if (toggleButton.isChecked) {
                                    if (quinteto < 1) {
                                        toggleButton.setBackgroundDrawable(
                                            resources.getDrawable(
                                                R.drawable.togglebuttonlocalactivado
                                            )
                                        )
                                        quinteto++
                                    } else {
                                        toggleButton.isChecked = false
                                        Toast.makeText(
                                            binding.root.context,
                                            "Solo se puede seleccionar 1 jugador",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    if (quinteto > 0) {
                                        toggleButton.setBackgroundDrawable(
                                            resources.getDrawable(
                                                R.drawable.togglebuttonlocaldesactivado
                                            )
                                        )
                                        quinteto--
                                    }
                                }
                            }
                            listToggleButton.add(toggleButton)
                            contLoca++
                        }
                    }

                }

                builder.setView(view)

                val dialog = builder.create()
                dialog.show()

                view.findViewById<Button>(R.id.btnGuardarPlantillaCambios).setOnClickListener {
                    if (quinteto == 1 && contLoca > 0) {
                        var cont = 0
                        db.collection("MinutoaMinuto").document(idPartido).get()
                            .addOnSuccessListener { it2 ->
                                val listRegistros =
                                    it2.get("registro") as ArrayList<Map<String?, Any?>>

                                for (j in listJugador) {
                                    val jugador = esta.get(j) as Map<String?, Any?>
                                    if (jugador["dorsal"].toString() == toggleButtonSale.text && jugador["equipo"].toString() == "Local") {
                                        val registro = hashMapOf(
                                            "cuarto" to cuarto,
                                            "dorsal" to toggleButtonSale.text,
                                            "nombre" to jugador["nombre"],
                                            "frase" to "ABANDONA LA PISTA EL ",
                                            "resultado" to "",
                                            "tiempo" to binding.TiempoCuarto.text.toString(),
                                            "equipo" to "Local",
                                            "tipoFrase" to "3",
                                            "tipoImg" to "1"
                                        ) as Map<String?, Any?>
                                        listRegistros.add(registro)
                                        quintetoL.remove(toggleButtonSale.text.toString())
                                        actualizaQuinteto("QuintetoL")
                                    }
                                }

                                for (i in 0..<listToggleButton.count()) {
                                    val toggleButton = listToggleButton[i]
                                    if (toggleButton.isChecked) {
                                        if (cont == 0) {
                                            toggleButtonSale.text = toggleButton.text
                                            toggleButtonSale.textOff = toggleButton.textOff
                                            toggleButtonSale.textOn = toggleButton.textOn
                                        }

                                        for (j in listJugador) {
                                            val jugador = esta.get(j) as Map<String?, Any?>
                                            if (jugador["dorsal"].toString() == toggleButton.text && jugador["equipo"].toString() == "Local") {
                                                val registro = hashMapOf(
                                                    "cuarto" to cuarto,
                                                    "dorsal" to toggleButton.text,
                                                    "nombre" to jugador["nombre"],
                                                    "frase" to "ENTRA A LA PISTA EL ",
                                                    "resultado" to "",
                                                    "tiempo" to binding.TiempoCuarto.text.toString(),
                                                    "equipo" to "Local",
                                                    "tipoFrase" to "3",
                                                    "tipoImg" to "1"
                                                ) as Map<String?, Any?>
                                                listRegistros.add(registro)
                                                cont++
                                                quintetoL.add(toggleButton.text.toString())
                                                actualizaQuinteto("QuintetoL")
                                            }
                                        }
                                    }
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
                        dialog.hide()
                    } else if (quinteto == 0 && contLoca == 0) {
                        db.collection("MinutoaMinuto").document(idPartido).get()
                            .addOnSuccessListener { it2 ->
                                val listRegistros =
                                    it2.get("registro") as ArrayList<Map<String?, Any?>>

                                for (j in listJugador) {
                                    val jugador = esta.get(j) as Map<String?, Any?>
                                    if (jugador["dorsal"].toString() == toggleButtonSale.text && jugador["equipo"].toString() == "Local") {
                                        val registro = hashMapOf(
                                            "cuarto" to cuarto,
                                            "dorsal" to toggleButtonSale.text,
                                            "nombre" to jugador["nombre"],
                                            "frase" to "ABANDONA LA PISTA EL ",
                                            "resultado" to "",
                                            "tiempo" to binding.TiempoCuarto.text.toString(),
                                            "equipo" to "Local",
                                            "tipoFrase" to "3",
                                            "tipoImg" to "1"
                                        ) as Map<String?, Any?>
                                        listRegistros.add(registro)
                                        quintetoL.remove(toggleButtonSale.text.toString())
                                        actualizaQuinteto("QuintetoL")
                                        toggleButtonSale.text = " "
                                        toggleButtonSale.textOff = " "
                                        toggleButtonSale.textOn = " "
                                    }
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
                        dialog.hide()

                    } else {
                        Toast.makeText(
                            binding.root.context,
                            "Seleccione a 1 jugador para continuar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
        vaciarToggle(llenarListToggle())
        actualizaTiempo()

    }

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    private fun faltas5Visitante(lista: ArrayList<ToggleButton>, toggleButtonSale: ToggleButton) {
        val builder = AlertDialog.Builder(binding.root.context)
        val view = layoutInflater.inflate(R.layout.cambios_equipo, null)

        view.findViewById<TextView>(R.id.txtQuinteto).text =
            "El jugador #" + toggleButtonSale.text + " lleva 5 faltas, por lo que esta expulsado."

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()
        var contLoca: Int = 0
        var quinteto: Int = 0

        db.collection("Estadisticas").document(idPartido).get()
            .addOnSuccessListener { esta ->
                val listJugador = esta.get("ListadoJugadores") as ArrayList<String>

                val listToggleButton: ArrayList<ToggleButton> =
                    java.util.ArrayList<ToggleButton>()

                for (i in 0..<listJugador.count()) {
                    val jugador = esta.get(listJugador[i].toString()) as Map<String?, Any?>
                    if (jugador["equipo"].toString() == "Visitante" && jugador["falC"].toString() != "5") {
                        if (jugador["dorsal"] != lista[0].text && jugador["dorsal"] != lista[1].text && jugador["dorsal"] != lista[2].text && jugador["dorsal"] != lista[3].text && jugador["dorsal"] != lista[4].text) {
                            val toggleButton: ToggleButton = ToggleButton(view.context)
                            toggleButton.text = jugador["dorsal"].toString()
                            toggleButton.id = contLoca
                            toggleButton.textOff = jugador["dorsal"].toString()
                            toggleButton.textOn = jugador["dorsal"].toString()
                            toggleButton.setBackgroundDrawable(resources.getDrawable(R.drawable.togglebuttonvisitantedesactivado))
                            toggleButton.setTextColor(Color.BLACK)

                            if (contLoca < 5)
                                view.findViewById<LinearLayout>(R.id.ContenedorCambios)
                                    .addView(toggleButton)
                            else
                                view.findViewById<LinearLayout>(R.id.ContenedorCambios2)
                                    .addView(toggleButton)

                            toggleButton.setOnCheckedChangeListener { _, _ ->

                                if (toggleButton.isChecked) {
                                    if (quinteto < 1) {
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
                                            "Solo se puede seleccionar 1 jugador",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    if (quinteto > 0) {
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
                            contLoca++
                        }
                    }

                }

                builder.setView(view)

                val dialog = builder.create()
                dialog.show()

                view.findViewById<Button>(R.id.btnGuardarPlantillaCambios).setOnClickListener {
                    if (quinteto == 1 && contLoca > 0) {
                        var cont = 0
                        db.collection("MinutoaMinuto").document(idPartido).get()
                            .addOnSuccessListener { it2 ->
                                val listRegistros =
                                    it2.get("registro") as ArrayList<Map<String?, Any?>>

                                for (j in listJugador) {
                                    val jugador = esta.get(j) as Map<String?, Any?>
                                    if (jugador["dorsal"].toString() == toggleButtonSale.text && jugador["equipo"].toString() == "Visitante") {
                                        val registro = hashMapOf(
                                            "cuarto" to cuarto,
                                            "dorsal" to toggleButtonSale.text,
                                            "nombre" to jugador["nombre"],
                                            "frase" to "ABANDONA LA PISTA EL ",
                                            "resultado" to "",
                                            "tiempo" to binding.TiempoCuarto.text.toString(),
                                            "equipo" to "Visitante",
                                            "tipoFrase" to "3",
                                            "tipoImg" to "1"
                                        ) as Map<String?, Any?>
                                        listRegistros.add(registro)
                                        quintetoV.remove(toggleButtonSale.text.toString())
                                        actualizaQuinteto("QuintetoV")
                                    }
                                }

                                for (i in 0..<listToggleButton.count()) {
                                    val toggleButton = listToggleButton[i]
                                    if (toggleButton.isChecked) {
                                        if (cont == 0) {
                                            toggleButtonSale.text = toggleButton.text
                                            toggleButtonSale.textOff = toggleButton.textOff
                                            toggleButtonSale.textOn = toggleButton.textOn
                                        }
                                        for (j in listJugador) {
                                            val jugador = esta.get(j) as Map<String?, Any?>
                                            if (jugador["dorsal"].toString() == toggleButton.text && jugador["equipo"].toString() == "Visitante") {
                                                val registro = hashMapOf(
                                                    "cuarto" to cuarto,
                                                    "dorsal" to toggleButton.text,
                                                    "nombre" to jugador["nombre"],
                                                    "frase" to "ENTRA A LA PISTA EL ",
                                                    "resultado" to "",
                                                    "tiempo" to binding.TiempoCuarto.text.toString(),
                                                    "equipo" to "Visitante",
                                                    "tipoFrase" to "3",
                                                    "tipoImg" to "1"
                                                ) as Map<String?, Any?>
                                                listRegistros.add(registro)
                                                cont++
                                                quintetoV.add(toggleButton.text.toString())
                                                actualizaQuinteto("QuintetoV")
                                            }
                                        }
                                    }
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
                        dialog.hide()
                    } else if (quinteto == 0 && contLoca == 0) {
                        db.collection("MinutoaMinuto").document(idPartido).get()
                            .addOnSuccessListener { it2 ->
                                val listRegistros =
                                    it2.get("registro") as ArrayList<Map<String?, Any?>>

                                for (j in listJugador) {
                                    val jugador = esta.get(j) as Map<String?, Any?>
                                    if (jugador["dorsal"].toString() == toggleButtonSale.text && jugador["equipo"].toString() == "Visitante") {
                                        val registro = hashMapOf(
                                            "cuarto" to cuarto,
                                            "dorsal" to toggleButtonSale.text,
                                            "nombre" to jugador["nombre"],
                                            "frase" to "ABANDONA LA PISTA EL ",
                                            "resultado" to "",
                                            "tiempo" to binding.TiempoCuarto.text.toString(),
                                            "equipo" to "Visitante",
                                            "tipoFrase" to "3",
                                            "tipoImg" to "1"
                                        ) as Map<String?, Any?>
                                        listRegistros.add(registro)
                                        quintetoV.remove(toggleButtonSale.text.toString())
                                        actualizaQuinteto("QuintetoV")
                                        toggleButtonSale.text = " "
                                        toggleButtonSale.textOff = " "
                                        toggleButtonSale.textOn = " "
                                    }
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
                        dialog.hide()
                    } else {
                        Toast.makeText(
                            binding.root.context,
                            "Seleccione a 1 jugador para continuar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
        vaciarToggle(llenarListToggle())
        actualizaTiempo()

    }

    private fun colocarQuinteto(equipo: String, listToggleButton: ArrayList<ToggleButton>) {
        if (equipo == "Local") {
            for ((cont, quin) in quintetoL.withIndex()) {
                listToggleButton[cont].text = quin
                listToggleButton[cont].textOff = quin
                listToggleButton[cont].textOn = quin
            }
        } else {
            for ((cont, quin) in quintetoV.withIndex()) {
                listToggleButton[cont].text = quin
                listToggleButton[cont].textOff = quin
                listToggleButton[cont].textOn = quin
            }
        }
    }

    private fun mostrarJugadoresCampo() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()

        db.collection("Estadisticas").document(idPartido).get()
            .addOnSuccessListener { esta ->
                val listJugador = esta.get("ListadoJugadores") as ArrayList<String>
                db.collection("MinutoaMinuto").document(idPartido).get()
                    .addOnSuccessListener { it2 ->
                        val listRegistros = it2.get("registro") as ArrayList<Map<String?, Any?>>

                        for (j in listJugador) {
                            val jugador = esta.get(j) as Map<String?, Any?>
                            for (campo in quintetoL) {
                                if (jugador["dorsal"].toString() == campo && jugador["equipo"].toString() == "Local") {
                                    val registro = hashMapOf(
                                        "cuarto" to cuarto,
                                        "dorsal" to campo,
                                        "nombre" to jugador["nombre"],
                                        "frase" to "ENTRA A LA PISTA EL ",
                                        "resultado" to "",
                                        "tiempo" to binding.TiempoCuarto.text.toString(),
                                        "equipo" to "Local",
                                        "tipoFrase" to "3",
                                        "tipoImg" to "1"
                                    ) as Map<String?, Any?>
                                    listRegistros.add(registro)
                                }
                            }
                        }
                        for (j in listJugador) {
                            val jugador = esta.get(j) as Map<String?, Any?>
                            for (campo in quintetoV) {
                                if (jugador["dorsal"].toString() == campo && jugador["equipo"].toString() == "Visitante") {
                                    val registro = hashMapOf(
                                        "cuarto" to cuarto,
                                        "dorsal" to campo,
                                        "nombre" to jugador["nombre"],
                                        "frase" to "ENTRA A LA PISTA EL ",
                                        "resultado" to "",
                                        "tiempo" to binding.TiempoCuarto.text.toString(),
                                        "equipo" to "Visitante",
                                        "tipoFrase" to "3",
                                        "tipoImg" to "1"
                                    ) as Map<String?, Any?>
                                    listRegistros.add(registro)
                                }
                            }
                        }

                        db.collection("MinutoaMinuto")
                            .document(idPartido)
                            .update(
                                hashMapOf(
                                    "registro" to listRegistros,
                                ) as Map<String?, Any?>
                            )
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
                dialog.hide()
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
                                    }
                                    "TIRO DE 3 FALLADO" -> {
                                        jugador["tc3pF"] = jugador["tc3pF"].toString().toInt() - 1
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