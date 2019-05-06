package io.flatcircle.connectivityhelper

import androidx.annotation.WorkerThread
import java.io.IOException

/**
 * Created by jacquessmuts on 2019-05-06
 * class used to Ping any ip-address
 */
object Pinger {

    @WorkerThread
    @Throws(InterruptedException::class)
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
//
//    private fun executeCommand(endpoint: String): Boolean {
//        println("executeCommand")
//        val runtime = Runtime.getRuntime()
//        try {
//            val mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
//            val mExitValue = mIpAddrProcess.waitFor()
//            println(" mExitValue $mExitValue")
//            return if (mExitValue == 0) {
//                true
//            } else {
//                false
//            }
//        } catch (ignore: InterruptedException) {
//            ignore.printStackTrace()
//            println(" Exception:$ignore")
//        } catch (e: IOException) {
//            e.printStackTrace()
//            println(" Exception:$e")
//        }
//
//        return false
//    }
}