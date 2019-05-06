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
import io.flatcircle.connectivityhelper.StateChangeHandler

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), StateChangeHandler {

    lateinit var netMonitor: ConnectionMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        netMonitor = ConnectionMonitor(applicationContext, this, ConnectionType.WiFi, ConnectionType.Cellular)

        val isConnectedToWifi = netMonitor.isConnectedTo(ConnectionType.WiFi)
        val isConnectedToCellular = netMonitor.isConnectedTo(ConnectionType.Cellular)
        Log.i("MainActivity", "Connected to Wifi")
    }

    override fun stateChange(state: ConnectionState) {
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
