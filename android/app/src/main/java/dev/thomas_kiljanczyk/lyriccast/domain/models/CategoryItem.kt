/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 10:15 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/8/25, 1:51 PM
 */

package dev.thomas_kiljanczyk.lyriccast.domain.models

import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Category

data class CategoryItem(
    val category: Category,
    var isSelected: Boolean = false
) : Comparable<CategoryItem> {
    override fun compareTo(other: CategoryItem): Int {
        return category.name.compareTo(other.category.name)
    }

    override fun toString(): String {
        return category.name
    }
}
