package com.example.mvvm.favourites.view

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mvvm.R
import com.example.mvvm.databinding.FragmentFavouritesBinding
import com.example.mvvm.favourites.viewmodel.FavPlacesViewModelFactory
import com.example.mvvm.favourites.viewmodel.FavViewModel
import com.example.mvvm.weather.model.db.WeatherDatabase
import com.example.mvvm.weather.model.localdatasource.WeatherLocalDataSource
import com.example.mvvm.weather.model.repo.WeatherRepository
import com.example.mvvm.weather.model.remotedatasource.WeatherRemoteDataSource
import com.example.mvvm.weather.model.utilities.customizeSnackbar
import com.example.mvvm.weather.model.utilities.isConnectedToInternet
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class FavouritesFragment : Fragment() {
    private lateinit var binding: FragmentFavouritesBinding
    private lateinit var adapter: FavouritePlacesAdapter
    private lateinit var viewModel: FavViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_profileFragment2_to_homeFragment)
            }
        })
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavouritesBinding.inflate(inflater, container, false)

        val weatherDao = WeatherDatabase.getDatabase(requireActivity().application).weatherDao()
        val weatherRepository = WeatherRepository(
            localDataSource = WeatherLocalDataSource(weatherDao),
            remoteDataSource = WeatherRemoteDataSource()
        )
        val factory = FavPlacesViewModelFactory(weatherRepository)
        viewModel = ViewModelProvider(this, factory).get(FavViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FavouritePlacesAdapter(mutableListOf(),
            onItemClick = { place ->
                if(isConnectedToInternet(requireContext()))
                {
                    val action = FavouritesFragmentDirections.actionFavouritesFragmentToPlaceDetailsFragment(place)
                    findNavController().navigate(action)
                }
                else
                {
                    val snackbar = Snackbar.make(binding.root,R.string.check_snackbar, Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(requireContext().resources.getColor(android.R.color.holo_red_dark))
                        .setTextColor(requireContext().resources.getColor(android.R.color.white))
                    snackbar.setDuration(2000)
                    customizeSnackbar(snackbar, requireContext())
                    snackbar.show()
                }
            },
            onDeleteClick = { position ->
                // Get the item to remove
                val placeToRemove = adapter.getItemAt(position)

                // Create an AlertDialog to confirm deletion
                AlertDialog.Builder(context)
                    .setTitle(R.string.confirm_deletion_title)
                    .setMessage(R.string.confirm_deletion_message)
                    .setPositiveButton(R.string.yes) { dialog, _ ->
                        // Proceed with deletion
                        viewModel.removePlace(placeToRemove)
                        updateUI(adapter.itemCount == 0)
                        dialog.dismiss() // Dismiss the dialog
                    }
                    .setNegativeButton(R.string.no) { dialog, _ ->
                        dialog.cancel() // Cancel the dialog
                    }
                    .show()
            }

        )


        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.favoritePlaces.collect { places ->
                adapter.updateList(places)
                updateUI(places.isEmpty())
            }
        }

        lifecycleScope.launch {
            viewModel.filteredPlaces.collect { filtered ->
                adapter.updateList(filtered)
            }
        }

        viewModel.getFavoritePlaces()

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.emitSearchQuery(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    viewModel.resetSearchQuery()
                } else {
                    viewModel.emitSearchQuery(newText)
                }
                return true
            }
        })

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

