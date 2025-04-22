package com.franciscolinares.ubb

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.franciscolinares.ubb.databinding.ActivityLoginBinding
import com.franciscolinares.ubb.invitado.MainInvitadoActivity
import com.franciscolinares.ubb.user.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.franciscolinares.ubb.utils.AuthManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val txtCorreo = binding.textCorreo
        val txtPassword = binding.textContraseA
        val btnLogin = binding.btnLogin
        val btnRegistrar = binding.btnRegistrar
        val btnOlvido = binding.txtRecuperarPassword

        btnLogin.setOnClickListener {
            if (txtCorreo.text.isEmpty() || txtPassword.text.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos.", Toast.LENGTH_SHORT).show()
            } else {
                val correo = txtCorreo.text.toString()
                val pass = txtPassword.text.toString()
                AuthManager.login(correo, pass) { success, error ->
                    if (success) {
                        val userId = FirebaseAuth.getInstance().currentUser
                        val db = FirebaseFirestore.getInstance()
                        db.collection("Users").document(userId!!.uid).get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    val rol = document.getString("rol")
                                    getFCMToken(userId.uid)
                                    Toast.makeText(this, "Sesión iniciada", Toast.LENGTH_SHORT).show()
                                    when (rol) {
                                        "admin" -> startActivity(Intent(this, MainActivity::class.java))
                                        "invitado" -> startActivity(Intent(this, MainInvitadoActivity::class.java))
                                    }
                                    finish() // opcional para cerrar esta Activity
                                } else {
                                    Toast.makeText(this, "Correo o contraseña errónea", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Correo o contraseña errónea", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnRegistrar.setOnClickListener {

            if (txtCorreo.text.isEmpty() || txtPassword.text.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos.", Toast.LENGTH_SHORT).show()
            } else {
                val correo = txtCorreo.text.toString()
                val pass = txtPassword.text.toString()
                AuthManager.register(correo, pass) { success, error ->
                    if (success) {
                        val user = FirebaseAuth.getInstance().currentUser
                        crearUsuarioEnFirestoreSiNoExiste(user!!)
                        Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error al registrar al usuario", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnOlvido.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun crearUsuarioEnFirestoreSiNoExiste(user: FirebaseUser) {
        val db = FirebaseFirestore.getInstance()
        val uid = user.uid
        val docRef = db.collection("Users").document(uid)

        docRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val nuevoUsuario = hashMapOf(
                    "nombre" to (user.displayName ?: ""),
                    "apellido1" to "",
                    "apellido2" to "",
                    "telefono" to "",
                    "poblacion" to "",
                    "correo" to (user.email ?: ""),
                    "rol" to "invitado",
                    "mis_equipos" to listOf<String>(),
                    "mis_jugadores" to listOf<String>(),
                    "UrlFoto" to (user.photoUrl?.toString() ?: "")
                )

                docRef.set(nuevoUsuario)
                    .addOnSuccessListener {
                        Log.d("Firestore", "Usuario guardado correctamente")
                    }
                    .addOnFailureListener {
                        Log.e("Firestore", "Error al guardar usuario: ${it.message}")
                    }
            } else {
                Log.d("Firestore", "El usuario ya existía, no se crea de nuevo")
            }
        }
    }

    // Función para obtener el token FCM y guardarlo en Firestore
    private fun getFCMToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                saveTokenToFirestore(userId, token)
            } else {
                Log.w("FCM", "Error al obtener el token", task.exception)
            }
        }
    }

    // Función para guardar el token en Firestore
    private fun saveTokenToFirestore(userId: String, token: String) {
        val userRef = db.collection("Users").document(userId)

        userRef.update("deviceToken", token)
            .addOnSuccessListener {
                Log.d("FCM", "Token guardado correctamente")
            }
            .addOnFailureListener { e ->
                Log.w("FCM", "Error al guardar el token", e)
            }
    }
}