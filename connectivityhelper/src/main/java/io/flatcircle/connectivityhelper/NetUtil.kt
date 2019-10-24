package io.flatcircle.connectivityhelper

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import java.io.IOException

/**
 * Created by jacquessmuts on 2019-05-02
 * Utilities for networks
 */
object NetUtil {


    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isProbablyOnline(context: Context?): Boolean {
        if (context == null)
            return false

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return isProbablyOnline(connectivityManager)
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isProbablyOnline(connectivityManager: ConnectivityManager): Boolean {
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return (activeNetworkInfo != null) && activeNetworkInfo.isConnected
    }

    /**
     * Identical to getWifiInfoPartial, but forces you to get ACCESS_COARSE_LOCATION permission on
     * Android 27+ in order to obtain the full wifi info
     */
    @RequiresPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    @RequiresApi(27)
    fun getWifiInfo(context: Context?): WifiInfo? {
        return getWifiInfoPartial(context)
    }

    /**
     * On Android 27 (8.0) and onwards, you won't get ssid/bssid info unless you have
     * ACCESS_COARSE_LOCATION permission
     */
    private fun getWifiInfoPartial(context: Context?): WifiInfo? {
        if (context == null)
            return null

        return getWifiInfoInternal(context)
    }

    private fun getWifiInfoInternal(context: Context): WifiInfo {
        val wifiMgr = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiMgr.connectionInfo
    }

    /**
     * This seems to work but I've been adviced that it does not always work. Be careful.
     */
    @WorkerThread
    @Throws(InterruptedException::class, SecurityException::class)
    @RequiresPermission(Manifest.permission.INTERNET)
    fun ping(destination: String, timeoutInSeconds: Int = 5): Boolean {
        try {
            val command =
                String.format("/system/bin/ping -c 3 -W %d %s", timeoutInSeconds, destination)
            val process = Runtime.getRuntime().exec(command)
            val ret = process.waitFor()
            process.destroy()
            return ret == 0
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return false
    }


    /**
     * Indicate if the wifi network is connected at all.
     *
     * @param context A context to use.
     * @return True if connected, false otherwise.
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isWifiConnected(context: Context?): Boolean {
        val connManager = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networks = connManager.allNetworks
        if (networks != null && networks.isNotEmpty()) {
            repeat(networks
                .map { connManager.getNetworkInfo(it) }
                .filter { it != null && it.type == ConnectivityManager.TYPE_WIFI && it.isConnected }.size
            ) { return true }
        }

        return false
    }

    /**
     * Returns the active WifiInfo.
     *
     * @return The active WifiInfo or null if not connected to a WiFi network or if context is null
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getActiveWifiInfo(context: Context?): WifiInfo? {
        if (context == null)
            return null

        // Check if a network is connected at all.
        if (!isWifiConnected(context)) {
            return null
        }

        // Get the SSID from the Wi-Fi manager service.
        return (context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .connectionInfo
    }


    /**
     * Returns the active WiFi SSID.
     *
     * @return The active WiFi SSID or null not connected to a WiFi network.
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getActiveWifiSSID(context: Context?): String? {
        val wifiInfo = getActiveWifiInfo(context)
        if (wifiInfo != null) {
            val ssid = wifiInfo.ssid
            if (ssid != null) {
                return if (ssid.length > 2 &&
                    ssid[0] == '"' &&
                    ssid[ssid.length - 1] == '"') {
                    // Strip encapsulating quotes.
                    ssid.substring(1, ssid.length - 1)
                } else {
                    ssid
                }
            }
        }

        return null
    }


    /**
     * Returns the active WiFi BSSID.
     *
     * @return The active WiFi BSSID or null not connected to a WiFi network.
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getActiveWifiBSSID(context: Context?): String? {
        val wifiInfo = getActiveWifiInfo(context)
        if (wifiInfo != null) {
            val bssid = wifiInfo.bssid
            if (bssid != null) {
                return bssid
            }
        }

        return null
    }

}