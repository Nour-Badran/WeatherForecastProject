package com.example.mvvm.favourites.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvm.databinding.ItemFavouritePlaceBinding
import com.example.mvvm.weather.model.pojos.FavoritePlaces

class FavouritePlacesAdapter(
    private val places: MutableList<FavoritePlaces>,
    private val onItemClick: (FavoritePlaces) -> Unit,
    private val onDeleteClick: (position: Int) -> Unit
) : RecyclerView.Adapter<FavouritePlacesAdapter.FavouritePlaceViewHolder>() {

    inner class FavouritePlaceViewHolder(private val binding: ItemFavouritePlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(place: FavoritePlaces, position: Int) {
            binding.placeName.text = place.cityName

            binding.deleteIcon.setOnClickListener {
                onDeleteClick(position)
            }
            binding.root.setOnClickListener {
                onItemClick(place)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouritePlaceViewHolder {
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

    fun updateList(newPlaces: List<FavoritePlaces>) {
        places.clear()
        places.addAll(newPlaces)
        notifyDataSetChanged()
    }

    fun getItemAt(position: Int): FavoritePlaces {
        return places[position]
    }

}
