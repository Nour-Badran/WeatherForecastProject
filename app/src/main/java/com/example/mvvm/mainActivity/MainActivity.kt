package com.example.mvvm.mainActivity

import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityHomeBinding
import com.example.mvvm.map.view.OnLocationSelectedListener
import com.example.mvvm.weather.model.pojos.FavoritePlaces
import com.example.mvvm.weather.model.db.WeatherDatabase
import com.example.mvvm.weather.model.localdatasource.WeatherLocalDataSource
import com.example.mvvm.settings.model.SettingsLocalDataSource
import com.example.mvvm.settings.model.SettingsRepository
import com.example.mvvm.weather.model.repo.WeatherRepository
import com.example.mvvm.networkBrodcastReciever.NetworkChangeReceiver
import com.example.mvvm.weather.model.remotedatasource.WeatherRemoteDataSource
import com.example.mvvm.favourites.viewmodel.FavPlacesViewModelFactory
import com.example.mvvm.favourites.viewmodel.FavViewModel
import com.example.mvvm.settings.viewmodel.SettingsViewModel
import com.example.mvvm.settings.viewmodel.SettingsViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class MainActivity : AppCompatActivity(), OnLocationSelectedListener {
    lateinit var db: ActivityHomeBinding
    private lateinit var navController: NavController
    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var networkChangeReceiver: NetworkChangeReceiver
    private lateinit var settingsViewModel: SettingsViewModel
    private var currentLanguage: String = Locale.getDefault().language // Initial language
    private lateinit var viewModel: FavViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(db.root)
        navController = findNavController(R.id.nav_host_home_fragment)
        bottomNavigationView = db.bottomNavigation
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        val settingsLocalDataSource = SettingsLocalDataSource(application)
        val settingsRepository = SettingsRepository(settingsLocalDataSource)
        val settingsFactory = SettingsViewModelFactory(settingsRepository)
        settingsViewModel = ViewModelProvider(this, settingsFactory).get(SettingsViewModel::class.java)

        // Observe language changes
        settingsViewModel.language.observe(this) { language ->
            if (language.equals("English") || language.equals("الإنجليزية")) {
                updateLocaleIfNeeded("en")
            } else {
                updateLocaleIfNeeded("ar")
            }
        }

        // Register network receiver
        initializeNetworkReceiver()
        val weatherDao = WeatherDatabase.getDatabase(application).weatherDao()
        val weatherRepository = WeatherRepository(
            localDataSource = WeatherLocalDataSource(weatherDao),
            remoteDataSource = WeatherRemoteDataSource()
        )
        val factory = FavPlacesViewModelFactory(weatherRepository)
        viewModel = ViewModelProvider(this, factory).get(FavViewModel::class.java)

    }

    private fun updateLocaleIfNeeded(newLanguage: String) {
        if (currentLanguage != newLanguage) {
            setLocale(newLanguage)
            currentLanguage = newLanguage
        }
    }

    private fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)

        // Set layout direction for RTL or LTR
        if (language == "ar") {
            config.setLayoutDirection(locale)
        } else {
            config.setLayoutDirection(Locale.ENGLISH)
        }

        resources.updateConfiguration(config, resources.displayMetrics)

        if (db.root.layoutDirection == View.LAYOUT_DIRECTION_LTR) {
            db.root.layoutDirection = View.LAYOUT_DIRECTION_RTL
        } else {
            db.root.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
    }

    private fun initializeNetworkReceiver() {
        networkChangeReceiver = NetworkChangeReceiver()
        val filter = IntentFilter()
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(networkChangeReceiver, filter)
    }

    override fun onLocationSelected(locationName: FavoritePlaces) {
        viewModel.addPlace(locationName)
    }
}
