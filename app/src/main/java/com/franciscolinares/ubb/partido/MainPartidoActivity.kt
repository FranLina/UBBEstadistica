package com.franciscolinares.ubb.partido

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.ActivityMainBinding
import com.franciscolinares.ubb.databinding.ActivityMainPartidoBinding

class MainPartidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainPartidoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainPartidoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.container, PartidoFragment.newInstance()).commitNow()
        }
    }
}