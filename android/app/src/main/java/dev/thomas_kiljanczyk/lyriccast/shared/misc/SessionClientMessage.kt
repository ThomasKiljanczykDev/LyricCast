/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 10/01/2025, 01:46
 */

package dev.thomas_kiljanczyk.lyriccast.shared.misc

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Serializable
data class SessionClientMessage<T>(
    val command: SessionClientCommand,
    val content: T
) {
    companion object {
        const val TAG = "SessionClientMessage"

        inline fun <reified T> fromJson(json: String): SessionClientMessage<T>? {
            try {
                return Json.decodeFromString<SessionClientMessage<T>>(json)
            } catch (ex: Exception) {
                when (ex) {
                    is SerializationException, is IllegalArgumentException -> {
                        Log.w(TAG, "Failed to decode JSON", ex)
                        return null
                    }

                    else -> throw ex
                }
            }
        }
    }
}