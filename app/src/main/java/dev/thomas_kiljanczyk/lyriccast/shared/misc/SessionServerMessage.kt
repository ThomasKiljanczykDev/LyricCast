/*
 * Created by Tomasz Kiljanczyk on 04/01/2025, 16:41
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 04/01/2025, 16:11
 */

package dev.thomas_kiljanczyk.lyriccast.shared.misc

import kotlinx.serialization.Serializable

@Serializable
data class SessionServerMessage<T>(
    val command: SessionCommand,
    val content: T
)