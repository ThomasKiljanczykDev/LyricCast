/*
 * Created by Tomasz Kiljanczyk on 4/20/21 1:30 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/20/21 1:16 AM
 */

package pl.gunock.lyriccast.datamodel.documents

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.bson.types.ObjectId
import pl.gunock.lyriccast.datatransfer.models.SongDto

open class SongDocument(
    var title: String,
    var lyrics: RealmList<LyricsSectionDocument>,
    @field:Required
    var presentation: RealmList<String>,
    var category: CategoryDocument? = null,
    @field:PrimaryKey
    var id: ObjectId = ObjectId()
) : RealmObject() {

    @Ignore
    val idLong: Long = id.hashCode().toLong()

    @Ignore
    val lyricsMap: Map<String, String> = lyrics.map { it.name to it.text }.toMap()

    @Ignore
    val lyricsList: List<String> = presentation.map { lyricsMap[it]!! }

    constructor() : this("", RealmList(), RealmList(), null, ObjectId())

    constructor(dto: SongDto, category: CategoryDocument?) : this(
        dto.title,
        RealmList(),
        RealmList(),
        category,
        ObjectId()
    )

    constructor(document: SongDocument, id: ObjectId) : this(
        document.title,
        document.lyrics,
        document.presentation,
        document.category,
        id
    )

    fun toDto(): SongDto {
        return SongDto(title, lyricsMap, presentation, category?.name ?: "")
    }

    override fun toString(): String {
        return "SongDocument(title='$title', lyrics=$lyrics, presentation=$presentation, category=$category)"
    }

}