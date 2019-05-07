package io.flatcircle.connectivityhelper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import androidx.annotation.RequiresPermission
import java.lang.IllegalArgumentException

/**
 * Created by jacquessmuts on 2019-05-03
 * This class must be instantiated as a singleton and .clear()'d upon activity/application destruction
 */
@SuppressLint("MissingPermission")
class ConnectionMonitor
private constructor(context: Context,
                    val stateChangeListener: StateChangeListener?,
                    val watchedConnections: List<ConnectionType>,
                    endpoint: String
): ConnectivityEventObserver, PingResultHandler {

    companion object {
        const val TIME_BETWEEN_PINGS = 30000L // 30 seconds
        const val GOOGLE_IP = "8.8.8.8" // This IP does not resolve in China
        const val BAIDU_IP = "180.76.5"
    }

    private val connectionMonitors: List<ConnectionStateMonitor> by lazy {
        watchedConnections.map { ConnectionStateMonitor(it) }
    }
    private val pingLooper = PingLooper(connectionMonitors, this, endpoint)

    override fun result(didReachEndpoint: Boolean) {
        if (didReachEndpoint) {
            connectionState = ConnectionState.Online
        } else {
            if (NetUtil.isProbablyOnline(connectivityManager)) {
                connectionState = ConnectionState.ProbablyOnline
            } else {
                connectionState = ConnectionState.Offline
            }
        }
    }

    private val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Returns naive connection status if this monitor is monitoring the given connectionType. Does
     * not indicate whether the connectionType is on the internet, merely whether it is functioning
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
     * Best guess as to the current connectionState. Only 100% accurate
     * [ConnectionState.millisSinceLastCheck] milliseconds ago.
     */
    var connectionState: ConnectionState = ConnectionState.Offline
        private set (nuState) {

            if (nuState is ConnectionState.ProbablyOnline &&
                (field.millisSinceLastCheck() > TIME_BETWEEN_PINGS || field is ConnectionState.Offline)) {
                    pingLooper.doPings()
            } else if (nuState is ConnectionState.Offline) {
                pingLooper.cancel()
            }
            field = nuState
            stateChangeListener?.stateChange(field)
        }

    init {
        connectionMonitors.forEach { it.enable(connectivityManager, this) }
        pingLooper.doPings()
    }

    override fun handleConnectivityEvent(connectivityEvent: ConnectivityEvent) {

        connectionState = connectionState.newState(connectivityManager)

        watchedConnections.firstOrNull {
            it.networkCapablity == connectivityEvent.connectionType.networkCapablity
        }?.isConnectedTo = connectivityEvent.isConnectedTo

    }

    fun clear() {
        pingLooper.cancel()
        connectionMonitors.forEach { it.disable(connectivityManager) }
        connectivityManager.isActiveNetworkMetered
    }


    class Builder(private val context: Context) {
        private var stateChangeListener: StateChangeListener? = null
        private var watchedConnections: List<ConnectionType> = listOf()
        private var timeBetweenPings: Long = TIME_BETWEEN_PINGS
        private var endpoint: String = GOOGLE_IP

        fun watchedConnections(vararg connections: ConnectionType): Builder {
            this.watchedConnections = connections.toList()
            return this
        }

        fun stateChangeListener(stateChangeListener: StateChangeListener): Builder {
            this.stateChangeListener = stateChangeListener
            return this
        }

        fun timeBetweenPings(milliseconds: Long): Builder {
            this.timeBetweenPings = milliseconds
            return this
        }

        fun endpoint(address: String): Builder {
            this.endpoint = address
            return this
        }

        @RequiresPermission(allOf = [Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE])
        fun build(): ConnectionMonitor {
            return ConnectionMonitor(context, stateChangeListener, watchedConnections, endpoint)
        }
    }

}

interface StateChangeListener {

    fun stateChange(state: ConnectionState)

}

@SuppressLint("MissingPermission")
sealed class ConnectionState(var lastCheckInMillis: Long){
    object Offline: ConnectionState(System.currentTimeMillis())

    /**
     * This indicates that the app is connected to a default network, and therefore probably online.
     * Since VPN configurations can be incorrect, or the user might be out of data on a cellular
     * network, or blocked behind a firewall, this is not 100% reliable.
     */
    object ProbablyOnline: ConnectionState(System.currentTimeMillis())

    /**
     * This indicates that the app was online at [lastCheckInMillis] by doing a successful ping
     */
    object Online: ConnectionState(System.currentTimeMillis())

    fun newState(connectivityManager: ConnectivityManager): ConnectionState {
        val pair = Pair(this, NetUtil.isProbablyOnline(connectivityManager))

        return when (pair) {
            Pair(Offline, false),
            Pair(ProbablyOnline, false),
            Pair(Online, false) -> Offline

            Pair(Offline, true),
            Pair(ProbablyOnline, true) -> ProbablyOnline

            Pair(Online, true) -> Online

            else -> Offline
        }
    }

    fun millisSinceLastCheck(): Long {
        return System.currentTimeMillis() - lastCheckInMillis
    }
}