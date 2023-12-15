package com.franciscolinares.ubb.equipo

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
import com.franciscolinares.ubb.databinding.FragmentCrearEquipoBinding
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class CrearEquipoFragment : Fragment() {

    private var _binding: FragmentCrearEquipoBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private val File = 1
    private var nombreEquipo = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCrearEquipoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        llenarSpinner()

        binding.btnCECrear.setOnClickListener {
            val nombre = binding.txtCENombreEquipo.text.toString()
            val categoria = binding.spinnerECategoria.selectedItem.toString()
            val sexo = binding.spinnerESexo.selectedItem.toString()
            val localidad = binding.txtCELocalidad.text.toString()

            if (nombre != "" && categoria != "" && sexo != "" && localidad != "") {
                nombreEquipo = generarNombre(nombre, categoria, sexo)
                db.collection("Equipos")
                    .document(nombreEquipo)
                    .get().addOnSuccessListener {
                        if (it.get("Nombre") != null) {
                            Toast.makeText(
                                binding.root.context,
                                "Ya existe un equipo con este nombre",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            db.collection("Equipos")
                                .document(nombreEquipo).set(
                                    hashMapOf(
                                        "Nombre" to nombre,
                                        "UrlFoto" to "",
                                        "Categoria" to categoria,
                                        "Sexo" to sexo,
                                        "Localidad" to localidad,
                                        "Jugadores" to hashMapOf<String, String>()
                                    ) as Map<String, Any>
                                ).addOnSuccessListener {
                                    fileUpload()
                                }.addOnFailureListener { exception ->
                                    Log.w(ContentValues.TAG, "Error setting documents.", exception)
                                }
                        }


                    }.addOnFailureListener { exception ->
                        Log.w(ContentValues.TAG, "Error getting documents.", exception)
                    }
            } else {
                Toast.makeText(
                    binding.root.context,
                    "Rellene los campos",
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

        val equipoRef = db.collection("Equipos").document(nombreEquipo)

        if (requestCode == File) {
            if (resultCode == Activity.RESULT_OK) {
                val FileUri = data!!.data
                val Folder: StorageReference =
                    FirebaseStorage.getInstance().reference.child("Equipos")
                val file_name: StorageReference = Folder.child(nombreEquipo)
                file_name.putFile(FileUri!!).addOnSuccessListener {
                    file_name.downloadUrl.addOnSuccessListener {

                        equipoRef.update(
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
                                .navigate(R.id.action_crearEquipoFragment_to_consultarEquipoFragment)
                        }
                    }
                }

            }
        }
    }

    fun generarNombre(nombre: String, Categoria: String, Sexo: String): String {

        nombreEquipo =
            nombre + Categoria[0].toString().uppercase() + Sexo[0].toString().uppercase()

        return nombreEquipo
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
        binding.spinnerECategoria.adapter = adaptador2

        //Sexo
        val lista3 =
            mutableListOf<String>("Masculino", "Femenino")
        val adaptador3 =
            ArrayAdapter<String>(binding.root.context, android.R.layout.simple_spinner_item)
        adaptador3.addAll(lista3)
        binding.spinnerESexo.adapter = adaptador3
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}