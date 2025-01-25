/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.datatransfer.enums

enum class ImportFormat(val displayName: String) {
    NONE("NONE"),
    OPEN_SONG("OpenSong"),
    LYRIC_CAST("LyricCast");

    companion object {
        fun getByName(name: String): ImportFormat {
            return entries.first { it.displayName == name }
        }
    }
}