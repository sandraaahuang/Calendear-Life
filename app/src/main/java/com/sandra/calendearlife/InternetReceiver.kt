package com.sandra.calendearlife

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

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
        builder.setTitle(context?.getString(R.string.no_internet_title))
        builder.setMessage(context?.getString(R.string.no_internet_message))

        builder.setPositiveButton(
            "Ok"
        ) { dialog, _ ->
            dialog.dismiss()
        }

        return builder
    }
}