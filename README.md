# ConnectivityHelper
Functions to help with checking your app's online status

[![CircleCI](https://circleci.com/gh/flatcircle/ConnectivityHelper.svg?style=svg)](https://circleci.com/gh/flatcircle/ConnectivityHelper) [ ![Download](https://api.bintray.com/packages/flatcircle/ConnectivityHelper/connectivityhelper/images/download.svg) ](https://bintray.com/flatcircle/ConnectivityHelper/connectivityhelper/_latestVersion)

Installation
--------

```groovy
implementation 'io.flatcircle:connectivityhelper:{version}'
```

Usage
-----

| Function  | Description | Example |
| ------------- | ------------- | ------------- |
| NetUtil.isProbablyOnline(context) | Does a basic check to see if the device has a connected network of some kind | [Example](https://github.com/flatcircle/LiveDataHelper/blob/master/app/src/main/java/io/flatcircle/livedatahelperexample/MainActivity.kt#L34)  |
| NetUtil.ping(endpoint) | Pings a given endpoint | [Example](https://github.com/flatcircle/LiveDataHelper/blob/master/app/src/main/java/io/flatcircle/livedatahelperexample/MainActivity.kt#L34)  |
| NetUtil.getWifiInfo(context) | Provides all wifi info, but requires location permission  | [Example](https://github.com/flatcircle/LiveDataHelper/blob/master/app/src/main/java/io/flatcircle/livedatahelperexample/MainActivity.kt#L34)  |
| NetUtil.getWifiInfoPartial(context) | Provides all wifi info it can, but will omit ssid/bssid without location permission on Android 27+ | [Example](https://github.com/flatcircle/LiveDataHelper/blob/master/app/src/main/java/io/flatcircle/livedatahelperexample/MainActivity.kt#L34)  |

ConnectionMonitor
-----

This library also comes with a ConnectionMonitor, which can be used to get active notifications about your app's connectivity status.

The ConnectionMonitor must be coupled with your Activity/Application's lifecycle like so:

```kotlin
class MainActivity : AppCompatActivity(), StateChangeHandler {

    lateinit var netMonitor: ConnectionMonitor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        netMonitor = ConnectionMonitor.Builder(applicationContext) // any context will do
                    .watchedConnections(ConnectionType.Cellular, ConnectionType.WiFi) // list of connections that will be watched by the monitor
                    .stateChangeListener(this) // optional interface for handling connectivity state changes
                    .timeBetweenPings(10000) // milliseconds between each ping to check online status
                    .endpoint("8.8.8.8") // endpoint for pinging against. Default is Google, but that's unreliable in some countries.
                    .build()

        val isConnectedToWifi = netMonitor.isConnectedTo(ConnectionType.WiFi) // Determines if the app is currently connected to the given network type
        val isConnectedToCellular = netMonitor.isConnectedTo(ConnectionType.Cellular) // Determines if the app is currently connected to the given network type

        val connectionState = netMonitor.connectionState // the current connectionState, which can be Offline, ProbablyOnline, or Online

        when (connectionState) {
            ConnectionState.Offline -> {} // The app is not connected to any network with internet capability
            ConnectionState.ProbablyOnline -> {} // The app is connected to a network which claims to have internet
            ConnectionState.Online -> {} // The app is able to successfully resolve a call to the endpoint, at least within the last [timeBetweenPings] milliseconds
        }
    }

    override fun stateChange(state: ConnectionState) {
        // function from StateChangeHandler, which receives all state changes
    }

    override fun onDestroy() {
        netMonitor.clear() // netMonitor must be cleared for garbage collection
        super.onDestroy()
    }
```