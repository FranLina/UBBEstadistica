package com.franciscolinares.ubb.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentJugadorBinding

class JugadorFragment : Fragment() {

    private var _binding: FragmentJugadorBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentJugadorBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.btnJCrearJugador.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_jugadorFragment_to_crearJugadorFragment)
        }

        binding.btnJConsultarJugador.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_jugadorFragment_to_consultarJugadorFragment)
        }

        /*binding.btnJAgregarEquipo.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_jugadoresFragment_to_agregarJugadorEquipoFragment)
        }*/

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}