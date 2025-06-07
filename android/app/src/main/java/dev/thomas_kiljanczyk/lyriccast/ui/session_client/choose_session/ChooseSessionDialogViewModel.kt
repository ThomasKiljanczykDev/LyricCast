/*
 * Created by Tomasz Kiljanczyk on 6/7/25, 5:53 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/7/25, 5:53 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.session_client.choose_session

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Strategy
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.GmsNearbyConstants
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChooseSessionDialogViewModel @Inject constructor(
    private val connectionsClient: ConnectionsClient
) : ViewModel() {
    companion object {
        const val TAG: String = "ChooseSessionDialogModel"
    }

    private val _serverEndpointId = MutableSharedFlow<String>()
    val serverEndpointId get() = _serverEndpointId.asSharedFlow()

    private val deviceMap = mutableMapOf<String, GmsNearbySessionItem>()

    private val _devices = MutableStateFlow<List<GmsNearbySessionItem>>(emptyList())
    val devices get() = _devices.asStateFlow()

    private val _sessionStartError = MutableSharedFlow<Boolean>()
    val sessionStartError get() = _sessionStartError.asSharedFlow()

    fun reset() {
        deviceMap.clear()
        _devices.value = emptyList()
    }

    fun pickDevice(item: GmsNearbySessionItem) {
        viewModelScope.launch {
            _serverEndpointId.emit(item.endpointId)
        }

        Log.d(TAG, "Picked : ${item.deviceName}")
    }

    fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        connectionsClient.startDiscovery(
            GmsNearbyConstants.SERVICE_UUID.toString(), object : EndpointDiscoveryCallback() {
                override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                    val deviceItem = GmsNearbySessionItem(info.endpointName, endpointId)
                    deviceMap[endpointId] = deviceItem

                    _devices.value = deviceMap.values.toList()
                }

                override fun onEndpointLost(endpointId: String) {
                    deviceMap.remove(endpointId)

                    _devices.value = deviceMap.values.toList()
                }

            }, discoveryOptions
        ).addOnFailureListener { e ->
            Log.e(TAG, "Failed to start discovering", e)
            viewModelScope.launch {
                _sessionStartError.emit(true)
            }
        }
    }

    fun stopDiscovery() {
        connectionsClient.stopDiscovery()
    }
}