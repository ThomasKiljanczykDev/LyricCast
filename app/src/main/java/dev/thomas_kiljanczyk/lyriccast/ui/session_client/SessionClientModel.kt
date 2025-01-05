/*
 * Created by Tomasz Kiljanczyk on 05/01/2025, 19:35
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 05/01/2025, 18:49
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
import com.google.android.gms.nearby.connection.Payload
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.NearbyConnectionLifecycleCallback
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.ShowLyricsContent
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.SimpleNearbyPayloadCallback
import dev.thomas_kiljanczyk.lyriccast.shared.misc.SessionClientCommand
import dev.thomas_kiljanczyk.lyriccast.shared.misc.SessionClientMessage
import dev.thomas_kiljanczyk.lyriccast.shared.misc.SessionServerCommand
import dev.thomas_kiljanczyk.lyriccast.shared.misc.SessionServerMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
        val payloadString = payload?.decodeToString() ?: return

        val message = SessionClientMessage.fromJson<ShowLyricsContent>(payloadString) ?: return

        when (message.command) {
            SessionClientCommand.SHOW_SLIDE -> {
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
                endpointId, SimpleNearbyPayloadCallback(this@SessionClientModel::handlePayload)
            )
        }

        override fun onConnectionResult(
            endpointId: String, connectionInfo: ConnectionInfo?, result: ConnectionResolution
        ) {
            if (result.status.isSuccess) {
                // TODO: show a success toast
                connectionsClient.sendPayload(
                    endpointId,
                    Payload.fromBytes(
                        SessionServerMessage(
                            SessionServerCommand.SEND_LATEST_SLIDE
                        ).toJson().toByteArray()
                    )
                )
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