package com.franciscolinares.ubb.invitado

import android.annotation.SuppressLint
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.ActivityMainEquipoInvitadoBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.util.Locale

class MainEquipoInvitadoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainEquipoInvitadoBinding
    private val db = Firebase.firestore
    var tabTitle = arrayOf("Partidos", "Jugadores")
    private var idEquipo: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainEquipoInvitadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var pager = binding.vPVistasInvitado
        var t1 = binding.tabs

        idEquipo = intent.getStringExtra("idEquipo")
        recuperarInfo()

        pager.adapter = MyAdapterInvitado(supportFragmentManager, lifecycle)
        TabLayoutMediator(t1, pager) { tab, position ->
            tab.text = tabTitle[position]
        }.attach()
    }

    @SuppressLint("SetTextI18n")
    private fun recuperarInfo() {

        db.collection("Equipos").document(idEquipo.toString()).get()
            .addOnSuccessListener {
                binding.txtEquipoInvitadoAB.text = it.get("Nombre").toString().uppercase(Locale.ROOT)
                binding.txtCategoriaInvitadoAB.text =
                    it.get("Categoria").toString().uppercase(Locale.ROOT) + " " + it.get("Sexo").toString().uppercase(Locale.ROOT)
                binding.txtLocalidadInvitadoAB.text = it.get("Localidad").toString().uppercase(Locale.ROOT)
                if (it.get("UrlFoto") != "") {
                    Picasso.get()
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.escudo_equipo)
                        .error(R.drawable.escudo_equipo)
                        .into(binding.logoEquipoInvitadoAB)
                }
            }

        db.collection("Partidos").get().addOnSuccessListener {
            var conPar = 0
            var conParG = 0
            var ppp = 0
            var ppc = 0
            for (partido in it) {
                if (partido["EquipoLocal"].toString() == idEquipo || partido["EquipoVisitante"].toString() == idEquipo) {
                    conPar++
                    if (partido["EquipoLocal"].toString() == idEquipo) {
                        ppp += partido["Resultado"].toString().split(" - ")[0].toInt()
                        ppc += partido["Resultado"].toString().split(" - ")[1].toInt()
                        if (comprobarGanador(partido["Resultado"].toString()) == 1)
                            conParG++
                    } else if (partido["EquipoVisitante"].toString() == idEquipo) {
                        ppp += partido["Resultado"].toString().split(" - ")[1].toInt()
                        ppc += partido["Resultado"].toString().split(" - ")[0].toInt()
                        if (comprobarGanador(partido["Resultado"].toString()) == 0)
                            conParG++
                    }

                }
            }
            val promedioPuntosF = if (conPar != 0) ppp.toFloat() / conPar else 0f
            val promedioPuntosFormateadoF = String.format(Locale.US, "%.1f", promedioPuntosF)
            val promedioPuntosC = if (conPar != 0) ppc.toFloat() / conPar else 0f
            val promedioPuntosFormateadoC = String.format(Locale.US, "%.1f", promedioPuntosC)
            binding.txtEquipoInvitadoPJ.text = "PJ: $conPar"
            binding.txtEquipoInvitadoPG.text = "PG: $conParG"
            binding.txtEquipoInvitadoPPP.text = "PPP: $promedioPuntosFormateadoF"
            binding.txtEquipoInvitadoPPC.text = "PPC: $promedioPuntosFormateadoC"
        }
    }

    private fun comprobarGanador(resultado: String): Int {
        val ptsL = resultado.split(" - ")[0].toInt()
        val ptsV = resultado.split(" - ")[1].toInt()

        return if (ptsL > ptsV) 1 else 0

    }
}