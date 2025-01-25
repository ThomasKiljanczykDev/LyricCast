/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 10/01/2025, 01:46
 */

package dev.thomas_kiljanczyk.lyriccast.datatransfer.models

import kotlinx.serialization.Serializable

@Serializable
data class SongDto(
    val title: String,
    val lyrics: Map<String, String>,
    val presentation: List<String>,
    val category: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is SongDto) {
            return false
        }
        return title == other.title
    }

    override fun hashCode(): Int {
        return title.hashCode()
    }
}