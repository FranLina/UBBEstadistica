package com.franciscolinares.ubb.partido

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View

class TiroView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val tiros = mutableListOf<Tiro>()
    val tamaño = 12f
    val grosor = 3.5f

    private val paintCirculoL = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = grosor
    }

    private val paintCirculoV = Paint().apply {
        color = Color.parseColor("#00B0FF")
        style = Paint.Style.STROKE
        strokeWidth = grosor
    }

    private val paintCruzL = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = grosor
    }

    private val paintCruzV = Paint().apply {
        color = Color.parseColor("#00B0FF")
        style = Paint.Style.STROKE
        strokeWidth = grosor
    }

    fun agregarTiro(x: Float, y: Float, encestado: Boolean, equipo: String) {
        tiros.add(Tiro(x, y, encestado, equipo))
        invalidate() // Redibujar la vista
    }

    fun mostrarTiros(nuevosTiros: List<Tiro>) {
        tiros.clear()
        tiros.addAll(nuevosTiros)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Dibujar cada tiro en su posición escalada
        for (tiro in tiros) {
            val xCanvas = tiro.x * width
            val yCanvas = tiro.y * height

            if (tiro.encestado) {
                // Dibujar círculo (acierto)
                if (tiro.equipo == "Local")
                    canvas.drawCircle(xCanvas, yCanvas, tamaño, paintCirculoL)
                else
                    canvas.drawCircle(xCanvas, yCanvas, tamaño, paintCirculoV)
            } else {
                // Dibujar cruz (fallo)
                if (tiro.equipo == "Local") {
                    canvas.drawLine(xCanvas - tamaño, yCanvas - tamaño, xCanvas + tamaño, yCanvas + tamaño, paintCruzL)
                    canvas.drawLine(xCanvas - tamaño, yCanvas + tamaño, xCanvas + tamaño, yCanvas - tamaño, paintCruzL)
                } else {
                    canvas.drawLine(xCanvas - tamaño, yCanvas - tamaño, xCanvas + tamaño, yCanvas + tamaño, paintCruzV)
                    canvas.drawLine(xCanvas - tamaño, yCanvas + tamaño, xCanvas + tamaño, yCanvas - tamaño, paintCruzV)
                }

            }
        }
    }

    data class Tiro(val x: Float, val y: Float, val encestado: Boolean, val equipo: String)
}