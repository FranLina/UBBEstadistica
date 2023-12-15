package com.franciscolinares.ubb.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentEquipoBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EquipoFragment : Fragment() {

    private var _binding: FragmentEquipoBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEquipoBinding.inflate(inflater, container, false)

        binding.btnECrear.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_equipoFragment_to_crearEquipoFragment)
        }

        binding.btnEConsultar.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_equipoFragment_to_consultarEquipoFragment)
        }

        /*binding.btnEAgregarLiga.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_equiposFragment_to_agregarEquipoLigaFragment)
        }*/

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}