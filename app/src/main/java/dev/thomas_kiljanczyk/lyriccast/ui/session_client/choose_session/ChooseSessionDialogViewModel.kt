/*
 * Created by Tomasz Kiljanczyk on 04/01/2025, 16:41
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 04/01/2025, 16:41
 */

package dev.thomas_kiljanczyk.lyriccast.ui.session_client.choose_session

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ChooseSessionDialogViewModel @Inject constructor() : ViewModel() {
    companion object {
        const val TAG: String = "PickDeviceDialogViewModel"
    }

    private val _serverEndpointId: MutableStateFlow<String?> = MutableStateFlow(null)
    val serverEndpointId: StateFlow<String?>
        get() = _serverEndpointId

    private val _message: MutableSharedFlow<String> = MutableSharedFlow(replay = 1)
    val message: Flow<String>
        get() = _message

    fun pickDevice(item: GmsNearbySessionItem) {
        _serverEndpointId.value = item.endpointId
        Log.i(TAG, "Picked : ${item.deviceName}")
    }
}