/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 25/01/2025, 18:55
 */

package dev.thomas_kiljanczyk.lyriccast.shared.cast

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TextCastMessage(
    val text: String
) {
    fun toJson(): String {
        return Json.encodeToString(this)
    }
}