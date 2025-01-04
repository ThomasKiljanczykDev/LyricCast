/*
 * Created by Tomasz Kiljanczyk on 04/01/2025, 16:41
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 03/01/2025, 00:38
 */

package dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby

import com.google.android.gms.common.api.internal.ApiKey
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionOptions
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsOptions
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.tasks.Task

// TODO: finish implementation
class ConnectionsClientFakeImpl : ConnectionsClient {
    override fun getApiKey(): ApiKey<ConnectionsOptions> {
        TODO("Not yet implemented")
    }

    override fun acceptConnection(p0: String, p1: PayloadCallback): Task<Void> {
        TODO("Not yet implemented")
    }

    override fun cancelPayload(p0: Long): Task<Void> {
        TODO("Not yet implemented")
    }

    override fun rejectConnection(p0: String): Task<Void> {
        TODO("Not yet implemented")
    }

    override fun requestConnection(
        p0: String,
        p1: String,
        p2: ConnectionLifecycleCallback
    ): Task<Void> {
        TODO("Not yet implemented")
    }

    override fun requestConnection(
        p0: ByteArray,
        p1: String,
        p2: ConnectionLifecycleCallback
    ): Task<Void> {
        TODO("Not yet implemented")
    }

    override fun requestConnection(
        p0: String,
        p1: String,
        p2: ConnectionLifecycleCallback,
        p3: ConnectionOptions
    ): Task<Void> {
        TODO("Not yet implemented")
    }

    override fun requestConnection(
        p0: ByteArray,
        p1: String,
        p2: ConnectionLifecycleCallback,
        p3: ConnectionOptions
    ): Task<Void> {
        TODO("Not yet implemented")
    }

    override fun sendPayload(p0: String, p1: Payload): Task<Void> {
        TODO("Not yet implemented")
    }

    override fun sendPayload(p0: MutableList<String>, p1: Payload): Task<Void> {
        TODO("Not yet implemented")
    }

    override fun startAdvertising(
        p0: String,
        p1: String,
        p2: ConnectionLifecycleCallback,
        p3: AdvertisingOptions
    ): Task<Void> {
        TODO("Not yet implemented")
    }

    override fun startAdvertising(
        p0: ByteArray,
        p1: String,
        p2: ConnectionLifecycleCallback,
        p3: AdvertisingOptions
    ): Task<Void> {
        TODO("Not yet implemented")
    }

    override fun startDiscovery(
        p0: String,
        p1: EndpointDiscoveryCallback,
        p2: DiscoveryOptions
    ): Task<Void> {
        TODO("Not yet implemented")
    }

    override fun disconnectFromEndpoint(p0: String) {
        TODO("Not yet implemented")
    }

    override fun stopAdvertising() {
        TODO("Not yet implemented")
    }

    override fun stopAllEndpoints() {
        TODO("Not yet implemented")
    }

    override fun stopDiscovery() {
        TODO("Not yet implemented")
    }
}