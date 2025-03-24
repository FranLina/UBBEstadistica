package com.franciscolinares.ubb.partido

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class TiroView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val tiros = mutableListOf<Tiro>()

    private val paintCirculoL = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val paintCirculoV = Paint().apply {
        color = Color.parseColor("#00B0FF")
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val paintCruzL = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val paintCruzV = Paint().apply {
        color = Color.parseColor("#00B0FF")
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    fun agregarTiro(x: Float, y: Float, encestado: Boolean, equipo: String) {
        tiros.add(Tiro(x, y, encestado, equipo))
        invalidate() // Redibujar la vista
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
                    canvas.drawCircle(xCanvas, yCanvas, 10f, paintCirculoL)
                else
                    canvas.drawCircle(xCanvas, yCanvas, 10f, paintCirculoV)
            } else {
                // Dibujar cruz (fallo)
                val tamaño = 10f
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