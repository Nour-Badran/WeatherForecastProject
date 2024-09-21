package com.example.mvvm.view

import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityHomeBinding
import com.example.mvvm.model.SettingsLocalDataSource
import com.example.mvvm.model.SettingsRepository
import com.example.mvvm.network.NetworkChangeReceiver
import com.example.mvvm.viewmodel.SettingsViewModel
import com.example.mvvm.viewmodel.SettingsViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class MainActivity : AppCompatActivity() {
    lateinit var db: ActivityHomeBinding
    private lateinit var navController: NavController
    lateinit var  bottomNavigationView: BottomNavigationView
    lateinit var networkChangeReceiver: NetworkChangeReceiver
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(db.root)
        navController = findNavController(R.id.nav_host_home_fragment)
        bottomNavigationView = db.bottomNavigation
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        val settingsLocalDataSource = SettingsLocalDataSource(application)
        val settingsRepository = SettingsRepository(settingsLocalDataSource)
        val settingsFactory = SettingsViewModelFactory(application, settingsRepository)
        settingsViewModel = ViewModelProvider(this, settingsFactory).get(SettingsViewModel::class.java)
        settingsViewModel.language.observe(this) { language ->
            if(language.equals("English") || language.equals("الإنجليزية"))
            {
                setLocale("en")
            }
            else
                setLocale("ar")
        }

        // Register network receiver
        intitilaizeNetworkReciever()
    }
    private fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)

        // Set layout direction for RTL
        if (language == "ar") {
            config.setLayoutDirection(locale)
        } else {
            config.setLayoutDirection(Locale.ENGLISH)
        }

        resources.updateConfiguration(config, resources.displayMetrics)

        // Notify fragments to refresh
        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment is Refreshable) {
                fragment.refresh()
            }
        }
    }


    private fun intitilaizeNetworkReciever() {
        networkChangeReceiver = NetworkChangeReceiver()
        val filter = IntentFilter()
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(networkChangeReceiver, filter)
    }
}