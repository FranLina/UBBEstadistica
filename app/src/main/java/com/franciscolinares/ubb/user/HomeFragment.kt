package com.franciscolinares.ubb.user

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.franciscolinares.ubb.LoginActivity
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.FragmentHomeBinding

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
            Navigation.findNavController(root)
                .navigate(R.id.action_homeFragment_to_datosPersonalesFragment)
        }
        binding.btnConfiguracion.setOnClickListener {
            Navigation.findNavController(root)
                .navigate(R.id.action_homeFragment_to_configuracionFragment)
        }
        binding.btnCerrar.setOnClickListener {
            val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
            val editor = prefs.edit()
            editor.putString("idUser", "")
            editor.putString("password", "")
            editor.apply()
            val intent = Intent(getActivity(), LoginActivity::class.java)
            startActivity(intent)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}