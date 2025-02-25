/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.datamodel.models

import dev.thomas_kiljanczyk.lyriccast.datatransfer.models.CategoryDto

data class Category(
    var name: String,
    var color: Int? = null,
    var id: String = ""
) : Comparable<Category> {

    val idLong: Long = id.hashCode().toLong()

    internal constructor(dto: CategoryDto) : this(dto.name, dto.color, "")

    internal fun toDto(): CategoryDto {
        return CategoryDto(name, color)
    }

    override fun compareTo(other: Category): Int {
        return name.compareTo(other.name)
    }

}