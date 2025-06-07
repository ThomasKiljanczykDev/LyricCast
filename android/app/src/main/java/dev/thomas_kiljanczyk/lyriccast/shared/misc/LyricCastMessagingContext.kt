/*
 * Created by Tomasz Kiljanczyk on 6/7/25, 7:31 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/7/25, 7:30 PM
 */

package dev.thomas_kiljanczyk.lyriccast.shared.misc

import dev.thomas_kiljanczyk.lyriccast.shared.cast.CastMessagingContext
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.GmsNearbySessionServerContext
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.ShowLyricsContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json

@OptIn(FlowPreview::class)
class LyricCastMessagingContext(
    private val castMessagingContext: CastMessagingContext,
    private val gmsNearbySessionServerContext: GmsNearbySessionServerContext
) {
    val receivedPayload get() = gmsNearbySessionServerContext.receivedPayload

    private val googleCastContentMessage = MutableSharedFlow<String>()
    private val gmsNearbyBroadcastMessage = MutableSharedFlow<String>()

    init {
        googleCastContentMessage.debounce(500).onEach { message ->
            castMessagingContext.sendContentMessage(message)
        }.launchIn(CoroutineScope(Dispatchers.IO))

        gmsNearbyBroadcastMessage.debounce(500).onEach { message ->
            gmsNearbySessionServerContext.broadcastMessage(message)
        }.launchIn(CoroutineScope(Dispatchers.IO))
    }

    suspend fun broadcastContentMessage(content: ShowLyricsContent) {
        castMessagingContext.sendContentMessage(content.slideText)
        if (gmsNearbySessionServerContext.serverIsRunning.value) {
            val messageJson = Json.encodeToString(
                SessionClientMessage(
                    SessionClientCommand.SHOW_SLIDE, content
                )
            )

            gmsNearbyBroadcastMessage.emit(messageJson)
        }
    }

    fun sendContentMessage(endpointId: String, content: ShowLyricsContent) {
        if (!gmsNearbySessionServerContext.serverIsRunning.value) {
            return
        }

        val messageJson = Json.encodeToString(
            SessionClientMessage(
                SessionClientCommand.SHOW_SLIDE, content
            )
        )

        gmsNearbySessionServerContext.sendMessage(endpointId, messageJson)
    }
}