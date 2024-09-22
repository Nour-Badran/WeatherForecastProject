package com.example.mvvm.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mvvm.R
import com.example.mvvm.databinding.FragmentFavouritesBinding
import com.example.mvvm.db.FavoritePlaces
import com.example.mvvm.db.WeatherDatabase
import com.example.mvvm.db.WeatherLocalDataSource
import com.example.mvvm.model.WeatherRepository
import com.example.mvvm.network.WeatherRemoteDataSource
import com.example.mvvm.viewmodel.FavPlacesViewModelFactory
import com.example.mvvm.viewmodel.FavViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FavouritesFragment : Fragment() {
    private lateinit var binding: FragmentFavouritesBinding
    private lateinit var adapter: FavouritePlacesAdapter
    private lateinit var viewModel: FavViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavouritesBinding.inflate(inflater, container, false)

        // Initialize the ViewModel
        val weatherDao = WeatherDatabase.getDatabase(requireActivity().application).weatherDao()
        val weatherRepository = WeatherRepository(
            context = requireActivity().application,
            localDataSource = WeatherLocalDataSource(weatherDao),
            remoteDataSource = WeatherRemoteDataSource()
        )
        val factory = FavPlacesViewModelFactory(weatherRepository)
        viewModel = ViewModelProvider(this, factory).get(FavViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the adapter
        adapter = FavouritePlacesAdapter(mutableListOf()) { position ->
            val placeToRemove = adapter.getItemAt(position)
            viewModel.removeFavorite(placeToRemove)
            updateUI(adapter.itemCount == 0)
        }

        // Set up RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Collect favorite places from the ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favoritePlaces.collect { places ->
                adapter.updateList(places)
                updateUI(places.isEmpty())
            }
        }

        // Collect filtered places from the ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filteredPlaces.collect { filtered ->
                adapter.updateList(filtered)
            }
        }

        // Load the favorite products initially
        viewModel.getFavoriteProducts()

        // Set up search functionality
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.emitSearchQuery(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    viewModel.resetSearchQuery() // Reset search when text is empty
                } else {
                    viewModel.emitSearchQuery(newText)
                }
                return true
            }
        })

        // Handle FloatingActionButton click for adding new places
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_favouritesFragment_to_mapFragment)
        }
    }

    private fun updateUI(isListEmpty: Boolean) {
        if (isListEmpty) {
            binding.recyclerView.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
        }
    }
}

