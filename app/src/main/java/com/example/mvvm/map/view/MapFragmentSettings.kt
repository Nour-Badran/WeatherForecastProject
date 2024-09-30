package com.example.mvvm.map.view

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
import com.example.mvvm.map.viewmodel.MapViewModel
import com.example.mvvm.map.viewmodel.MapViewModelFactory
import com.example.mvvm.weather.model.pojos.FavoritePlaces
import com.example.mvvm.settings.model.SettingsLocalDataSource
import com.example.mvvm.settings.model.SettingsRepository
import com.example.mvvm.settings.viewmodel.SettingsViewModel
import com.example.mvvm.settings.viewmodel.SettingsViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private lateinit var settingViewModel: SettingsViewModel
    private lateinit var repository: SettingsRepository
    private var currentMarker: Marker? = null
    private lateinit var geocoder: Geocoder
    private lateinit var adapter: ArrayAdapter<String>
    private var searchJob: Job? = null
    private var previousQuery: String? = null
    private lateinit var viewModel: MapViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = SettingsRepository(SettingsLocalDataSource(requireContext()))
        val factory = SettingsViewModelFactory(repository)
        settingViewModel = ViewModelProvider(this, factory).get(SettingsViewModel::class.java)
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_mapFragmentSettings2_to_SettingsFragment)
            }
        })
        Configuration.getInstance().load(requireContext(), requireContext().getSharedPreferences("osmdroid", 0))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_map_settings, container, false)
        mapView = view.findViewById(R.id.map)
        citySearch = view.findViewById(R.id.city_search)
        geocoder = Geocoder(requireContext(), Locale.getDefault())

        val factory = MapViewModelFactory()
        viewModel = factory.create(MapViewModel::class.java)

        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(16.0)

        mapView.controller.setCenter(GeoPoint(30.054205, 30.939972)) // Default center at Giza

        // Set up AutoCompleteTextView with dynamic location suggestions
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        citySearch.setAdapter(adapter)

        setupSearch()

        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let { addMarker(it, getString(R.string.unknown_location)) }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }

        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(mapEventsOverlay)


        return view
    }
    private fun setupSearch() {
        citySearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    adapter.clear()
                    return
                }
                if (s.toString() != previousQuery) {
                    previousQuery = s.toString()
                    searchJob?.cancel()
                    searchJob = lifecycleScope.launch {
                        delay(500) // Debounce delay
                        fetchLocationSuggestions(s.toString())
                    }
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
                    Toast.makeText(requireContext(), R.string.location_not_found, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchLocationSuggestions(query: String) {
        lifecycleScope.launch {
            viewModel.fetchLocationSuggestions(query).collect { suggestions ->
                if (suggestions.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.error_fetching_suggestions, Toast.LENGTH_SHORT).show()
                }
                adapter.clear()
                adapter.addAll(suggestions)
                adapter.notifyDataSetChanged()
            }
        }
    }
    private fun getLocationFromCityName(city: String, onLocationFound: (GeoPoint?) -> Unit) {
        lifecycleScope.launch {
            try {
                val addresses = geocoder.getFromLocationName(city, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    onLocationFound(GeoPoint(addresses[0].latitude, addresses[0].longitude))
                } else {
                    onLocationFound(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onLocationFound(null)
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
            .setTitle(R.string.save_location_title)
            .setMessage(getString(R.string.save_default_location_message, cityName))
            .setPositiveButton(R.string.yes) { _, _ ->
                val favoritePlace = FavoritePlaces(cityName = cityName, lat = location.latitude, lon = location.longitude)
                settingViewModel.updateLocation("MAP", R.id.map_radio_button)
                settingViewModel.setLatLon(favoritePlace.lat, favoritePlace.lon)
                Toast.makeText(requireContext(), getString(R.string.default_location_saved_message, cityName), Toast.LENGTH_SHORT).show()
                navigateToHome()
            }
            .setNegativeButton(R.string.no) { _, _ ->
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