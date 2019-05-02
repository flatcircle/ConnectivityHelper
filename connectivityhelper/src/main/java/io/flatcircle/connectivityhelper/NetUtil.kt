package io.flatcircle.connectivityhelper

import android.content.Context
import android.net.ConnectivityManager

/**
 * Created by jacquessmuts on 2019-05-02
 * Utilities for networks
 */
object NetUtil {

    fun isOnline(context: Context?): Boolean {

        if (context == null)
            return false

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return (activeNetworkInfo != null) && activeNetworkInfo.isConnected
    }
}