package com.franciscolinares.ubb.jugador

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentCrearJugadorBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.time.DateTimeException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Arrays
import java.util.regex.Pattern

class CrearJugadorFragment : Fragment() {

    private var _binding: FragmentCrearJugadorBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private val File = 1
    private var jugadorId = ""

    private val REGEXP: Pattern = Pattern.compile("[0-9]{8}[A-Z]")
    private val DIGITO_CONTROL = "TRWAGMYFPDXBNJZSQVHLCKE"
    private val INVALIDOS = arrayOf("00000000T", "00000001R", "99999999R")

    val listaCategoria = mutableListOf("Senior", "Junior", "Cadete", "Infantil")
    val listaSexo = mutableListOf("Masculino", "Femenino")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCrearJugadorBinding.inflate(inflater, container, false)
        val root: View = binding.root

        llenarSpinner()

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        jugadorId = prefs.getString("jugadorId", "").toString()

        if (jugadorId != "") {

            val jugadorRef = db.collection("Jugadores").document(jugadorId)

            jugadorRef.get().addOnSuccessListener {
                binding.txtCJNombre.setText(it.get("Nombre").toString())
                binding.txtCJApellido1.setText(it.get("Apellido1").toString())
                binding.txtCJApellido2.setText(it.get("Apellido2").toString())
                binding.txtCJFechaNacimiento.setText(it.get("FechaNacimiento").toString())
                if (it.get("UrlFoto").toString() != "") {
                    Glide.with(binding.root.context)
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.jugador_de_baloncesto)
                        .error(R.drawable.jugador_de_baloncesto)
                        .centerCrop()
                        .override(300,300)
                        .into(binding.imgCJFoto)
                }
                val cat = it.get("Categoria").toString()
                val sexo = it.get("Sexo").toString()
                var posicionSeleccionada = listaCategoria.indexOfFirst { it == cat }
                if (posicionSeleccionada != -1) {
                    binding.spinnerCJCategoria.setSelection(posicionSeleccionada)
                }
                posicionSeleccionada = listaSexo.indexOfFirst { it == sexo }
                if (posicionSeleccionada != -1) {
                    binding.spinnerCJSexo.setSelection(posicionSeleccionada)
                }
            }


        }

        binding.btnCJCrear.setOnClickListener {

            if (binding.txtCJFechaNacimiento.text.toString() != "" && binding.txtCJNombre.text.toString() != "") {
                val nombre = binding.txtCJNombre.text.toString()
                val apellido1 = binding.txtCJApellido1.text.toString()
                val apellido2 = binding.txtCJApellido2.text.toString()
                val fechaNacimiento = binding.txtCJFechaNacimiento.text.toString()
                val categoria = binding.spinnerCJCategoria.selectedItem.toString()
                val sexo = binding.spinnerCJSexo.selectedItem.toString()

                if (jugadorId != "") {
                    db.collection("Jugadores").document(jugadorId).update(
                        hashMapOf(
                            "Nombre" to nombre,
                            "Apellido1" to apellido1,
                            "Apellido2" to apellido2,
                            "FechaNacimiento" to fechaNacimiento,
                            "Categoria" to categoria,
                            "Sexo" to sexo,
                        ) as Map<String, Any>
                    ).addOnSuccessListener {
                        Toast.makeText(context, "Guardado el jugador con exito", Toast.LENGTH_LONG).show()
                    }.addOnFailureListener { exception ->
                        Log.w(
                            ContentValues.TAG,
                            "Error setting documents.",
                            exception
                        )
                    }
                } else {

                    db.collection("Jugadores").add(
                        hashMapOf(
                            "Nombre" to nombre,
                            "Apellido1" to apellido1,
                            "Apellido2" to apellido2,
                            "FechaNacimiento" to fechaNacimiento,
                            "Categoria" to categoria,
                            "Sexo" to sexo,
                            "UrlFoto" to "",
                            "Equipo" to ""
                        ) as Map<String, Any>
                    ).addOnSuccessListener {
                        val editor = prefs.edit()
                        editor.putString("jugadorId", it.id)
                        editor.apply()
                        jugadorId = it.id
                        Toast.makeText(context, "Guardado el jugador con exito", Toast.LENGTH_LONG).show()
                    }.addOnFailureListener { exception ->
                        Log.w(
                            ContentValues.TAG,
                            "Error setting documents.",
                            exception
                        )
                    }
                }
            } else {
                Toast.makeText(
                    binding.root.context,
                    "Rellene los campos (Nombre y Fecha Nacimiento)",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.btnCJCargarFoto.setOnClickListener {
            if (jugadorId != "") {
                fileUpload()
            } else {
                Toast.makeText(context, "Primero guarda el jugador para poder cargar una foto", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    private fun fileUpload() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        jugadorId = prefs.getString("jugadorId", "").toString()
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, File)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val jugadorRef = db.collection("Jugadores").document(jugadorId)

        if (requestCode == File) {
            if (resultCode == Activity.RESULT_OK) {
                val fileUri = data!!.data
                val folder: StorageReference = FirebaseStorage.getInstance().reference.child("Jugadores")
                val fileName: StorageReference = folder.child(jugadorId)
                fileName.putFile(fileUri!!).addOnSuccessListener {
                    fileName.downloadUrl.addOnSuccessListener {

                        jugadorRef.update(
                            hashMapOf(
                                "UrlFoto" to java.lang.String.valueOf(it),
                            ) as Map<String, Any>
                        ).addOnSuccessListener {
                            Toast.makeText(context, "Cargada la foto con exito", Toast.LENGTH_LONG).show()
                            jugadorRef.get().addOnSuccessListener { j ->
                                if (j.get("UrlFoto").toString() != "") {
                                    Glide.with(binding.root.context)
                                        .load(j.get("UrlFoto").toString())
                                        .placeholder(R.drawable.jugador_de_baloncesto)
                                        .error(R.drawable.jugador_de_baloncesto)
                                        .into(binding.imgCJFoto)
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    private fun validarFecha(fecha: String, formato: String): Boolean {

        val formatter = DateTimeFormatter.ofPattern(formato)

        try {
            val fechaValidada: LocalDate = LocalDate.parse(fecha, formatter)
            return fecha == fechaValidada.format(formatter)
        } catch (e: DateTimeException) {
            return false
        }
    }

    fun validarDNI(dni: String): Boolean {
        return (Arrays.binarySearch(INVALIDOS, dni) < 0
                && REGEXP.matcher(dni).matches()
                && dni[8] == DIGITO_CONTROL[dni.substring(0, 8).toInt() % 23])
    }

    fun llenarSpinner() {
        val adaptador2 = ArrayAdapter<String>(binding.root.context, android.R.layout.simple_spinner_item)
        adaptador2.addAll(listaCategoria)
        binding.spinnerCJCategoria.adapter = adaptador2
        val adaptador3 = ArrayAdapter<String>(binding.root.context, android.R.layout.simple_spinner_item)
        adaptador3.addAll(listaSexo)
        binding.spinnerCJSexo.adapter = adaptador3
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}