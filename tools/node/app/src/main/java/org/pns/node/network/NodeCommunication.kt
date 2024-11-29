package org.pns.node.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.*
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NodeCommunication(private val context: Context) {
    private var wifiP2pManager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                when (intent.action) {
                    WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                        val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                            _connectionState.value = ConnectionState.Ready
                        } else {
                            _connectionState.value = ConnectionState.Disconnected
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("NodeComm", "Error in receiver: ${e.message}")
                _connectionState.value = ConnectionState.Error(e.message ?: "Unknown error")
            }
        }
    }

    init {
        try {
            if (checkPermissions()) {
                initializeWifiP2p()
            } else {
                _connectionState.value = ConnectionState.Error("Missing required permissions")
            }
        } catch (e: Exception) {
            Log.e("NodeComm", "Error initializing: ${e.message}")
            _connectionState.value = ConnectionState.Error(e.message ?: "Initialization error")
        }
    }

    private fun checkPermissions(): Boolean {
        val permissions = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.NEARBY_WIFI_DEVICES
        )
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun initializeWifiP2p() {
        try {
            wifiP2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
            channel = wifiP2pManager?.initialize(context, context.mainLooper, null)
            _connectionState.value = ConnectionState.Ready
        } catch (e: Exception) {
            Log.e("NodeComm", "Error initializing WiFi P2P: ${e.message}")
            _connectionState.value = ConnectionState.Error("WiFi P2P initialization failed")
        }
    }

    fun startDiscovery() {
        if (!checkPermissions()) {
            _connectionState.value = ConnectionState.Error("Missing required permissions")
            return
        }

        try {
            wifiP2pManager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    _connectionState.value = ConnectionState.Discovering
                }
                override fun onFailure(reason: Int) {
                    _connectionState.value = ConnectionState.Error("Discovery failed: $reason")
                }
            }) ?: run {
                _connectionState.value = ConnectionState.Error("WiFi P2P not initialized")
            }
        } catch (e: Exception) {
            Log.e("NodeComm", "Error in discovery: ${e.message}")
            _connectionState.value = ConnectionState.Error("Discovery error: ${e.message}")
        }
    }

    fun register() {
        try {
            val intentFilter = IntentFilter().apply {
                addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            }
            context.registerReceiver(receiver, intentFilter)
        } catch (e: Exception) {
            Log.e("NodeComm", "Error registering receiver: ${e.message}")
        }
    }

    fun unregister() {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            Log.e("NodeComm", "Error unregistering receiver: ${e.message}")
        }
    }

    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Ready : ConnectionState()
        object Discovering : ConnectionState()
        data class Connecting(val deviceName: String) : ConnectionState()
        data class Connected(val deviceName: String) : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }
}