/*
 * Created by Tomasz Kiljanczyk on 6/7/25, 5:57 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 1/25/25, 6:56 PM
 */

package dev.thomas_kiljanczyk.lyriccast.datatransfer.factories

import dev.thomas_kiljanczyk.lyriccast.datatransfer.enums.SongXmlParserType
import dev.thomas_kiljanczyk.lyriccast.datatransfer.parsers.ImportSongXmlParser
import dev.thomas_kiljanczyk.lyriccast.datatransfer.parsers.OpenSongXmlParser
import java.io.File

object ImportSongXmlParserFactory {

    fun create(fileDir: File, type: SongXmlParserType): ImportSongXmlParser {
        return when (type) {
            SongXmlParserType.OPEN_SONG -> OpenSongXmlParser(fileDir)
        }
    }

}