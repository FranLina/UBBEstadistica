package com.franciscolinares.ubb.user

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.franciscolinares.ubb.R
import com.franciscolinares.ubb.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val db = FirebaseFirestore.getInstance()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.jugadorFragment,
                R.id.equipoFragment,
                R.id.crearPartidoFragment,
                R.id.gestionarPartidosFragment,
                R.id.cargarPartidosEstadisticaFragment,
                R.id.datosPersonalesFragment,
                R.id.configuracionFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val prefs = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val idUser = prefs.getString("idUser", "")

        val usuarioRef = db.collection("Users").document(idUser.toString())

        usuarioRef.get()
            .addOnSuccessListener {
                navView.getHeaderView(0).findViewById<TextView>(R.id.tvNombreApellidos).text =
                    it.get("nombre").toString() + " " + it.get("apellido1")
                        .toString() + " " + it.get("apellido2").toString()
                navView.getHeaderView(0).findViewById<TextView>(R.id.tvEmail).text =
                    it.get("correo").toString()

                if (it.get("UrlFoto").toString() != "") {
                    Picasso.get()
                        .load(it.get("UrlFoto").toString())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(navView.getHeaderView(0).findViewById<ImageView>(R.id.imageMUsuario))
                }

            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}