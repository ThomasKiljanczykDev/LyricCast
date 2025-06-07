/*
 * Created by Tomasz Kiljanczyk on 6/7/25, 5:57 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 1/25/25, 6:56 PM
 */

package dev.thomas_kiljanczyk.lyriccast.datatransfer.models

import kotlinx.serialization.Serializable

@Serializable
data class SetlistDto(val name: String, val songs: List<String>)