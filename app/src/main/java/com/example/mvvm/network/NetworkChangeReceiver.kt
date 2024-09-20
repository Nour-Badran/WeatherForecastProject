package com.example.mvvm.network

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

class NetworkChangeReceiver : BroadcastReceiver() {
    var flag: Boolean = true

    override fun onReceive(context: Context, intent: Intent) {
        val isConnected = NetworkUtil.isNetworkConnected(context)

        if (context is Activity) {
            val rootView = context.findViewById<View>(android.R.id.content)

            if (isConnected) {
                if (!flag) {
                    flag = true
                    val snackbar = Snackbar.make(rootView, "Network connected", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(context.resources.getColor(android.R.color.holo_green_dark))
                        .setTextColor(context.resources.getColor(android.R.color.white))
                    customizeSnackbar(snackbar, context)
                    snackbar.setDuration(4000)
                    snackbar.show()
                }
            } else {
                flag = false
                val snackbar = Snackbar.make(rootView, "Network disconnected", Snackbar.LENGTH_SHORT)
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

    private fun customizeSnackbar(snackbar: Snackbar, context: Context) {
        val textView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.textSize = 20f // Set text size to 18sp (adjust as needed)
        // Optionally, you can set a custom font if you have one in your assets or resources:
        // textView.typeface = Typeface.createFromAsset(context.assets, "fonts/custom_font.ttf")
    }
}

