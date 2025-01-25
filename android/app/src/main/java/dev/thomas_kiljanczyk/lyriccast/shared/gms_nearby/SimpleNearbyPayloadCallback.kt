/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 10/01/2025, 01:46
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