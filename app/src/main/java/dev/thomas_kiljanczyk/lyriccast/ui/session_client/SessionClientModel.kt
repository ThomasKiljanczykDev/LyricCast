/*
 * Created by Tomasz Kiljanczyk on 04/01/2025, 16:41
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 04/01/2025, 16:41
 */

package dev.thomas_kiljanczyk.lyriccast.ui.session_client

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.NearbyConnectionLifecycleCallback
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.ShowLyricsContent
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.SimpleNearbyPayloadCallback
import dev.thomas_kiljanczyk.lyriccast.shared.misc.SessionCommand
import dev.thomas_kiljanczyk.lyriccast.shared.misc.SessionServerMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class SessionClientModel @Inject constructor(
    private val connectionsClient: ConnectionsClient
) : ViewModel() {
    companion object {
        private const val TAG = "SessionClientModel"
    }

    val songTitle: Flow<String> get() = _songTitle
    private val _songTitle = MutableStateFlow("")


    val currentSlideText: Flow<String> get() = _currentSlideText
    private val _currentSlideText = MutableStateFlow("")

    val currentSlideNumber: Flow<String> get() = _currentSlideNumber
    private val _currentSlideNumber = MutableStateFlow("")

    private fun handlePayload(payload: ByteArray?) {
        // TODO: handle invalid payload
        val payloadString = payload?.decodeToString() ?: return

        val message = try {
            Json.decodeFromString<SessionServerMessage<ShowLyricsContent>>(payloadString)
        } catch (e: Exception) {
            // TODO: handle invalid payload
            Log.e(TAG, "Failed to decode payload", e)
            return
        }

        when (message.command) {
            SessionCommand.SHOW_LYRICS -> {
                val content = message.content
                _songTitle.value = content.songTitle
                _currentSlideText.value = content.slideText
                _currentSlideNumber.value = "${content.slideNumber}/${content.totalSlides}"
            }
        }
    }

    private inner class ClientConnectionLifecycleCallback : NearbyConnectionLifecycleCallback() {
        override fun onConnectionInitiated(
            endpointId: String, connectionInfo: ConnectionInfo
        ) {
            super.onConnectionInitiated(endpointId, connectionInfo)
            connectionsClient.acceptConnection(
                endpointId,
                SimpleNearbyPayloadCallback(this@SessionClientModel::handlePayload)
            )
        }

        override fun onConnectionResult(
            endpointId: String, connectionInfo: ConnectionInfo?, result: ConnectionResolution
        ) {
            if (result.status.isSuccess) {
                // TODO: handle connection success - ask server for current slide
                // TODO: show a success toast
            } else {
                // TODO: handle connection failure
            }
        }

        override fun onDisconnected(endpointId: String, connectionInfo: ConnectionInfo?) {
            connectionsClient.disconnectFromEndpoint(endpointId)
            // TODO: handle connection failure
        }
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(anyOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH])
    fun startClient(endpointId: String, deviceName: String) {
        connectionsClient.requestConnection(
            deviceName, endpointId, ClientConnectionLifecycleCallback()
        )
    }

    fun stopClient() {
        connectionsClient.stopAllEndpoints()
        Log.i(TAG, "Client disconnected")
    }
}