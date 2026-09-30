package com.learnquest.mp.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Reports whether the device currently has a validated route to the internet.
 *
 * A transport being available (for example, connected Wi-Fi) is not enough:
 * Android only reports online after validation succeeds, which avoids showing
 * "online" behind captive portals or on a network with no upstream access.
 */
class NetworkConnectivityObserver(context: Context) : AutoCloseable {
    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isOnline = MutableStateFlow(hasValidatedInternet())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: android.net.Network) {
            refresh()
        }

        override fun onCapabilitiesChanged(
            network: android.net.Network,
            networkCapabilities: NetworkCapabilities
        ) {
            refresh()
        }

        override fun onLost(network: android.net.Network) {
            refresh()
        }
    }

    init {
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(),
            callback
        )
        refresh()
    }

    private fun hasValidatedInternet(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun refresh() {
        _isOnline.value = hasValidatedInternet()
    }

    override fun close() {
        runCatching { connectivityManager.unregisterNetworkCallback(callback) }
    }
}
