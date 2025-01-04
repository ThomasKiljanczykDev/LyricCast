/*
 * Created by Tomasz Kiljanczyk on 04/01/2025, 16:41
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 03/01/2025, 00:51
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