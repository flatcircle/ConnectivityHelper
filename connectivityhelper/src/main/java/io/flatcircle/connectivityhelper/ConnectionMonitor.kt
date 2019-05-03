package io.flatcircle.connectivityhelper

import android.content.Context
import android.net.ConnectivityManager
import java.lang.IllegalArgumentException

/**
 * Created by jacquessmuts on 2019-05-03
 * This class must be instantiated as a singleton and .clear()'d upon activity/application destruction
 */
class ConnectionMonitor(context: Context,
                        vararg watchConnections: ConnectionType
): ConnectivityEventObserver {

    private val watchedConnections: List<ConnectionType> = watchConnections.toList()

    private val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Returns true if this monitor is monitoring the given connectionType
     */
    @Throws(IllegalArgumentException::class)
    fun isConnectedTo(connectionType: ConnectionType): Boolean {
        val toReturn = watchedConnections.firstOrNull {
            it.networkCapablity == connectionType.networkCapablity
        }?.isConnectedTo

        if (toReturn == null) {
            throw IllegalArgumentException("The ConnectionMonitor was not instantiated with the passed $connectionType connectiontype")
        }
        return toReturn
    }

    /**
     * This indicates that the app is connected to some internet-enabled network,
     * and therefore probably online. Since VPN configurations can be incorrect, or the user might
     * be out of data on a cellular network, or blocked behind a firewall, this is not 100% reliable.
     */
    var isProbablyOnline = false
        private set

    private val connectionMonitors: List<ConnectionStateMonitor> by lazy {
        val toReturn = mutableListOf<ConnectionStateMonitor>()
        watchedConnections.forEach {
            toReturn.add(ConnectionStateMonitor(it))
        }
        toReturn
    }

    init {
        connectionMonitors.forEach { it.enable(connectivityManager, this) }
    }

    override fun handleConnectivityEvent(connectivityEvent: ConnectivityEvent) {

        isProbablyOnline = NetUtil.isProbablyOnline(connectivityManager)

        watchedConnections.firstOrNull {
            it.networkCapablity == connectivityEvent.connectionType.networkCapablity
        }?.isConnectedTo = connectivityEvent.isConnectedTo

    }

    fun clear() {
        connectionMonitors.forEach { it.disable(connectivityManager) }
        connectivityManager.isActiveNetworkMetered
    }

}
