/*
 * Created by Tomasz Kiljanczyk on 04/01/2025, 16:41
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 01/01/2025, 23:30
 */

package dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby

import androidx.annotation.CallSuper
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution

open class NearbyConnectionLifecycleCallback : ConnectionLifecycleCallback() {
    private val connectionInfoMap = mutableMapOf<String, ConnectionInfo>()

    @CallSuper
    override fun onConnectionInitiated(
        endpointId: String,
        connectionInfo: ConnectionInfo
    ) {
        connectionInfoMap[endpointId] = connectionInfo

    }

    protected open fun onConnectionResult(
        endpointId: String,
        connectionInfo: ConnectionInfo?,
        result: ConnectionResolution
    ) {

    }

    final override fun onConnectionResult(
        endpointId: String,
        result: ConnectionResolution
    ) {
        val connectionInfo = connectionInfoMap[endpointId]
        onConnectionResult(endpointId, connectionInfo, result)

        if (!result.status.isSuccess) {
            connectionInfoMap.remove(endpointId)
        }
    }

    protected open fun onDisconnected(endpointId: String, connectionInfo: ConnectionInfo?) {

    }

    final override fun onDisconnected(endpointId: String) {
        val info = connectionInfoMap.remove(endpointId)
        onDisconnected(endpointId, info)

        connectionInfoMap.remove(endpointId)
    }
}