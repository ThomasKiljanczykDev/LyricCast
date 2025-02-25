/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 10/01/2025, 01:46
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