package io.flatcircle.connectivityhelper

import android.net.NetworkCapabilities
import androidx.annotation.RequiresApi

/**
 * Created by jacquessmuts on 2019-05-03
 * The Connection types supported by the ConnectionMonitor
 */

sealed class ConnectionType(val networkCapablity: Int, var isConnectedTo: Boolean = false) {
    object Cellular: ConnectionType(NetworkCapabilities.TRANSPORT_CELLULAR)
    object WiFi: ConnectionType(NetworkCapabilities.TRANSPORT_WIFI)
    object Bluetooth: ConnectionType(NetworkCapabilities.TRANSPORT_BLUETOOTH)
    object Ethernet: ConnectionType(NetworkCapabilities.TRANSPORT_ETHERNET)
    object VPN: ConnectionType(NetworkCapabilities.TRANSPORT_VPN)

    @RequiresApi(26)
    object WiFiAware: ConnectionType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
    @RequiresApi(27)
    object LoWPAN: ConnectionType(NetworkCapabilities.TRANSPORT_LOWPAN)
}