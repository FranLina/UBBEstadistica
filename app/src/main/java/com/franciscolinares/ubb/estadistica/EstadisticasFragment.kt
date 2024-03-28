package com.franciscolinares.ubb.estadistica

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentEstadisticasBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEstadisticasBinding.inflate(inflater, container, false)
        val root = binding.root

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()

        recuperaInfo()

        db.collection("Estadisticas").document(idPartido).get()
            .addOnSuccessListener {
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
                    db.collection("Partidos").document(idPartido).get()
                        .addOnSuccessListener { partido ->
                            if (partido.get("Estado") != "Finalizado") {
                                db.collection("Partidos").document(idPartido).get()
                                    .addOnSuccessListener { documentSnapshot ->
                                        binding.txtPuntosLocalPartido.text =
                                            (documentSnapshot.get("Resultado").toString()
                                                .split(" - "))[0]
                                        binding.txtPuntosVisitantePartido.text =
                                            (documentSnapshot.get("Resultado").toString()
                                                .split(" - "))[1]
                                    }

                                db.collection("Estadisticas").document(idPartido).get()
                                    .addOnSuccessListener {
                                        val listJugador =
                                            it.get("ListadoJugadores") as ArrayList<String>
                                        val newJugadores: ArrayList<Map<String?, Any?>> =
                                            arrayListOf()
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

    @SuppressLint("SetTextI18n")
    private fun recuperaInfo() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idPartido = prefs.getString("idPartido", "").toString()

        db.collection("Partidos").document(idPartido).get()
            .addOnSuccessListener {
                binding.txtNombreELocal.text = "  " + it.get("EquipoLocal").toString().toUpperCase(
                    Locale.ROOT)
                binding.txtNombreEVisitante.text = "  " + it.get("EquipoVisitante").toString().toUpperCase(Locale.ROOT)
                binding.txtNombreELocalEsta.text = it.get("EquipoLocal").toString().toUpperCase(Locale.ROOT)
                binding.txtNombreEVisitanteEsta.text = it.get("EquipoVisitante").toString().toUpperCase(Locale.ROOT)
                binding.txtPuntosLocalPartido.text =
                    (it.get("Resultado").toString().split(" - "))[0]
                binding.txtPuntosVisitantePartido.text =
                    (it.get("Resultado").toString().split(" - "))[1]
                cargaEscudos(it.get("EquipoLocal").toString(), it.get("EquipoVisitante").toString())
            }
    }

    @SuppressLint("SetTextI18n", "InflateParams", "ResourceAsColor")
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

        val cabeceraLPN = LayoutInflater.from(binding.root.context)
            .inflate(R.layout.row_cabecera_principal_nombre, null, false)
        val cabeceraLN = LayoutInflater.from(binding.root.context)
            .inflate(R.layout.row_cabecera_nombre, null, false)
        binding.TLLocalNombre.addView(cabeceraLPN)
        binding.TLLocalNombre.addView(cabeceraLN)

        val cabeceraLP = LayoutInflater.from(binding.root.context)
            .inflate(R.layout.row_cabecera_principal, null, false)
        val cabeceraL =
            LayoutInflater.from(binding.root.context).inflate(R.layout.row_cabecera, null, false)
        binding.TLLocal.addView(cabeceraLP)
        binding.TLLocal.addView(cabeceraL)

        val cabeceraVPN = LayoutInflater.from(binding.root.context)
            .inflate(R.layout.row_cabecera_principal_nombre, null, false)
        val cabeceraVN = LayoutInflater.from(binding.root.context)
            .inflate(R.layout.row_cabecera_nombre, null, false)
        binding.TLVisitanteNombre.addView(cabeceraVPN)
        binding.TLVisitanteNombre.addView(cabeceraVN)

        val cabeceraVP = LayoutInflater.from(binding.root.context)
            .inflate(R.layout.row_cabecera_principal, null, false)
        val cabeceraV =
            LayoutInflater.from(binding.root.context).inflate(R.layout.row_cabecera, null, false)
        binding.TLVisitante.addView(cabeceraVP)
        binding.TLVisitante.addView(cabeceraV)

        for (j in 0..<jugadores.count()) {

            val jugador = jugadores[j]

            val registro = LayoutInflater.from(binding.root.context)
                .inflate(R.layout.row_estadistica, null, false)

            val registroN = LayoutInflater.from(binding.root.context)
                .inflate(R.layout.row_estadistica_nombre, null, false)

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
                        " " + (jugador["dorsal"].toString() + " " + jugador["nombre"].toString()
                            .toUpperCase()).substring(
                            0,
                            16
                        )
                    }..."
                } else {
                    " " + jugador["dorsal"].toString() + " " + jugador["nombre"].toString()
                        .toUpperCase()
                }

            registro.findViewById<TextView>(R.id.txtEPuntos).text = jugador["puntos"].toString()
            registro.findViewById<TextView>(R.id.txtETC2P1).text =
                jugador["tc2pA"].toString() + "/" + (jugador["tc2pA"].toString()
                    .toInt() + jugador["tc2pF"].toString().toInt())
            registro.findViewById<TextView>(R.id.txtETC2P2).text =
                ((jugador["tc2pA"].toString().toDouble() / (jugador["tc2pA"].toString()
                    .toDouble() + jugador["tc2pF"].toString().toDouble())) * 100).toInt().toString()
            registro.findViewById<TextView>(R.id.txtETC3P1).text =
                jugador["tc3pA"].toString() + "/" + (jugador["tc3pA"].toString()
                    .toInt() + jugador["tc3pF"].toString().toInt())
            registro.findViewById<TextView>(R.id.txtETC3P2).text =
                ((jugador["tc3pA"].toString().toDouble() / (jugador["tc3pA"].toString()
                    .toDouble() + jugador["tc3pF"].toString().toDouble())) * 100).toInt().toString()
            registro.findViewById<TextView>(R.id.txtETL1).text =
                jugador["tlA"].toString() + "/" + (jugador["tlA"].toString()
                    .toInt() + jugador["tlF"].toString().toInt())
            registro.findViewById<TextView>(R.id.txtETL2).text =
                ((jugador["tlA"].toString().toDouble() / (jugador["tlA"].toString()
                    .toDouble() + jugador["tlF"].toString().toDouble())) * 100).toInt().toString()
            registro.findViewById<TextView>(R.id.txtERebO).text = jugador["rebO"].toString()
            registro.findViewById<TextView>(R.id.txtERebD).text = jugador["rebD"].toString()
            registro.findViewById<TextView>(R.id.txtERebT).text =
                (jugador["rebD"].toString().toInt() + jugador["rebO"].toString().toInt()).toString()
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

        val registroNTotalL = LayoutInflater.from(binding.root.context)
            .inflate(R.layout.row_estadistica_total, null, false)
        binding.TLLocalNombre.addView(registroNTotalL)

        val registroL = LayoutInflater.from(binding.root.context)
            .inflate(R.layout.row_estadistica_total_datos, null, false)
        registroL.findViewById<TextView>(R.id.txtEPuntos).text = ptsL.toString()
        registroL.findViewById<TextView>(R.id.txtETC2P1).text = tc2AL.toString()+"/"+(tc2AL+tc2FL).toString()
        registroL.findViewById<TextView>(R.id.txtETC2P2).text = ((tc2AL.toDouble() / (tc2AL.toDouble() + tc2FL.toDouble())) * 100).toInt().toString()
        registroL.findViewById<TextView>(R.id.txtETC3P1).text = tc3AL.toString()+"/"+(tc3AL+tc3FL).toString()
        registroL.findViewById<TextView>(R.id.txtETC3P2).text = ((tc3AL.toDouble() / (tc3AL.toDouble() + tc3FL.toDouble())) * 100).toInt().toString()
        registroL.findViewById<TextView>(R.id.txtETL1).text = tlAL.toString()+"/"+(tlAL+tlFL).toString()
        registroL.findViewById<TextView>(R.id.txtETL2).text = ((tlAL.toDouble() / (tlAL.toDouble() + tlFL.toDouble())) * 100).toInt().toString()
        registroL.findViewById<TextView>(R.id.txtERebO).text = rebOL.toString()
        registroL.findViewById<TextView>(R.id.txtERebD).text = rebDL.toString()
        registroL.findViewById<TextView>(R.id.txtERebT).text = (rebOL + rebDL).toString()
        registroL.findViewById<TextView>(R.id.txtEAsi).text = asiL.toString()
        registroL.findViewById<TextView>(R.id.txtERec).text =recL.toString()
        registroL.findViewById<TextView>(R.id.txtEPer).text = perL.toString()
        registroL.findViewById<TextView>(R.id.txtETapC).text = tapCL.toString()
        registroL.findViewById<TextView>(R.id.txtETapR).text = tapRL.toString()
        registroL.findViewById<TextView>(R.id.txtEFalC).text = falCL.toString()
        registroL.findViewById<TextView>(R.id.txtEFalR).text =falRL.toString()
        registroL.findViewById<TextView>(R.id.txtEVal).text = valL.toString()
        binding.TLLocal.addView(registroL)


        val registroNTotalV = LayoutInflater.from(binding.root.context)
            .inflate(R.layout.row_estadistica_total, null, false)
        binding.TLVisitanteNombre.addView(registroNTotalV)

        val registroV = LayoutInflater.from(binding.root.context)
            .inflate(R.layout.row_estadistica_total_datos, null, false)
        registroV.findViewById<TextView>(R.id.txtEPuntos).text = ptsV.toString()
        registroV.findViewById<TextView>(R.id.txtETC2P1).text = tc2AV.toString()+"/"+(tc2AV+tc2FV).toString()
        registroV.findViewById<TextView>(R.id.txtETC2P2).text = ((tc2AV.toDouble() / (tc2AV.toDouble() + tc2FV.toDouble())) * 100).toInt().toString()
        registroV.findViewById<TextView>(R.id.txtETC3P1).text = tc3AV.toString()+"/"+(tc3AV+tc3FV).toString()
        registroV.findViewById<TextView>(R.id.txtETC3P2).text = ((tc3AV.toDouble() / (tc3AV.toDouble() + tc3FV.toDouble())) * 100).toInt().toString()
        registroV.findViewById<TextView>(R.id.txtETL1).text = tlAV.toString()+"/"+(tlAV+tlFV).toString()
        registroV.findViewById<TextView>(R.id.txtETL2).text = ((tlAV.toDouble() / (tlAV.toDouble() + tlFV.toDouble())) * 100).toInt().toString()
        registroV.findViewById<TextView>(R.id.txtERebO).text = rebOV.toString()
        registroV.findViewById<TextView>(R.id.txtERebD).text = rebDV.toString()
        registroV.findViewById<TextView>(R.id.txtERebT).text = (rebOV + rebDV).toString()
        registroV.findViewById<TextView>(R.id.txtEAsi).text = asiV.toString()
        registroV.findViewById<TextView>(R.id.txtERec).text =recV.toString()
        registroV.findViewById<TextView>(R.id.txtEPer).text = perV.toString()
        registroV.findViewById<TextView>(R.id.txtETapC).text = tapCV.toString()
        registroV.findViewById<TextView>(R.id.txtETapR).text = tapRV.toString()
        registroV.findViewById<TextView>(R.id.txtEFalC).text = falCV.toString()
        registroV.findViewById<TextView>(R.id.txtEFalR).text =falRV.toString()
        registroV.findViewById<TextView>(R.id.txtEVal).text = valV.toString()
        binding.TLVisitante.addView(registroV)

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

        db.collection("Equipos").document(eLocal).get()
            .addOnSuccessListener {
                if (it.get("UrlFoto") != "") {
                    Picasso.get()
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.escudo_equipo)
                        .error(R.drawable.escudo_equipo)
                        .into(binding.imageLocalPartido)
                    Picasso.get()
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.escudo_equipo)
                        .error(R.drawable.escudo_equipo)
                        .into(binding.imageLocalPartidoEsta)
                }
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }

        db.collection("Equipos").document(eVisitante).get()
            .addOnSuccessListener {
                if (it.get("UrlFoto") != "") {
                    Picasso.get()
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.escudo_equipo)
                        .error(R.drawable.escudo_equipo)
                        .into(binding.imageVisitantePartido)
                    Picasso.get()
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.escudo_equipo)
                        .error(R.drawable.escudo_equipo)
                        .into(binding.imageVisitantePartidoEsta)
                }
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }
}