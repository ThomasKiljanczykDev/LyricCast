/*
 * Created by Tomasz Kiljanczyk on 6/7/25, 5:53 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/7/25, 5:53 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.session_client

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionClientViewModel @Inject constructor(
    private val connectionsClient: ConnectionsClient
) : ViewModel() {
    companion object {
        private const val TAG = "SessionClientModel"
    }

    enum class ConnectionState {
        DISCONNECTED, CONNECTED, FAILED
    }

    data class SlideContent(
        val songTitle: String,
        val slideText: String,
        val slideNumber: String
    )

    val currentSlide get() = _currentSlide.asStateFlow()
    private val _currentSlide = MutableStateFlow(SlideContent("", "", ""))

    val connectionState get() = _connectionState.asSharedFlow()
    private val _connectionState = MutableSharedFlow<ConnectionState>()

    private var currentEndpointId: String? = null

    init {
        _connectionState.onEach {
            if (it == ConnectionState.DISCONNECTED) {
                _currentSlide.value = SlideContent("", "", "")
            }
        }.launchIn(viewModelScope)
    }

    private fun handlePayload(payload: ByteArray?) {
        val payloadString = payload?.decodeToString() ?: return

        val message = SessionClientMessage.fromJson<ShowLyricsContent>(payloadString) ?: return

        when (message.command) {
            SessionClientCommand.SHOW_SLIDE -> {
                val content = message.content
                _currentSlide.value = SlideContent(
                    content.songTitle,
                    content.slideText,
                    "${content.slideNumber}/${content.totalSlides}"
                )
            }
        }
    }

    private inner class ClientConnectionLifecycleCallback : NearbyConnectionLifecycleCallback() {
        override fun onConnectionInitiated(
            endpointId: String, connectionInfo: ConnectionInfo
        ) {
            super.onConnectionInitiated(endpointId, connectionInfo)
            connectionsClient.acceptConnection(
                endpointId, SimpleNearbyPayloadCallback(this@SessionClientViewModel::handlePayload)
            )
        }

        override fun onConnectionResult(
            endpointId: String, connectionInfo: ConnectionInfo?, result: ConnectionResolution
        ) {
            if (result.status.isSuccess) {
                currentEndpointId = endpointId
                requestLatestSlide()
                viewModelScope.launch {
                    _connectionState.emit(ConnectionState.CONNECTED)
                }
            } else {
                currentEndpointId = null
                viewModelScope.launch {
                    _connectionState.emit(ConnectionState.FAILED)
                }
            }
        }

        override fun onDisconnected(endpointId: String, connectionInfo: ConnectionInfo?) {
            currentEndpointId = null
            connectionsClient.disconnectFromEndpoint(endpointId)
            viewModelScope.launch {
                _connectionState.emit(ConnectionState.DISCONNECTED)
            }
        }
    }

    fun requestLatestSlide() {
        currentEndpointId?.let { endpointId ->
            connectionsClient.sendPayload(
                endpointId, Payload.fromBytes(
                    SessionServerMessage(
                        SessionServerCommand.SEND_LATEST_SLIDE
                    ).toJson().toByteArray()
                )
            )
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH])
    fun startClient(endpointId: String, deviceName: String) {
        connectionsClient.requestConnection(
            deviceName, endpointId, ClientConnectionLifecycleCallback()
        )
    }

    fun stopClient() {
        connectionsClient.stopAllEndpoints()
        Log.d(TAG, "Client disconnected")
    }
}