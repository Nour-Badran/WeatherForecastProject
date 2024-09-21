package com.example.mvvm

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.MotionEvent
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.widget.Toast
import com.example.mvvm.R
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint

class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private var selectedPlace: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(requireContext(), android.preference.PreferenceManager.getDefaultSharedPreferences(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.map)
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)

        // Set up the map view
        mapView.setMultiTouchControls(true)

        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(GeoPoint(30, 30))



        mapView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val location: IGeoPoint = mapView.projection.fromPixels(event.x.toInt(), event.y.toInt())
                addMarker(location)
            }
            true
        }

        return view
    }

    private fun addMarker(location: IGeoPoint) {
        val geoPoint = GeoPoint(location)

        val marker = Marker(mapView).apply {
            position = geoPoint
            title = "Selected Location"
            setOnMarkerClickListener { _, _ ->
                Toast.makeText(requireContext(), "Marker: $title", Toast.LENGTH_SHORT).show()
                true
            }
        }
        mapView.overlays.add(marker)
        mapView.controller.animateTo(geoPoint)

        selectedPlace = "Selected Place: ${geoPoint.latitude}, ${geoPoint.longitude}"
        Toast.makeText(requireContext(), selectedPlace, Toast.LENGTH_SHORT).show()

        mapView.invalidate() // Refresh the map to show the new marker
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


