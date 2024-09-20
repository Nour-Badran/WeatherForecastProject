package com.example.mvvm.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvm.databinding.ItemFavouritePlaceBinding

class FavouritePlacesAdapter(
    private val places: MutableList<String>,
    private val onDeleteClick: (position: Int) -> Unit
) : RecyclerView.Adapter<FavouritePlacesAdapter.FavouritePlaceViewHolder>() {

    inner class FavouritePlaceViewHolder(private val binding: ItemFavouritePlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(placeName: String, position: Int) {
            // Set the place name
            binding.placeName.text = placeName

            // Handle delete icon click
            binding.deleteIcon.setOnClickListener {
                onDeleteClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouritePlaceViewHolder {
        // Inflate the item layout using ViewBinding
        val binding = ItemFavouritePlaceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavouritePlaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavouritePlaceViewHolder, position: Int) {
        holder.bind(places[position], position)
    }

    override fun getItemCount(): Int {
        return places.size
    }

    fun removeItem(position: Int) {
        // Check if the position is valid
        if (position >= 0 && position < places.size) {
            places.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, places.size) // Ensure the adapter updates subsequent items
        }
    }

}
