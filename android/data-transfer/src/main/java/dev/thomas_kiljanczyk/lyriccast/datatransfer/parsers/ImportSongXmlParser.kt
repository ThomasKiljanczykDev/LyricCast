/*
 * Created by Tomasz Kiljanczyk on 6/7/25, 5:57 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 1/25/25, 6:56 PM
 */

package dev.thomas_kiljanczyk.lyriccast.datatransfer.parsers

import dev.thomas_kiljanczyk.lyriccast.datatransfer.models.SongDto
import java.io.File
import java.io.InputStream

abstract class ImportSongXmlParser(filesDir: File) {
    protected val importDirectory: File = File(filesDir.canonicalPath, ".import")

    abstract fun parseZip(inputStream: InputStream): Set<SongDto>

    abstract fun parse(inputStream: InputStream?, category: String = ""): SongDto
}