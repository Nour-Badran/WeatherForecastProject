package com.example.mvvm.alert.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvm.alert.model.AlertEntity
import com.example.mvvm.databinding.ItemAlertBinding
import java.util.Locale

class AlertAdapter(private val onCancelClick: (AlertEntity) -> Unit) :
    ListAdapter<AlertEntity, AlertAdapter.AlertViewHolder>(AlertDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(getItem(position)) // Use getItem to fetch the current item
    }

    inner class AlertViewHolder(private val binding: ItemAlertBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(alert: AlertEntity) {
            val currentLanguage = Locale.getDefault().language

            binding.tvAlertType.text = if (currentLanguage == "ar") {
                if (alert.alertType == "ALARM") "منبه" else "إشعار"
            } else {
                if (alert.alertType == "ALARM") "Alarm" else "Notification"
            }
            binding.tvAlertTime.text = String.format("%02d:%02d", alert.hour, alert.minute)
            binding.btnCancel.setOnClickListener {
                onCancelClick(alert)
            }
        }
    }
}

class AlertDiffUtil : DiffUtil.ItemCallback<AlertEntity>() {
    override fun areItemsTheSame(oldItem: AlertEntity, newItem: AlertEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: AlertEntity, newItem: AlertEntity): Boolean {
        return oldItem == newItem
    }
}

