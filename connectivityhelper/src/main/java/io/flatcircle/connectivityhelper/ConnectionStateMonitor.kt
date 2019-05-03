package io.flatcircle.connectivityhelper

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest

/**
 * Created by jacquessmuts on 2019-05-03
 * Monitors the state of the provided connectiontype
 */
internal class ConnectionStateMonitor(val connectionType: ConnectionType) : ConnectivityManager.NetworkCallback() {

    var connectionObserver: ConnectivityEventObserver? = null

    private val networkRequest: NetworkRequest =
        NetworkRequest
            .Builder()
            .addTransportType(connectionType.networkCapablity).build()

    fun enable(connectivityManager: ConnectivityManager, connectivityEventObserver: ConnectivityEventObserver) {
        connectionObserver = connectivityEventObserver
        connectivityManager.registerNetworkCallback(networkRequest, this)
    }

    fun disable(connectivityManager: ConnectivityManager?) {
        connectionObserver = null
        connectivityManager?.unregisterNetworkCallback(this)
    }

    override fun onAvailable(network: Network) {
        connectionObserver?.handleConnectivityEvent(ConnectivityEvent(true, connectionType))
    }

    override fun onLost(network: Network?) {
        connectionObserver?.handleConnectivityEvent(ConnectivityEvent(false, connectionType))
    }
}

internal interface ConnectivityEventObserver {
    fun handleConnectivityEvent(connectivityEvent: ConnectivityEvent)
}

data class ConnectivityEvent(val isConnectedTo: Boolean, val connectionType: ConnectionType)