/*
 * Created by Tomasz Kiljanczyk on 6/7/25, 5:57 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 1/25/25, 6:56 PM
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