/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 10/01/2025, 01:46
 */

package dev.thomas_kiljanczyk.lyriccast.shared.cast

import kotlinx.serialization.Serializable

@Serializable
data class ControlCastMessage<T>(
    val action: String,
    val value: T
)