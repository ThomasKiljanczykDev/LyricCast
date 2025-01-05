/*
 * Created by Tomasz Kiljanczyk on 05/01/2025, 19:35
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 05/01/2025, 18:38
 */

package dev.thomas_kiljanczyk.lyriccast.shared.cast

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TextCastMessage(
    val text: String
) {
    fun toJson(): String {
        return Json.encodeToString(this)
    }
}