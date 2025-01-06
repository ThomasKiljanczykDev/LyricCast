/*
 * Created by Tomasz Kiljanczyk on 06/01/2025, 18:29
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 06/01/2025, 18:29
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.menu.gms_nearby_session.dialog

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.GmsNearbySessionServerContext
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.GmsNearbySessionServerContext.AdvertisingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class StartSessionServerDialogModel @Inject constructor(
    private val gmsNearbySessionServerContext: GmsNearbySessionServerContext
) : ViewModel() {
    var sessionNameIsValid: MutableStateFlow<Boolean> = MutableStateFlow(true)
    var sessionName = ""

    val serverAdvertisingState: Flow<AdvertisingState> =
        gmsNearbySessionServerContext.advertisingState

    fun startSessionServer() {
        if (!sessionNameIsValid.value) {
            return
        }

        try {
            gmsNearbySessionServerContext.startServer(sessionName)
        } catch (ex: SecurityException) {
            // TODO: handle exception
        }
    }
}