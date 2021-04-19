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
import org.bson.types.ObjectId
import pl.gunock.lyriccast.datatransfer.models.SetlistDto

open class SetlistDocument(
    var name: String,
    var presentation: RealmList<SongDocument>,
    @field:PrimaryKey
    var id: ObjectId = ObjectId()
) : RealmObject() {

    @Ignore
    val idLong: Long = id.hashCode().toLong()

    fun toDto(): SetlistDto {
        val songs: List<String> = presentation.map { it.title }
        return SetlistDto(name, songs)
    }

    constructor() : this("", RealmList(), ObjectId())

    constructor(dto: SetlistDto) : this(dto.name, RealmList(), ObjectId())

    constructor(document: SetlistDocument, id: ObjectId) : this(
        document.name,
        document.presentation,
        id
    )
}