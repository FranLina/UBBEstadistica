package com.franciscolinares.ubb

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.franciscolinares.ubb.user.MainActivity
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.franciscolinares.ubb.invitado.MainInvitadoActivity
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var logoImageView: ImageView
    private lateinit var welcomeText: TextView
    private lateinit var loadingSpinner: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Inicializar las vistas
        logoImageView = findViewById(R.id.logoImageView)
        welcomeText = findViewById(R.id.welcomeText)
        loadingSpinner = findViewById(R.id.loadingSpinner)

        // Animación de desvanecimiento del logo
        val fadeIn = ObjectAnimator.ofFloat(logoImageView, "alpha", 0f, 1f)
        fadeIn.duration = 1000 // 1 segundo de animación
        fadeIn.start()

        // Mostrar el ProgressBar y ocultar el logo después de un breve retardo
        loadingSpinner.visibility = View.VISIBLE // Mostrar la animación de carga

        // Esperar un poco y hacer la comprobación de usuario
        Handler().postDelayed({
            // Comprobamos si el usuario está autenticado
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                // Si está autenticado, obtenemos el rol del usuario
                val db = FirebaseFirestore.getInstance()
                db.collection("Users").document(user.uid).get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val rol = document.getString("rol")
                            when (rol) {
                                "admin" -> startActivity(Intent(this, MainActivity::class.java))
                                "invitado" -> startActivity(Intent(this, MainInvitadoActivity::class.java))
                            }
                            finish() // Asegurarse de cerrar la SplashActivity
                        } else {
                            Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al obtener datos: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                // Si el usuario no está autenticado, redirige a la pantalla de login
                startActivity(Intent(this, LoginActivity::class.java))
                finish() // Asegúrate de cerrar SplashActivity
            }
        }, 2000) // 2 segundos de espera antes de hacer la verificación
    }
}
