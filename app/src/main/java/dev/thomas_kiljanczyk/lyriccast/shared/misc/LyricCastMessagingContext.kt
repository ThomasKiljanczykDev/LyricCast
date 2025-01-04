/*
 * Created by Tomasz Kiljanczyk on 04/01/2025, 16:41
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 04/01/2025, 16:37
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
    fun sendContentMessage(content: ShowLyricsContent) {
        castMessagingContext.sendContentMessage(content.slideText)
        if (gmsNearbySessionServerContext.serverIsRunning.value) {
            val message = SessionServerMessage(
                SessionCommand.SHOW_LYRICS,
                content
            )
            val messageJson = Json.encodeToString(message)

            gmsNearbySessionServerContext.broadcastMessage(messageJson)
        }
    }
}