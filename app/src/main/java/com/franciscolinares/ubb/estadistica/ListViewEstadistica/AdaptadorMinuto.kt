package com.franciscolinares.ubb.estadistica.ListViewEstadistica

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet.Layout
import com.franciscolinares.ubb.R
import com.squareup.picasso.Picasso

class AdaptadorMinuto (private val mcontext: Context, private var listaMinuto: List<MinutoAMinuto>) :
    ArrayAdapter<MinutoAMinuto>(mcontext, 0, listaMinuto) {

    @SuppressLint("ViewHolder", "SetTextI18n", "MissingInflatedId", "CutPasteId", "RtlHardcoded")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var layout = LayoutInflater.from(mcontext).inflate(R.layout.minutoaminuto, parent, false)
        val minuto = listaMinuto[position]

        if(minuto.equipo=="Local"){
            layout.findViewById<View>(R.id.lineaSeparadora).setBackgroundColor(Color.parseColor("#FF3D00"))

        }else{
            layout = LayoutInflater.from(mcontext).inflate(R.layout.minutoaminuto_visitante, parent, false)
            layout.findViewById<View>(R.id.lineaSeparadora).setBackgroundColor(Color.parseColor("#00B0FF"))
        }

        cambiaFoto(minuto.imgAccion,layout.findViewById<ImageView>(R.id.imgAccionMAM))

        if (minuto.tipoFrase == "1") {
            layout.findViewById<TextView>(R.id.txtLVMFrase).text = minuto.frase
            layout.findViewById<TextView>(R.id.txtLVM).text = "#" + minuto.dorsal + ", p " + minuto.cuarto + ", " + minuto.tiempo
            layout.findViewById<TextView>(R.id.txtLVMResultado).visibility = View.VISIBLE
            layout.findViewById<TextView>(R.id.txtLVMResultado).text = minuto.resultado
        } else if (minuto.tipoFrase == "2") {
            layout.findViewById<ImageView>(R.id.imgAccionMAM).visibility = View.GONE
            layout.findViewById<TextView>(R.id.txtLVMFrase).text = minuto.frase + " " + minuto.cuarto
            layout.findViewById<TextView>(R.id.txtLVMFrase).setBackgroundColor(Color.parseColor("#FF4CAF50"))
            layout.findViewById<TextView>(R.id.txtLVMFrase).setTextColor(Color.WHITE)
            layout.findViewById<TextView>(R.id.txtLVMFrase).gravity = Gravity.CENTER
            layout.findViewById<TextView>(R.id.txtLVM).text = ""
            layout.findViewById<TextView>(R.id.txtLVM).gravity = Gravity.CENTER
            layout.findViewById<TextView>(R.id.txtLVM).setBackgroundColor(Color.parseColor("#FF4CAF50"))
            layout.findViewById<TextView>(R.id.txtLVM).setTextColor(Color.WHITE)
            layout.findViewById<View>(R.id.lineaSeparadora).visibility = View.GONE
        } else if (minuto.tipoFrase == "3") {
            layout.findViewById<TextView>(R.id.txtLVMFrase).text = minuto.frase + "#" + minuto.dorsal
            layout.findViewById<TextView>(R.id.txtLVM).text = "p " + minuto.cuarto + ", " + minuto.tiempo
        } else if (minuto.tipoFrase == "4") {
            layout.findViewById<ImageView>(R.id.imgAccionMAM).visibility = View.GONE
            layout.findViewById<TextView>(R.id.txtLVMFrase).text = minuto.frase + " " + minuto.cuarto
            layout.findViewById<TextView>(R.id.txtLVMFrase).setBackgroundColor(Color.parseColor("#3A3A3A"))
            layout.findViewById<TextView>(R.id.txtLVMFrase).setTextColor(Color.WHITE)
            layout.findViewById<TextView>(R.id.txtLVMFrase).gravity = Gravity.CENTER
            layout.findViewById<TextView>(R.id.txtLVM).text = ""
            layout.findViewById<TextView>(R.id.txtLVM).gravity = Gravity.CENTER
            layout.findViewById<TextView>(R.id.txtLVM).setBackgroundColor(Color.parseColor("#3A3A3A"))
            layout.findViewById<TextView>(R.id.txtLVM).setTextColor(Color.WHITE)
            layout.findViewById<View>(R.id.lineaSeparadora).visibility = View.GONE
        }

        return layout
    }

    fun updateData(newData: List<MinutoAMinuto>) {
        listaMinuto = newData
        notifyDataSetChanged() // Esto refresca la vista, pero no recrea todos los elementos
    }

    private fun cambiaFoto(tipoImg :String, imagenAccion :ImageView){

        when(tipoImg){
            "1"->{
                Picasso.get()
                    .load(R.drawable.cambio)
                    .placeholder(R.drawable.equipacion)
                    .error(R.drawable.equipacion)
                    .into(imagenAccion)
            }
            "2"->{
                Picasso.get()
                    .load(R.drawable.tmblanco)
                    .placeholder(R.drawable.equipacion)
                    .error(R.drawable.equipacion)
                    .into(imagenAccion)
            }
            "3"->{
                Picasso.get()
                    .load(R.drawable.faltanb)
                    .placeholder(R.drawable.equipacion)
                    .error(R.drawable.equipacion)
                    .into(imagenAccion)
            }
            "4"->{
                Picasso.get()
                    .load(R.drawable.tiro_libre)
                    .placeholder(R.drawable.equipacion)
                    .error(R.drawable.equipacion)
                    .into(imagenAccion)
            }
            "5"->{
                Picasso.get()
                    .load(R.drawable.canasta1)
                    .placeholder(R.drawable.equipacion)
                    .error(R.drawable.equipacion)
                    .into(imagenAccion)
            }
            "6"->{
                Picasso.get()
                    .load(R.drawable.canasta_fallada)
                    .placeholder(R.drawable.equipacion)
                    .error(R.drawable.equipacion)
                    .into(imagenAccion)
            }
            "7"->{
                Picasso.get()
                    .load(R.drawable.canasta2)
                    .placeholder(R.drawable.equipacion)
                    .error(R.drawable.equipacion)
                    .into(imagenAccion)
            }
            "8"->{
                Picasso.get()
                    .load(R.drawable.canasta3)
                    .placeholder(R.drawable.equipacion)
                    .error(R.drawable.equipacion)
                    .into(imagenAccion)
            }
            "9"->{
                Picasso.get()
                    .load(R.drawable.asistencia)
                    .placeholder(R.drawable.equipacion)
                    .error(R.drawable.equipacion)
                    .into(imagenAccion)
            }
            "10"->{
                Picasso.get()
                    .load(R.drawable.perdida)
                    .placeholder(R.drawable.equipacion)
                    .error(R.drawable.equipacion)
                    .into(imagenAccion)
            }
            "11"->{
                Picasso.get()
                    .load(R.drawable.recuperacion)
                    .placeholder(R.drawable.equipacion)
                    .error(R.drawable.equipacion)
                    .into(imagenAccion)
            }
            "12"->{
                Picasso.get()
                    .load(R.drawable.tapon_cometido)
                    .placeholder(R.drawable.equipacion)
                    .error(R.drawable.equipacion)
                    .into(imagenAccion)
            }
            "13"->{
                Picasso.get()
                    .load(R.drawable.tapon_recibido)
                    .placeholder(R.drawable.equipacion)
                    .error(R.drawable.equipacion)
                    .into(imagenAccion)
            }
            "14"->{
                Picasso.get()
                    .load(R.drawable.rebote)
                    .placeholder(R.drawable.equipacion)
                    .error(R.drawable.equipacion)
                    .into(imagenAccion)
            }

        }

    }

}