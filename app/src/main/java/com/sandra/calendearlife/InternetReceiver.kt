package com.sandra.calendearlife

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo

class InternetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true

        when {
            !isConnected -> {
                buildShowInternetDialog(context).show()
            }
        }
    }

    private fun buildShowInternetDialog(context: Context?): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("No Internet Connection")
        builder.setMessage("You need to have Mobile Data or wifi to access this. Press ok to Exit")

        builder.setPositiveButton(
            "Ok"
        ) { dialog, which -> dialog.dismiss() }

        return builder
    }
}