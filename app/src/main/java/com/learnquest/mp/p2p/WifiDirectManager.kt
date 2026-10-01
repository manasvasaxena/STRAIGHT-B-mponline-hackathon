package com.learnquest.mp.p2p

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.p2p.*
import android.os.Build
import android.util.Log
import com.learnquest.mp.p2p.models.PlayerBattleInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WifiDirectManager(private val context: Context) : WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {

    private val p2pManager: WifiP2pManager? = context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
    private var channel: WifiP2pManager.Channel? = null

    private val _discoveredPeers = MutableStateFlow<List<PlayerBattleInfo>>(emptyList())
    val discoveredPeers: StateFlow<List<PlayerBattleInfo>> = _discoveredPeers.asStateFlow()

    private val _isWifiP2pEnabled = MutableStateFlow(true)
    val isWifiP2pEnabled: StateFlow<Boolean> = _isWifiP2pEnabled.asStateFlow()

    private val _connectionInfo = MutableStateFlow<WifiP2pInfo?>(null)
    val connectionInfo: StateFlow<WifiP2pInfo?> = _connectionInfo.asStateFlow()

    private var isReceiverRegistered = false

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    _isWifiP2pEnabled.value = (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    @SuppressLint("MissingPermission")
                    p2pManager?.requestPeers(channel, this@WifiDirectManager)
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    val networkInfo = intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                    if (networkInfo?.isConnected == true) {
                        p2pManager?.requestConnectionInfo(channel, this@WifiDirectManager)
                    } else {
                        _connectionInfo.value = null
                    }
                }
            }
        }
    }

    fun init() {
        if (channel == null && p2pManager != null) {
            channel = p2pManager.initialize(context, context.mainLooper, null)
        }
        if (!isReceiverRegistered) {
            try {
                context.registerReceiver(receiver, intentFilter)
                isReceiverRegistered = true
            } catch (e: Exception) {
                Log.e("WifiDirectManager", "Receiver registration error", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery(onResult: (Boolean) -> Unit = {}) {
        init()
        p2pManager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                onResult(true)
            }
            override fun onFailure(reason: Int) {
                onResult(false)
            }
        })
    }

    fun stopDiscovery() {
        p2pManager?.stopPeerDiscovery(channel, null)
    }

    @SuppressLint("MissingPermission")
    fun connectToPeer(deviceAddress: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val config = WifiP2pConfig().apply {
            this.deviceAddress = deviceAddress
            this.groupOwnerIntent = 15 // Prefer host
        }
        p2pManager?.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                onSuccess()
            }
            override fun onFailure(reason: Int) {
                onFailure("Connection failed with code: $reason")
            }
        })
    }

    fun disconnect() {
        p2pManager?.removeGroup(channel, null)
        _connectionInfo.value = null
        _discoveredPeers.value = emptyList()
    }

    override fun onPeersAvailable(peers: WifiP2pDeviceList?) {
        val deviceList = peers?.deviceList ?: return
        val mappedList = deviceList.map { device ->
            PlayerBattleInfo(
                id = device.deviceAddress,
                name = if (device.deviceName.isNull_or_empty()) "LearnQuest Device (${device.deviceAddress.takeLast(4)})" else device.deviceName,
                avatarRes = "student_avatar_1",
                level = 3,
                totalXp = 500,
                deviceAddress = device.deviceAddress
            )
        }
        _discoveredPeers.value = mappedList
    }

    override fun onConnectionInfoAvailable(info: WifiP2pInfo?) {
        _connectionInfo.value = info
    }

    fun close() {
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
            isReceiverRegistered = false
        }
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.isEmpty()
}
