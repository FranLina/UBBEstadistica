package com.franciscolinares.ubb

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.franciscolinares.ubb.databinding.ActivityLoginBinding
import com.franciscolinares.ubb.user.MainActivity
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idUser = prefs.getString("idUser", "")
        val password = prefs.getString("password", "")

        if (!idUser.toString().isEmpty() && !password.toString().isEmpty()) {
            comprobarUsuario(idUser.toString(), password.toString())
        }

        binding.btnEntrar.setOnClickListener() {
            comprobarUsuario(
                binding.textUsuario.text.toString(),
                binding.textContraseA.text.toString()
            )
        }
    }

    private fun comprobarUsuario(idUser: String, password: String) {
        // Referencia directa al documento usando el ID del usuario
        val usuarioRef = db.collection("Users").document(idUser)

        // Obtener el documento con el ID del usuario
        usuarioRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists() && documentSnapshot.getString("password") == password) {
                    // El usuario existe en la base de datos
                    //Log.d("Firestore", "Usuario encontrado.")

                    val intent = Intent(this, MainActivity::class.java)
                    Toast.makeText(this, "Inicio sesion correctamente", Toast.LENGTH_LONG).show()

                    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
                    val editor = prefs.edit()
                    editor.putString("idUser", idUser)
                    editor.putString("password", password)
                    editor.apply()

                    startActivity(intent)

                } else {
                    // El usuario no existe en la base de datos
                    //Log.d("Firestore", "Usuario no encontrado.")

                    Toast.makeText(
                        this,
                        "El usuario o contraseña estan mal, intentelo de nuevo",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { exception ->
                // Manejar errores
                //Log.w("Firestore", "Error al obtener documento", exception)
                Toast.makeText(
                    this,
                    "Error al intentar conectar con la base de datos",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}