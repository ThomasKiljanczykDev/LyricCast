/*
 * Created by Tomasz Kiljanczyk on 5/31/25, 2:51 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 5/31/25, 2:02 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.category_manager.edit_category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Category
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.CategoriesRepository
import dev.thomas_kiljanczyk.lyriccast.domain.models.ColorItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class EditCategoryDialogViewModel @Inject constructor(
    private val categoriesRepository: CategoriesRepository
) : ViewModel() {

    val categoryNames: Set<String> get() = _categoryNames
    private var _categoryNames: Set<String> = setOf()

    var categoryId: String = ""

    var categoryName: String = ""

    var categoryColor: ColorItem? = null

    init {
        categoriesRepository.getAllCategories()
            .onEach { categories -> _categoryNames = categories.map { it.name }.toSet() }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    suspend fun saveCategory() {
        val category =
            Category(name = categoryName, color = categoryColor?.value, id = categoryId)

        categoriesRepository.upsertCategory(category)
    }

}