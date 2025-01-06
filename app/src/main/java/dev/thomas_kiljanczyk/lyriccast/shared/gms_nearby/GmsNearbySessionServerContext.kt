/*
 * Created by Tomasz Kiljanczyk on 06/01/2025, 18:29
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 06/01/2025, 17:33
 */

package dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ReceivedPayload(val endpointId: String, val payload: String)

class GmsNearbySessionServerContext(
    private val connectionsClient: ConnectionsClient
) {
    companion object {
        const val TAG = "GmsNearbyServerContext"
    }

    enum class AdvertisingState {
        ADVERTISING, NOT_ADVERTISING, FAILED
    }

    enum class ConnectionState {
        DISCONNECTED, CONNECTED, FAILED
    }

    data class DeviceConnectionInfo(val deviceName: String, val connectionState: ConnectionState)

    private val _serverIsRunning: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val serverIsRunning: StateFlow<Boolean> = _serverIsRunning

    private val _deviceConnectionInfo: MutableSharedFlow<DeviceConnectionInfo> = MutableSharedFlow()
    val deviceConnectionInfo: Flow<DeviceConnectionInfo> = _deviceConnectionInfo

    private val _advertisingState: MutableSharedFlow<AdvertisingState> = MutableSharedFlow()
    val advertisingState: Flow<AdvertisingState> = _advertisingState

    private val _receivedPayload: MutableSharedFlow<ReceivedPayload> = MutableSharedFlow()
    val receivedPayload: Flow<ReceivedPayload> = _receivedPayload

    private val connectedEndpointIds = mutableSetOf<String>()

    private inner class ServerConnectionLifecycleCallback : NearbyConnectionLifecycleCallback() {
        override fun onConnectionInitiated(
            endpointId: String, connectionInfo: ConnectionInfo
        ) {
            super.onConnectionInitiated(endpointId, connectionInfo)
            connectionsClient.acceptConnection(endpointId, SimpleNearbyPayloadCallback {
                val payloadString = it?.decodeToString() ?: return@SimpleNearbyPayloadCallback
                Log.i(TAG, "Received message : $payloadString")

                CoroutineScope(Dispatchers.Default).launch {
                    _receivedPayload.emit(ReceivedPayload(endpointId, payloadString))
                }
            })
        }

        override fun onConnectionResult(
            endpointId: String, connectionInfo: ConnectionInfo?, result: ConnectionResolution
        ) {
            if (result.status.isSuccess) {
                connectedEndpointIds.add(endpointId)

                if (connectionInfo != null) {
                    CoroutineScope(Dispatchers.Default).launch {
                        _deviceConnectionInfo.emit(
                            DeviceConnectionInfo(
                                connectionInfo.endpointName,
                                ConnectionState.CONNECTED
                            )
                        )
                    }
                }
            } else {
                if (connectionInfo != null) {
                    CoroutineScope(Dispatchers.Default).launch {
                        _deviceConnectionInfo.emit(
                            DeviceConnectionInfo(
                                connectionInfo.endpointName,
                                ConnectionState.FAILED
                            )
                        )
                    }
                }
            }
        }

        override fun onDisconnected(endpointId: String, connectionInfo: ConnectionInfo?) {
            if (connectionInfo != null) {
                CoroutineScope(Dispatchers.Default).launch {
                    _deviceConnectionInfo.emit(
                        DeviceConnectionInfo(
                            connectionInfo.endpointName,
                            ConnectionState.DISCONNECTED
                        )
                    )
                }
            }
            connectedEndpointIds.remove(endpointId)
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH])
    fun startServer(deviceName: String) {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()

        connectionsClient.startAdvertising(
            deviceName,
            GmsNearbyConstants.SERVICE_UUID.toString(),
            ServerConnectionLifecycleCallback(),
            advertisingOptions
        ).addOnSuccessListener {
            _serverIsRunning.value = true
            CoroutineScope(Dispatchers.Default).launch {
                _advertisingState.emit(AdvertisingState.ADVERTISING)
            }
        }.addOnFailureListener { e: Exception? ->
            Log.e(TAG, "Failed to start server", e)
            _serverIsRunning.value = false
            CoroutineScope(Dispatchers.Default).launch {
                _advertisingState.emit(AdvertisingState.FAILED)
            }
        }
    }


    fun stopServer() {
        connectionsClient.stopAdvertising()
        connectionsClient.stopAllEndpoints()
        _serverIsRunning.value = false
        CoroutineScope(Dispatchers.Default).launch {
            _advertisingState.emit(AdvertisingState.NOT_ADVERTISING)
        }
    }

    fun broadcastMessage(message: String) {
        if (connectedEndpointIds.isEmpty()) {
            return
        }

        broadcastMessage(connectedEndpointIds.toList(), message)
    }

    private fun broadcastMessage(endpointIds: List<String>, message: String) {
        Log.i(TAG, "Sending message : $message")

        connectionsClient.sendPayload(
            endpointIds, Payload.fromBytes(message.toByteArray())
        ).addOnSuccessListener {
            Log.i(TAG, "Message sent")
        }.addOnFailureListener { e: Exception? ->
            Log.e(TAG, "Failed to send message", e)
        }
    }

    fun sendMessage(endpointId: String, message: String) {
        broadcastMessage(listOf(endpointId), message)
    }
}