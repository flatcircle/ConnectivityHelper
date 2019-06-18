package io.flatcircle.connectivityhelperexample

import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import io.flatcircle.connectivityhelper.ConnectionType
import io.flatcircle.connectivityhelper.ConnectionMonitor
import io.flatcircle.connectivityhelper.ConnectionState
import io.flatcircle.connectivityhelper.StateChangeListener

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), StateChangeListener {

    lateinit var netMonitor: ConnectionMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        netMonitor = ConnectionMonitor.Builder(applicationContext) // any context will do
            .watchedConnections(ConnectionType.Cellular, ConnectionType.WiFi) // list of connections that will be watched by the monitor
            .stateChangeListener(this) // optional interface for handling connectivity state changes
            .timeBetweenPings(10000) // milliseconds between each ping to check online status. Default 30000
            .endpoint("8.8.8.8") // endpoint for pinging against. Default is Google, but that's unreliable in some countries.
            .build()

        val isConnectedToWifi = netMonitor.isConnectedTo(ConnectionType.WiFi) // Determines if the app is currently connected to the given network type
        val isConnectedToCellular = netMonitor.isConnectedTo(ConnectionType.Cellular) // Determines if the app is currently connected to the given network type
        Log.i("MainActivity", "Connected to Wifi")

        val connectionState = netMonitor.connectionState // Can be Offline, ProbablyOnline, or Online

        when (connectionState) {
            ConnectionState.Offline -> {} // The app is not connected to any network with internet capability
            ConnectionState.ProbablyOnline -> {} // The app is connected to a network which claims to have internet
            ConnectionState.Online -> {} // The app is able to successfully resolve a call to the endpoint, at least within the last [timeBetweenPings] milliseconds
        }
    }

    override fun netStateChange(state: ConnectionState) {
        Log.w("MainActivity", "newState = $state")
        runOnUiThread {
            textMain.setText("Connection state is $state")
        }

    }

    override fun onDestroy() {
        netMonitor.clear()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
