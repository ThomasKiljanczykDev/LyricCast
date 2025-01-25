/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.datamodel.models

data class ImportOptions(
    val deleteAll: Boolean = false,
    val replaceOnConflict: Boolean = false,
    val colors: IntArray = intArrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImportOptions

        if (deleteAll != other.deleteAll) return false
        if (replaceOnConflict != other.replaceOnConflict) return false
        if (!colors.contentEquals(other.colors)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = deleteAll.hashCode()
        result = 31 * result + replaceOnConflict.hashCode()
        result = 31 * result + colors.contentHashCode()
        return result
    }
}
