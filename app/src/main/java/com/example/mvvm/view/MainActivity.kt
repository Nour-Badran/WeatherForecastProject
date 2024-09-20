package com.example.mvvm.view

import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityHomeBinding
import com.example.mvvm.network.NetworkChangeReceiver
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    lateinit var db: ActivityHomeBinding
    private lateinit var navController: NavController
    lateinit var  bottomNavigationView: BottomNavigationView
    lateinit var networkChangeReceiver: NetworkChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(db.root)
        navController = findNavController(R.id.nav_host_home_fragment)
        bottomNavigationView = db.bottomNavigation
        NavigationUI.setupWithNavController(bottomNavigationView, navController)
        // Register network receiver
        intitilaizeNetworkReciever()
    }

    private fun intitilaizeNetworkReciever() {
        networkChangeReceiver = NetworkChangeReceiver()
        val filter = IntentFilter()
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(networkChangeReceiver, filter)
    }
}