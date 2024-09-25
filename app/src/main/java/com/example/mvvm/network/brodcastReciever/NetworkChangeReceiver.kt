package com.example.mvvm.network.brodcastReciever

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import com.example.mvvm.R
import com.example.mvvm.utilities.customizeSnackbar
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class NetworkChangeReceiver : BroadcastReceiver() {
    var flag: Boolean = true

    override fun onReceive(context: Context, intent: Intent) {
        val isConnected = NetworkUtil.isNetworkConnected(context)

        if (context is Activity) {
            val rootView = context.findViewById<View>(android.R.id.content)
            if (Locale.getDefault().language == "ar") {
                ViewCompat.setLayoutDirection(rootView, ViewCompat.LAYOUT_DIRECTION_RTL)
            } else {
                ViewCompat.setLayoutDirection(rootView, ViewCompat.LAYOUT_DIRECTION_LTR)
            }
            if (isConnected) {
                if (!flag) {
                    flag = true
                    val snackbar = Snackbar.make(rootView, context.getString(R.string.network_connected), Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(context.resources.getColor(android.R.color.holo_green_dark))
                        .setTextColor(context.resources.getColor(android.R.color.white))
                    customizeSnackbar(snackbar, context)
                    snackbar.setDuration(4000)
                    snackbar.show()
                }
            } else {
                flag = false
                val snackbar = Snackbar.make(rootView, context.getString(R.string.network_disconnected), Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(context.resources.getColor(android.R.color.holo_red_dark))
                    .setTextColor(context.resources.getColor(android.R.color.white))
                customizeSnackbar(snackbar, context)
                snackbar.setDuration(4000)
                snackbar.show()
            }
        } else {
            Toast.makeText(context, "Network status changed", Toast.LENGTH_SHORT).show()
        }
    }


}

