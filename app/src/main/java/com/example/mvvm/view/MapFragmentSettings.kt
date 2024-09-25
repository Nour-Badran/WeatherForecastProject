package com.example.mvvm.view

import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mvvm.R
import com.example.mvvm.db.FavoritePlaces
import com.example.mvvm.model.SettingsLocalDataSource
import com.example.mvvm.model.SettingsRepository
import com.example.mvvm.viewmodel.SettingsViewModel
import com.example.mvvm.viewmodel.SettingsViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Locale

class MapFragmentSettings : Fragment() {
    private lateinit var mapView: MapView
    private lateinit var citySearch: AutoCompleteTextView
    private lateinit var viewModel: SettingsViewModel
    private lateinit var repository: SettingsRepository
    private var currentMarker: Marker? = null // Property to hold the current marker
    private lateinit var geocoder: Geocoder
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = SettingsRepository(SettingsLocalDataSource(requireContext()))
        val factory = SettingsViewModelFactory(requireActivity().application, repository)
        viewModel = ViewModelProvider(this, factory).get(SettingsViewModel::class.java)
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //viewModel.updateLocation("MAP", R.id.mapFragment)
                findNavController().navigate(R.id.action_mapFragmentSettings2_to_SettingsFragment)
            }
        })
        Configuration.getInstance().load(requireContext(), requireContext().getSharedPreferences("osmdroid", 0))

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_map_settings, container, false)
        mapView = view.findViewById(R.id.map)
        citySearch = view.findViewById(R.id.city_search)
        geocoder = Geocoder(requireContext(), Locale.getDefault())

        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(GeoPoint(30.0, 31.1342)) // Default center at Giza

        // Set up AutoCompleteTextView with dynamic location suggestions
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        citySearch.setAdapter(adapter)

        citySearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.length > 0) {
                    fetchLocationSuggestions(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        citySearch.setOnItemClickListener { parent, _, position, _ ->
            val selectedCity = parent.getItemAtPosition(position).toString()
            getLocationFromCityName(selectedCity) { location ->
                if (location != null) {
                    addMarker(location, selectedCity)
                    mapView.controller.animateTo(location)
                } else {
                    Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let { addMarker(it, "Unknown Location") }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                // Handle long press if needed
                return false
            }
        }

        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(mapEventsOverlay)


        return view
    }
    private fun fetchLocationSuggestions(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Use Geocoder to search for location names matching the query
                val addresses = geocoder.getFromLocationName(query, 5) // Fetch top 5 results
                val suggestions = addresses?.flatMap { address ->
                    // Extracting locality, subLocality, featureName, and other address parts
                    listOfNotNull(
                        address.locality,
                        address.subLocality,
                        address.featureName,
                        address.adminArea, // Administrative area (e.g., city or state)
                        address.subAdminArea, // Sub-administrative area (e.g., district)
                    )
                }?.distinct() // Remove duplicates

                // Switch to the main thread to update UI
                withContext(Dispatchers.Main) {
                    adapter.clear()
                    if (!suggestions.isNullOrEmpty()) {
                        adapter.addAll(suggestions)
                        adapter.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error fetching suggestions", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun getLocationFromCityName(city: String, onLocationFound: (GeoPoint?) -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocationName(city, 1)
                if (addresses != null) {
                    if (addresses.isNotEmpty()) {
                        val location = addresses[0]
                        withContext(Dispatchers.Main) {
                            onLocationFound(GeoPoint(location.latitude, location.longitude))
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onLocationFound(null)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onLocationFound(null)
                }
            }
        }
    }
    private fun addMarker(location: GeoPoint, defaultTitle: String) {
        // Remove the existing marker if it exists
        currentMarker?.let {
            mapView.overlays.remove(it)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            var locationName = defaultTitle

            try {
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    locationName = addresses[0].locality ?: defaultTitle
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            withContext(Dispatchers.Main) {
                currentMarker = Marker(mapView).apply {
                    position = location
                    title = locationName
                    mapView.overlays.add(this)
                }
                mapView.invalidate()
                showSaveDialog(locationName, location)
            }
        }
    }
    private fun showSaveDialog(cityName: String, location: GeoPoint) {
        AlertDialog.Builder(requireContext())
            .setTitle("Save Location")
            .setMessage("Do you want to set $cityName as your default location?")
            .setPositiveButton("Yes") { _, _ ->
                val favoritePlace = FavoritePlaces(cityName = cityName, lat = location.latitude, lon = location.longitude)
                viewModel.updateLocation("MAP", R.id.map_radio_button)
                viewModel.setLatLon(favoritePlace.lat,favoritePlace.lon)
                Toast.makeText(requireContext(), "$cityName saved as default location!", Toast.LENGTH_SHORT).show()
                navigateToHome()
            }
            .setNegativeButton("No") { _, _ ->
                currentMarker?.let {
                    mapView.overlays.remove(it)
                    currentMarker = null
                    mapView.invalidate()
                }
            }
            .show()
    }
    private fun navigateToHome(){
        findNavController().navigate(R.id.action_mapFragmentSettings2_to_homeFragment)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}