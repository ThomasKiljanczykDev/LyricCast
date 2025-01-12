/*
 * Created by Tomasz Kiljanczyk on 12/01/2025, 23:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 12/01/2025, 23:00
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.menu.gms_nearby_session.dialog

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.GmsNearbySessionServerContext
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.GmsNearbySessionServerContext.AdvertisingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class StartSessionServerDialogModel @Inject constructor(
    private val gmsNearbySessionServerContext: GmsNearbySessionServerContext
) : ViewModel() {
    data class ServerAdvertisingState(
        val state: AdvertisingState, @StringRes val explanationResId: Int?
    )

    var sessionNameIsValid: MutableStateFlow<Boolean> = MutableStateFlow(true)
    var sessionName = ""

    val serverAdvertisingState: Flow<ServerAdvertisingState> =
        gmsNearbySessionServerContext.advertisingState.map {
            @StringRes
            var explanationResId: Int? = null
            // MISSING_PERMISSION_BLUETOOTH_ADVERTISE status encountered
            if (it.exception is ApiException && it.exception.status.statusCode == 8038) {
                explanationResId =
                    R.string.dialog_fragment_start_session_session_start_missing_permissions
            }

            return@map ServerAdvertisingState(it.state, explanationResId)
        }

    fun startSessionServer() {
        if (!sessionNameIsValid.value) {
            return
        }

        gmsNearbySessionServerContext.startServer(sessionName)
    }
}