package com.franciscolinares.ubb.jugador

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.Navigation
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentCrearJugadorBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
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
    private var idJugador = ""

    private val REGEXP: Pattern = Pattern.compile("[0-9]{8}[A-Z]")
    private val DIGITO_CONTROL = "TRWAGMYFPDXBNJZSQVHLCKE"
    private val INVALIDOS = arrayOf("00000000T", "00000001R", "99999999R")

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

        binding.btnCJCrear.setOnClickListener {
            if (binding.txtCJFechaNacimiento.text.toString() != "" && binding.txtCJNombre.text.toString() != "") {

                /*if (validarFecha(binding.txtCJFechaNacimiento.text.toString(), "dd/MM/yyyy")) {

                    Toast.makeText(
                        binding.root.context,
                        "La Fecha de Nacimiento introducida no es correcta",
                        Toast.LENGTH_LONG
                    ).show()

                } else {*/
                val dni = binding.txtCJDNI.text.toString()
                val nombre = binding.txtCJNombre.text.toString()
                val apellido1 = binding.txtCJApellido1.text.toString()
                val apellido2 = binding.txtCJApellido2.text.toString()
                val fechaNacimiento = binding.txtCJFechaNacimiento.text.toString()
                val categoria = binding.spinnerCJCategoria.selectedItem.toString()
                val sexo = binding.spinnerCJSexo.selectedItem.toString()

                db.collection("Jugadores").add(
                    hashMapOf(
                        "Nombre" to nombre,
                        "Apellido1" to apellido1,
                        "Apellido2" to apellido2,
                        "FechaNacimineto" to fechaNacimiento,
                        "Categoria" to categoria,
                        "Sexo" to sexo,
                        "UrlFoto" to "",
                        "Equipo" to ""
                    ) as Map<String, Any>
                ).addOnSuccessListener {
                    idJugador = it.id
                    fileUpload()
                }.addOnFailureListener { exception ->
                    Log.w(
                        ContentValues.TAG,
                        "Error setting documents.",
                        exception
                    )
                }

                /*}*/
            } else {
                Toast.makeText(
                    binding.root.context,
                    "Rellene los campos (Nombre y Fecha Nacimiento)",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        return root
    }

    fun fileUpload() {

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, File)

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val jugadorRef = db.collection("Jugadores").document(idJugador)

        if (requestCode == File) {
            if (resultCode == Activity.RESULT_OK) {
                val FileUri = data!!.data
                val Folder: StorageReference =
                    FirebaseStorage.getInstance().reference.child("Jugadores")
                val file_name: StorageReference =
                    Folder.child(idJugador)
                file_name.putFile(FileUri!!).addOnSuccessListener {
                    file_name.downloadUrl.addOnSuccessListener {

                        jugadorRef.update(
                            hashMapOf(
                                "UrlFoto" to java.lang.String.valueOf(it),
                            ) as Map<String, Any>
                        ).addOnSuccessListener {
                            Toast.makeText(
                                binding.root.context,
                                "Guardado los datos con exito",
                                Toast.LENGTH_LONG
                            ).show()
                            Navigation.findNavController(binding.root)
                                .navigate(R.id.action_crearJugadorFragment_to_consultarJugadorFragment)
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

        //Categoria
        val lista2 =
            mutableListOf<String>(
                "Senior",
                "Junior",
                "Cadete",
                "Infantil"
            )
        val adaptador2 =
            ArrayAdapter<String>(binding.root.context, android.R.layout.simple_spinner_item)
        adaptador2.addAll(lista2)
        binding.spinnerCJCategoria.adapter = adaptador2

        //Sexo
        val lista3 =
            mutableListOf<String>("Masculino", "Femenino")
        val adaptador3 =
            ArrayAdapter<String>(binding.root.context, android.R.layout.simple_spinner_item)
        adaptador3.addAll(lista3)
        binding.spinnerCJSexo.adapter = adaptador3
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}