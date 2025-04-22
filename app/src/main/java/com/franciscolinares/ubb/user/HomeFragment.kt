package com.franciscolinares.ubb.user

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.franciscolinares.ubb.LoginActivity
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentHomeBinding
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.btnDatos.setOnClickListener {
            root.findNavController()
                .navigate(R.id.action_homeFragment_to_datosPersonalesFragment)
        }
        binding.btnConfiguracion.setOnClickListener {
            root.findNavController().navigate(R.id.action_homeFragment_to_configuracionFragment)
        }
        binding.btnCerrar.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val userRef = FirebaseFirestore.getInstance().collection("Users").document(userId)
                userRef.update("deviceToken", FieldValue.delete())
                    .addOnSuccessListener {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        requireActivity().finish()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Logout", "Error al borrar el token: ${e.message}")
                    }
            } else {
                // Por si acaso no hay sesión activa (caso raro)
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
        }
        binding.btnJugadores.setOnClickListener {
            root.findNavController().navigate(R.id.action_homeFragment_to_jugadorFragment)
        }
        binding.btnEquipos.setOnClickListener {
            root.findNavController().navigate(R.id.action_homeFragment_to_equipoFragment)
        }
        binding.btnCrearEnfrentamiento.setOnClickListener {
            root.findNavController()
                .navigate(R.id.action_homeFragment_to_crearPartidoFragment)
        }
        binding.btnMisPartidos.setOnClickListener {
            root.findNavController()
                .navigate(R.id.action_homeFragment_to_gestionarPartidosFragment)
        }
        binding.btnEstadistica.setOnClickListener {
            root.findNavController()
                .navigate(R.id.action_homeFragment_to_cargarPartidosEstadisticaFragment)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}