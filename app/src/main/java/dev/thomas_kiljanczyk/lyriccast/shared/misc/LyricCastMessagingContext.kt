/*
 * Created by Tomasz Kiljanczyk on 06/01/2025, 01:11
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 05/01/2025, 22:22
 */

package dev.thomas_kiljanczyk.lyriccast.shared.misc

import dev.thomas_kiljanczyk.lyriccast.shared.cast.CastMessagingContext
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.GmsNearbySessionServerContext
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.ShowLyricsContent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LyricCastMessagingContext(
    private val castMessagingContext: CastMessagingContext,
    private val gmsNearbySessionServerContext: GmsNearbySessionServerContext
) {

    val receivedPayload get() = gmsNearbySessionServerContext.receivedPayload

    suspend fun broadcastContentMessage(content: ShowLyricsContent) {
        castMessagingContext.sendContentMessage(content.slideText)
        if (gmsNearbySessionServerContext.serverIsRunning.value) {
            val messageJson = Json.encodeToString(
                SessionClientMessage(
                    SessionClientCommand.SHOW_SLIDE, content
                )
            )

            gmsNearbySessionServerContext.broadcastMessage(messageJson)
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