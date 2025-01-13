/*
 * Created by Tomasz Kiljanczyk on 04/01/2025, 16:41
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 01/01/2025, 23:30
 */

package dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby

import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate

class SimpleNearbyPayloadCallback(
    private val onPayloadReceived: (ByteArray?) -> Unit
) : PayloadCallback() {
    override fun onPayloadReceived(
        endpointId: String,
        payload: Payload
    ) {
        onPayloadReceived(payload.asBytes())
    }

    override fun onPayloadTransferUpdate(
        endpointId: String,
        payload: PayloadTransferUpdate
    ) {
    }
}