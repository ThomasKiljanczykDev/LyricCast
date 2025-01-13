/*
 * Created by Tomasz Kiljanczyk on 13/01/2025, 09:48
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 13/01/2025, 09:47
 */

package dev.thomas_kiljanczyk.lyriccast.application

import kotlinx.serialization.Serializable

@Serializable
data class CastConfiguration(
    val backgroundColor: String,
    val fontColor: String,
    val maxFontSize: Int
)
