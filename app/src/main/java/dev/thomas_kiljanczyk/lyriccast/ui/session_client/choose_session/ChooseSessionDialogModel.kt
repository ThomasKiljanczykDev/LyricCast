/*
 * Created by Tomasz Kiljanczyk on 06/01/2025, 19:03
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 06/01/2025, 19:03
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
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChooseSessionDialogModel @Inject constructor(
    private val connectionsClient: ConnectionsClient
) : ViewModel() {
    companion object {
        const val TAG: String = "PickDeviceDialogViewModel"
    }

    private val _serverEndpointId = MutableSharedFlow<String>()
    val serverEndpointId: SharedFlow<String>
        get() = _serverEndpointId

    private val deviceMap = mutableMapOf<String, GmsNearbySessionItem>()

    private val _devices = MutableStateFlow<List<GmsNearbySessionItem>>(emptyList())
    val devices: StateFlow<List<GmsNearbySessionItem>>
        get() = _devices


    fun pickDevice(item: GmsNearbySessionItem) {
        viewModelScope.launch {
            _serverEndpointId.emit(item.endpointId)
        }

        Log.i(TAG, "Picked : ${item.deviceName}")
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
            // TODO: show a explanatory message to the user
        }
    }

    fun stopDiscovery() {
        connectionsClient.stopDiscovery()
    }
}