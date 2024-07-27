package com.example.playmusicapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast

class NetworkConnectivityReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // quan ly ket noi mang
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        // lay thong tin ve kha nang cua mang hien tai dang hoat dong
        val networkCapabilities =
            connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)
        // kiem tra ket noi mang
        val isConnected =
            networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false

        if (isConnected) {
            Toast.makeText(context, "Network connected", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Network disconnected", Toast.LENGTH_SHORT).show()
        }
    }
}