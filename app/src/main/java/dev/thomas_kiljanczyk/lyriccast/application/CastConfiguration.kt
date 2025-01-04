/*
 * Created by Tomasz Kiljanczyk on 04/01/2025, 16:41
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 03/01/2025, 00:43
 */

package dev.thomas_kiljanczyk.lyriccast.application

import kotlinx.serialization.Serializable

@Serializable
data class CastConfiguration(
    val backgroundColor: String,
    val fontColor: String,
    val maxFontSize: Int
)