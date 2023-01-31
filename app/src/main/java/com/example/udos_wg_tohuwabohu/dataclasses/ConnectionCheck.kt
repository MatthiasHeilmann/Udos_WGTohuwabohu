package com.example.udos_wg_tohuwabohu.dataclasses

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast

class ConnectionCheck private constructor() {

    companion object {
        private var instance: ConnectionCheck? = null

        fun getInstance(): ConnectionCheck = instance ?: synchronized(this) {
            instance ?: ConnectionCheck().also { instance = it }
        }
    }


    fun check(context: Context): Boolean {
        if (checkConnection(context)) {
            return true
        }
        Toast.makeText(context, "Es besteht kein Internetverbindung!", Toast.LENGTH_LONG).show()
        return false
    }

    fun checkConnection(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }
}