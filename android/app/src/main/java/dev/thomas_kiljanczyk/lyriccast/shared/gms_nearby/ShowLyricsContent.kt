/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 10/01/2025, 01:46
 */

package dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby

import kotlinx.serialization.Serializable

@Serializable
data class ShowLyricsContent(
    val songTitle: String,
    val slideText: String,
    val slideNumber: Int,
    val totalSlides: Int
)