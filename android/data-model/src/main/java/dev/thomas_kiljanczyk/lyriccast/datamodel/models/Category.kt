/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 10:15 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/8/25, 9:17 PM
 */

package dev.thomas_kiljanczyk.lyriccast.datamodel.models

import dev.thomas_kiljanczyk.lyriccast.datatransfer.models.CategoryDto

/**
 * @property color the ARGB color of the category, or null if not set
 */
data class Category(
    var name: String,

    var color: Int? = null,
    var id: String = ""
) : Comparable<Category> {
    internal constructor(dto: CategoryDto) : this(dto.name, dto.color, "")

    internal fun toDto(): CategoryDto {
        return CategoryDto(name, color)
    }

    override fun compareTo(other: Category): Int {
        return name.compareTo(other.name)
    }

}