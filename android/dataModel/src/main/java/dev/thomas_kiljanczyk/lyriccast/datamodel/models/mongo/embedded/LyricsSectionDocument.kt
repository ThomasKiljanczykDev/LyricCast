/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 27/12/2024, 02:15
 */

package dev.thomas_kiljanczyk.lyriccast.datamodel.models.mongo.embedded

import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Song
import io.realm.kotlin.types.EmbeddedRealmObject

internal open class LyricsSectionDocument() : EmbeddedRealmObject {
    var name: String = ""
    var text: String = ""

    constructor(name: String, text: String) : this() {
        this.name = name
        this.text = text
    }

    constructor(lyricsSection: Song.LyricsSection) : this(lyricsSection.name, lyricsSection.text)

    fun toGenericModel(): Song.LyricsSection {
        return Song.LyricsSection(
            name = name,
            text = text
        )
    }

    override fun toString(): String {
        return "LyricsSectionDocument(name='$name', text='$text')"
    }

}
