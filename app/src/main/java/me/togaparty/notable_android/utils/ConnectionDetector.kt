package me.togaparty.notable_android.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build


class ConnectionDetector(context: Context) {
    private val _context: Context = context
    val connected: Boolean
        get() {
            val connectivityManager: ConnectivityManager =
                    _context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                connectivityManager.activeNetworkInfo?.isConnected ?: false
            } else {

                val networks = connectivityManager.allNetworks
                var hasNetwork = false
                if( networks.isNotEmpty()) {
                    networks.forEach {
                        network ->
                        hasNetwork = connectivityManager.getNetworkCapabilities(network)
                                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                        return@forEach
                    }
                }

                hasNetwork
            }

        }

}