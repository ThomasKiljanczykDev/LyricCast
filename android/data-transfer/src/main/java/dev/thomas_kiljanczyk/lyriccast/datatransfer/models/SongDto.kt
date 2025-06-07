/*
 * Created by Tomasz Kiljanczyk on 6/7/25, 5:57 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 1/25/25, 6:56 PM
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