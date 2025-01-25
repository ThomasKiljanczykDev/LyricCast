/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 10/01/2025, 01:46
 */

package dev.thomas_kiljanczyk.lyriccast.datatransfer.models

import kotlinx.serialization.Serializable

@Serializable
data class SetlistDto(val name: String, val songs: List<String>)