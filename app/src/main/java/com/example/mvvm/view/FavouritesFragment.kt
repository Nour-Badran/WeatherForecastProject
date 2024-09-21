package com.example.mvvm.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mvvm.R
import com.example.mvvm.databinding.FragmentFavouritesBinding

class FavouritesFragment : Fragment() {
    lateinit var binding: FragmentFavouritesBinding
    private lateinit var adapter: FavouritePlacesAdapter
    private val placesList = mutableListOf("Place 1", "Place 2", "Place 3") // Sample data

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the adapter with the list and delete click logic
        adapter = FavouritePlacesAdapter(placesList) { position ->
            // Handle the delete action here
            adapter.removeItem(position)
            updateUI(adapter.itemCount == 0)
        }

        // Set up RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Check if the list is empty and update the UI
        updateUI(adapter.itemCount == 0)

        // Handle FloatingActionButton click for adding new places
        binding.fab.setOnClickListener {
            // Navigate to MapFragment
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

