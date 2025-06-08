/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 10:15 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/8/25, 10:01 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.category_manager.edit_category

import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Category
import dev.thomas_kiljanczyk.lyriccast.domain.models.ColorItem

sealed class AddOrEditCategoryFormEvent {
    data class CategoryInitialized(
        val category: Category
    ) : AddOrEditCategoryFormEvent()

    data class CategoryIdChanged(val id: String?) : AddOrEditCategoryFormEvent()
    data class CategoryNameChanged(val name: String) : AddOrEditCategoryFormEvent()
    data class CategoryColorChanged(val colorItem: ColorItem) :
        AddOrEditCategoryFormEvent()

    data object Submit : AddOrEditCategoryFormEvent()
}