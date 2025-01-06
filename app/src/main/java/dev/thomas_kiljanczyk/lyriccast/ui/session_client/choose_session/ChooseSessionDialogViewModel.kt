/*
 * Created by Tomasz Kiljanczyk on 06/01/2025, 12:56
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 06/01/2025, 12:34
 */

package dev.thomas_kiljanczyk.lyriccast.ui.session_client.choose_session

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChooseSessionDialogViewModel @Inject constructor() : ViewModel() {
    companion object {
        const val TAG: String = "PickDeviceDialogViewModel"
    }

    private val _serverEndpointId = MutableSharedFlow<String>()
    val serverEndpointId: SharedFlow<String>
        get() = _serverEndpointId

    fun pickDevice(item: GmsNearbySessionItem) {
        viewModelScope.launch {
            _serverEndpointId.emit(item.endpointId)
        }

        Log.i(TAG, "Picked : ${item.deviceName}")
    }
}