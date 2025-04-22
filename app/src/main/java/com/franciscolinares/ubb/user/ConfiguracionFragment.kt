package com.franciscolinares.ubb.user

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentConfiguracionBinding
import com.franciscolinares.ubb.databinding.FragmentDatosPersonalesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ConfiguracionFragment : Fragment() {

    private var _binding: FragmentConfiguracionBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    private lateinit var editTextOldPassword: EditText
    private lateinit var editTextNewPassword: EditText
    private lateinit var btnChangePassword: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentConfiguracionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        /*val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idUser = prefs.getString("idUser", "")

        val usuarioRef = db.collection("Users").document(idUser.toString())*/

        editTextOldPassword = binding.etCContraseA
        editTextNewPassword = binding.etCRepiteContraseA
        btnChangePassword = binding.btnCGuardar

        btnChangePassword.setOnClickListener {
            val newPassword = editTextNewPassword.text.toString()

            if (newPassword.isNotEmpty()) {
                val user = FirebaseAuth.getInstance().currentUser

                if (user != null) {
                    updatePassword(user, newPassword)
                } else {
                    Toast.makeText(root.context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(root.context, "Por favor ingresa una nueva contraseña", Toast.LENGTH_SHORT).show()
            }
        }

        /*binding.btnCGuardar.setOnClickListener {
            val contraseña = binding.etCContraseA.text.toString()
            val rcontraseña = binding.etCRepiteContraseA.text.toString()
            if (validatePass(contraseña, rcontraseña)) {
                usuarioRef.update(
                    hashMapOf(
                        "password" to contraseña,
                    ) as Map<String, Any>
                ).addOnSuccessListener {
                    val prefs =
                        PreferenceManager.getDefaultSharedPreferences(binding.root.context)
                    val editor = prefs.edit()
                    editor.putString("password", contraseña)
                    editor.apply()
                    Toast.makeText(
                        binding.root.context,
                        "Cambio de contraseña con exito",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.etCContraseA.setText("")
                    binding.etCRepiteContraseA.setText("")
                }
            }
        }*/

        return root
    }

    private fun updatePassword(user: FirebaseUser, newPassword: String) {
        user.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Contraseña cambiada correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error al cambiar la contraseña", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /*private fun validatePass(pass1: String, pass2: String): Boolean {
        var salida: Boolean = false
        if (pass1 != "" && pass2 != "") {
            if (pass1 == pass2) {
                salida = true
            } else {
                salida = false
                Toast.makeText(
                    binding.root.context,
                    "Las contraseñas no coinciden, intentelo de nuevo",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            salida = false
            Toast.makeText(
                binding.root.context,
                "Rellene los campos",
                Toast.LENGTH_LONG
            ).show()
        }
        return salida
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}