package io.flatcircle.connectivityhelper

import android.annotation.SuppressLint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Created by jacquessmuts on 2019-05-06
 * Internal class which pings repeatedly
 */
internal class PingLooper(val connectionMonitors: List<ConnectionStateMonitor>,
                          val pingResultHandler: PingResultHandler,
                          val endpoint: String): CoroutineScope {

    private val job: Job by lazy { SupervisorJob() }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private var pingJob: Job? = null
    /**
     * This job loops infinitely by pinging endpoints until it gets interrupted
     */
    @SuppressLint("MissingPermission")
    fun doPings() {

        launch {
            pingJob?.cancelAndJoin()

            pingJob = launch {
                var isConnectedThisLoop = false
                connectionMonitors.forEach {
                    if (!isConnectedThisLoop) {
                        val isConnected = NetUtil.ping(endpoint)
                        if (isConnected) {
                            pingResultHandler.result(true)
                            isConnectedThisLoop = true
                        }
                    }
                }
                if (!isConnectedThisLoop) {
                    pingResultHandler.result(false)
                }
                delay(ConnectionMonitor.TIME_BETWEEN_PINGS)
                doPings()
            }
        }
    }

    fun cancel() {
        pingJob?.cancel()
    }
}

interface PingResultHandler {

    fun result(didReachEndpoint: Boolean)

}