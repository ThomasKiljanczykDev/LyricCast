/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.domain.models

import dev.thomas_kiljanczyk.lyriccast.common.extensions.normalize
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Setlist

data class SetlistItem(
    val setlist: Setlist
) : Comparable<SetlistItem> {

    val normalizedName = setlist.name.normalize()
    var isSelected: Boolean = false
    var hasCheckbox: Boolean = false

    override fun compareTo(other: SetlistItem): Int {
        return setlist.name.compareTo(other.setlist.name)
    }
}