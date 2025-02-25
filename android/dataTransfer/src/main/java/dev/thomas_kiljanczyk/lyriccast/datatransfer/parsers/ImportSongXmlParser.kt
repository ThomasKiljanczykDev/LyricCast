/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 08/12/2024, 21:35
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