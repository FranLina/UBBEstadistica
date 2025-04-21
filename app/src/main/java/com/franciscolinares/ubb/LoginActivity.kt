package com.franciscolinares.ubb

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.franciscolinares.ubb.databinding.ActivityLoginBinding
import com.franciscolinares.ubb.invitado.MainInvitadoActivity
import com.franciscolinares.ubb.user.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.content.edit

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

        if (idUser != "invitado") {
            binding.textUsuario.setText(idUser.toString())
            binding.textContraseA.setText(password.toString())
        }


        if (idUser.toString().isNotEmpty() && password.toString().isNotEmpty() && idUser.toString() != "invitado") {
            comprobarUsuario(idUser.toString(), password.toString())
        } else if (idUser.toString() == "invitado" && password.toString() == "invitado") {
            val intent = Intent(this, MainInvitadoActivity::class.java)
            startActivity(intent)
        }

        binding.btnEntrar.setOnClickListener() {
            comprobarUsuario(
                binding.textUsuario.text.toString(),
                binding.textContraseA.text.toString()
            )
        }

        binding.txtInvitado.setOnClickListener {

            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            prefs.edit() {
                putString("idUser", "invitado")
                putString("password", "invitado")
            }

            val intent = Intent(this, MainInvitadoActivity::class.java)
            startActivity(intent)
        }
    }

    private fun comprobarUsuario(idUser: String, password: String) {
        // Referencia directa al documento usando el ID del usuario
        val usuarioRef = db.collection("Users").document(idUser)

        // Obtener el documento con el ID del usuario
        usuarioRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists() && documentSnapshot.getString("password") == password) {

                val intent = Intent(this, MainActivity::class.java)
                Toast.makeText(this, "Inicio sesion correctamente", Toast.LENGTH_LONG).show()

                val prefs = PreferenceManager.getDefaultSharedPreferences(this)
                val editor = prefs.edit()
                editor.putString("idUser", idUser)
                editor.putString("password", password)
                editor.apply()

                startActivity(intent)

            } else {

                Toast.makeText(
                    this,
                    "El usuario o contraseña estan mal, intentelo de nuevo",
                    Toast.LENGTH_LONG
                ).show()
            }
        }.addOnFailureListener { exception ->

            Toast.makeText(
                this,
                "Error al intentar conectar con la base de datos",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}