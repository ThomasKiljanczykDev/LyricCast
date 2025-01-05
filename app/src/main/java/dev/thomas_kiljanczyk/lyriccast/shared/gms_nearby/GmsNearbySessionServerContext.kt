/*
 * Created by Tomasz Kiljanczyk on 05/01/2025, 19:35
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 05/01/2025, 19:34
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
import dev.thomas_kiljanczyk.lyriccast.R
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

    private val _serverIsRunning: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val serverIsRunning: StateFlow<Boolean> = _serverIsRunning

    private val _connectionMessage: MutableSharedFlow<Pair<Int, String?>> =
        MutableSharedFlow(replay = 1)
    val connectionMessage: Flow<Pair<Int, String?>> = _connectionMessage

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
                val endpointName = connectionInfo?.endpointName
                val messageResId =
                    if (endpointName != null) R.string.gms_nearby_server_connected else R.string.gms_nearby_server_connected_unknown

                CoroutineScope(Dispatchers.Default).launch {
                    _connectionMessage.emit(Pair(messageResId, endpointName))
                }

                connectedEndpointIds.add(endpointId)
            }
        }

        override fun onDisconnected(endpointId: String, connectionInfo: ConnectionInfo?) {
            val endpointName = connectionInfo?.endpointName
            val messageResId =
                if (endpointName != null) R.string.gms_nearby_server_disconnected else R.string.gms_nearby_server_disconnected_unknown

            CoroutineScope(Dispatchers.Default).launch {
                _connectionMessage.emit(Pair(messageResId, endpointName))
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
        }.addOnFailureListener { e: Exception? ->
            Log.e(TAG, "Failed to start server", e)
            _serverIsRunning.value = false
        }
    }


    fun stopServer() {
        connectionsClient.stopAdvertising()
        connectionsClient.stopAllEndpoints()
        _serverIsRunning.value = false
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