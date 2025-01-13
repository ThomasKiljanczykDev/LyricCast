/*
 * Created by Tomasz Kiljanczyk on 05/01/2025, 19:35
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 05/01/2025, 18:35
 */

package dev.thomas_kiljanczyk.lyriccast.shared.misc

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SessionServerMessage(
    val command: SessionServerCommand
) {
    companion object {
        const val TAG = "SessionServerMessage"

        fun fromJson(json: String): SessionServerMessage? {
            try {
                return Json.decodeFromString<SessionServerMessage>(json)
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

    fun toJson(): String {
        return Json.encodeToString(this)
    }
}