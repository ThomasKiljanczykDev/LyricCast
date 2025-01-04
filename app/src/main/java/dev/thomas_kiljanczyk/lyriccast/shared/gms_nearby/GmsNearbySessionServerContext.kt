/*
 * Created by Tomasz Kiljanczyk on 04/01/2025, 16:41
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 04/01/2025, 16:41
 */

package dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Strategy
import dev.thomas_kiljanczyk.lyriccast.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GmsNearbySessionServerContext(
    private val connectionsClient: ConnectionsClient
) {
    companion object {
        const val TAG = "GmsNearbyServerContext"
    }

    private val _serverIsRunning: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val serverIsRunning: StateFlow<Boolean> = _serverIsRunning

    private val _messageFlow: MutableSharedFlow<Pair<Int, String?>> = MutableSharedFlow(replay = 1)
    val messageFlow: Flow<Pair<Int, String?>> = _messageFlow
    private val connectedEndpointIds = mutableSetOf<String>()

    // TODO: verify if this shouldn't be moved elsewhere
    private var lastMessage: String? = null

    private inner class ServerConnectionLifecycleCallback : NearbyConnectionLifecycleCallback() {
        override fun onConnectionInitiated(
            endpointId: String, connectionInfo: ConnectionInfo
        ) {
            super.onConnectionInitiated(endpointId, connectionInfo)
            connectionsClient.acceptConnection(endpointId, SimpleNearbyPayloadCallback {})
        }

        override fun onConnectionResult(
            endpointId: String, connectionInfo: ConnectionInfo?, result: ConnectionResolution
        ) {
            if (result.status.isSuccess) {
                val endpointName = connectionInfo?.endpointName
                val messageResId =
                    if (endpointName != null) R.string.gms_nearby_server_connected else R.string.gms_nearby_server_connected_unknown
                _messageFlow.tryEmit(Pair(messageResId, endpointName))

                connectedEndpointIds.add(endpointId)

                // TODO: ask for latest content on client side, implement communication on server side
                lastMessage?.let {
                    sendMessage(endpointId, it)
                }
            }
        }

        override fun onDisconnected(endpointId: String, connectionInfo: ConnectionInfo?) {
            val endpointName = connectionInfo?.endpointName
            val messageResId =
                if (endpointName != null) R.string.gms_nearby_server_disconnected else R.string.gms_nearby_server_disconnected_unknown

            _messageFlow.tryEmit(Pair(messageResId, endpointName))
            connectedEndpointIds.remove(endpointId)
        }
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(anyOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH])
    fun startServer(deviceName: String) {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()

        connectionsClient.startAdvertising(
            deviceName,
            GmsNearbyConstants.SERVICE_UUID.toString(),
            ServerConnectionLifecycleCallback(),
            advertisingOptions
        ).addOnSuccessListener { unused: Void? ->
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
        broadcastMessage(connectedEndpointIds.toList(), message)
    }

    private fun broadcastMessage(endpointIds: List<String>, message: String) {
        Log.i(TAG, "Sending message : $message")
        lastMessage = message

        connectionsClient.sendPayload(
            endpointIds, Payload.fromBytes(message.toByteArray())
        ).addOnSuccessListener {
            Log.i(TAG, "Message sent")
        }.addOnFailureListener { e: Exception? ->
            Log.e(TAG, "Failed to send message", e)
        }
    }

    private fun sendMessage(endpointId: String, message: String) {
        broadcastMessage(listOf(endpointId), message)
    }
}