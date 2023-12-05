package com.franciscolinares.ubb.user

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
import android.widget.Toast
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentDatosPersonalesBinding
import com.franciscolinares.ubb.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class DatosPersonalesFragment : Fragment() {

    private var _binding: FragmentDatosPersonalesBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val File = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDatosPersonalesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idUser = prefs.getString("idUser", "")

        val usuarioRef = db.collection("Users").document(idUser.toString())

        // Obtener el documento con el ID del usuario
        usuarioRef.get()
            .addOnSuccessListener { documentSnapshot ->
                binding.etDPNombre.setText(documentSnapshot.get("nombre").toString())
                binding.etDPApellido1.setText(documentSnapshot.get("apellido1").toString())
                binding.etDPApellido2.setText(documentSnapshot.get("apellido2").toString())
                binding.etDPUbicacion.setText(documentSnapshot.get("direccion").toString())
                binding.etDPPoblacion.setText(documentSnapshot.get("poblacion").toString())
                binding.etDPProvincia.setText(documentSnapshot.get("provincia").toString())
                binding.etDPCorreo.setText(documentSnapshot.get("correo").toString())
                binding.etDPTelefono.setText(documentSnapshot.get("telefono").toString())
            }
            .addOnFailureListener { exception ->
                // Manejar errores
                //Log.w("Firestore", "Error al obtener documento", exception)
                Toast.makeText(
                    root.context,
                    "Error al intentar conectar con la base de datos",
                    Toast.LENGTH_LONG
                ).show()
            }

        binding.btnDPCargar.setOnClickListener {
            fileUpload()
        }

        binding.btnDPEnviar.setOnClickListener {
            usuarioRef.update(
                hashMapOf(
                    "apellido1" to binding.etDPApellido1.text.toString(),
                    "apellido2" to binding.etDPApellido2.text.toString(),
                    "nombre" to binding.etDPNombre.text.toString(),
                    "correo" to binding.etDPCorreo.text.toString(),
                    "poblacion" to binding.etDPPoblacion.text.toString(),
                    "provincia" to binding.etDPProvincia.text.toString(),
                    "telefono" to binding.etDPTelefono.text.toString(),
                    "direccion" to binding.etDPUbicacion.text.toString()
                ) as Map<String, Any>
            ).addOnSuccessListener {
                Toast.makeText(
                    binding.root.context,
                    "Guardado los datos con exito",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        return root
    }

    fun fileUpload() {
        //Funcion para iniciar acción del gesto de archivos
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, File)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idUser = prefs.getString("idUser", "")

        val usuarioRef = db.collection("Users").document(idUser.toString())

        if (requestCode == File) {
            if (resultCode == Activity.RESULT_OK) {
                val fileUri = data!!.data
                var folder: StorageReference =
                    FirebaseStorage.getInstance().reference.child("Users")
                val fileName: StorageReference =
                    folder.child(idUser.toString())
                fileName.putFile(fileUri!!).addOnSuccessListener {
                    fileName.downloadUrl.addOnSuccessListener {
                        usuarioRef.update(
                            hashMapOf(
                                "UrlFoto" to java.lang.String.valueOf(it),
                            ) as Map<String, Any>
                        ).addOnSuccessListener {
                            Toast.makeText(
                                binding.root.context,
                                "Se subió correctamente",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}