/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 10:15 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/8/25, 10:15 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.category_manager.edit_category

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Category
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.CategoriesRepository
import dev.thomas_kiljanczyk.lyriccast.domain.models.ColorItem
import dev.thomas_kiljanczyk.lyriccast.domain.models.UiText
import dev.thomas_kiljanczyk.lyriccast.domain.use_case.ValidateCategoryName
import dev.thomas_kiljanczyk.lyriccast.domain.use_case.ValidateCategoryNameContext
import dev.thomas_kiljanczyk.lyriccast.ui.shared.misc.colorItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

sealed interface AddOrEditCategoryState {
    data class Ready(
        val id: String? = null,
        val name: String = "",
        val initialName: String? = null,
        val nameError: UiText? = null,
        val color: ColorItem = colorItems.first()
    ) : AddOrEditCategoryState {
        val isValid: Boolean
            get() = nameError == null
    }
}

@HiltViewModel
class AddOrEditCategoryDialogViewModel @Inject constructor(
    private val categoriesRepository: CategoriesRepository,
) : ViewModel() {
    private val validateCategoryName: ValidateCategoryName = ValidateCategoryName()

    private var _state by mutableStateOf(AddOrEditCategoryState.Ready())
    val state: AddOrEditCategoryState
        get() = _state

    private var _categoryNames: Set<String> = setOf()

    init {
        categoriesRepository.getAllCategories()
            .onEach { categories -> _categoryNames = categories.map { it.name }.toSet() }
            .flowOn(Dispatchers.Default).launchIn(viewModelScope)
    }

    suspend fun onEvent(event: AddOrEditCategoryFormEvent) {
        when (event) {
            is AddOrEditCategoryFormEvent.CategoryInitialized -> {
                val colorItem = colorItems.firstOrNull {
                    it.value == event.category.color
                } ?: colorItems.first()

                _state = AddOrEditCategoryState.Ready(
                    id = event.category.id,
                    name = event.category.name,
                    initialName = event.category.name,
                    color = colorItem
                )
            }

            is AddOrEditCategoryFormEvent.CategoryIdChanged -> {
                _state = _state.copy(
                    id = event.id
                )
            }

            is AddOrEditCategoryFormEvent.CategoryNameChanged -> {
                val newName =
                    event.name.take(ValidateCategoryName.MAX_LENGTH).uppercase()

                val categoryNames = if (_state.initialName == null) {
                    _categoryNames
                } else {
                    _categoryNames - _state.initialName!!
                }

                val validationResult =
                    validateCategoryName(newName, ValidateCategoryNameContext(categoryNames))
                _state = _state.copy(
                    name = newName,
                    nameError = validationResult.errorMessage,
                )
            }

            is AddOrEditCategoryFormEvent.CategoryColorChanged -> {
                _state = _state.copy(
                    color = event.colorItem
                )
            }

            is AddOrEditCategoryFormEvent.Submit -> {
                if (!_state.isValid) {
                    return
                }

                submit()
            }

        }
    }

    private suspend fun submit() {
        val category =
            Category(
                name = _state.name,
                color = _state.color.value,
                id = _state.id ?: ""
            )

        categoriesRepository.upsertCategory(category)
    }

}
