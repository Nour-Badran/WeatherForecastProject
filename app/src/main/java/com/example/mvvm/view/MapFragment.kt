package com.example.mvvm.view

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MapEventsOverlay
import com.example.mvvm.R
import com.example.mvvm.db.FavoritePlaces
import com.example.mvvm.viewmodel.MapViewModel
import com.example.mvvm.viewmodel.MapViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var citySearch: AutoCompleteTextView
    private var listener: OnLocationSelectedListener? = null
    private var currentMarker: Marker? = null
    private lateinit var geocoder: Geocoder
    private lateinit var adapter: ArrayAdapter<String>
    private var searchJob: Job? = null
    private var previousQuery: String? = null

    // Use ViewModel
    private lateinit var viewModel: MapViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToFavorites()
            }
        })
        Configuration.getInstance().load(requireContext(), requireContext().getSharedPreferences("osmdroid", 0))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnLocationSelectedListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.map)
        citySearch = view.findViewById(R.id.city_search)
        geocoder = Geocoder(requireContext(), Locale.getDefault())

        val factory = MapViewModelFactory()
        viewModel = factory.create(MapViewModel::class.java)

        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(16.0)
        mapView.controller.setCenter(GeoPoint(30.054205, 30.939972))

        // Set up AutoCompleteTextView with dynamic location suggestions
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        citySearch.setAdapter(adapter)

        citySearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    adapter.clear()
                    adapter.notifyDataSetChanged()
                } else if (s.toString() != previousQuery) {
                    previousQuery = s.toString()
                    searchJob?.cancel()
                    searchJob = lifecycleScope.launch {
                        delay(500)
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

    private fun fetchLocationSuggestions(query: String) {
        lifecycleScope.launch {
            viewModel.fetchLocationSuggestions(query).collect { suggestions ->
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
        currentMarker?.let {
            mapView.overlays.remove(it)
        }

        lifecycleScope.launch {
            val locationName = withContext(Dispatchers.IO) {
                try {
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    addresses?.firstOrNull()?.locality ?: defaultTitle
                } catch (e: Exception) {
                    e.printStackTrace()
                    defaultTitle
                }
            }

            currentMarker = Marker(mapView).apply {
                position = location
                title = locationName
                mapView.overlays.add(this)
            }
            mapView.invalidate()
            showSaveDialog(locationName, location)
        }
    }

    private fun showSaveDialog(cityName: String, location: GeoPoint) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.save_location_title)
            .setMessage(getString(R.string.save_location_message, cityName))
            .setPositiveButton(R.string.yes) { _, _ ->
                val favoritePlace = FavoritePlaces(cityName = cityName, lat = location.latitude, lon = location.longitude)
                listener?.onLocationSelected(favoritePlace)
                Toast.makeText(requireContext(), getString(R.string.favorite_saved_message, cityName), Toast.LENGTH_SHORT).show()
                navigateToFavorites()
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

    private fun navigateToFavorites() {
        findNavController().navigate(R.id.action_mapFragment_to_FavouritesFragment)
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
