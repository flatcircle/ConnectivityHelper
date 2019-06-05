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
    fun getWifiInfoPartial(context: Context?): WifiInfo? {
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

}