/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 10:15 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/8/25, 10:15 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.category_manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.CategoriesRepository
import dev.thomas_kiljanczyk.lyriccast.domain.models.CategoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

sealed interface CategoryManagerState {
    data class Ready(val categories: List<CategoryItem> = listOf()) : CategoryManagerState
}

@HiltViewModel
class CategoryManagerViewModel @Inject constructor(
    private val categoriesRepository: CategoriesRepository
) : ViewModel() {

    private val _state = MutableStateFlow<CategoryManagerState>(CategoryManagerState.Ready())
    val state = _state.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoryManagerState.Ready()
    )

    init {
        categoriesRepository.getAllCategories().onEach {
            val categoryItems =
                it.sortedBy { category -> category.name }.map { category -> CategoryItem(category) }
            _state.value = CategoryManagerState.Ready(
                categoryItems
            )
        }.flowOn(Dispatchers.Default).launchIn(viewModelScope)
    }

    suspend fun deleteSelectedCategories() {
        if (_state.value !is CategoryManagerState.Ready) return

        val selectedCategories =
            (_state.value as CategoryManagerState.Ready).categories.filter { it.isSelected }
                .map { item -> item.category.id }

        categoriesRepository.deleteCategories(selectedCategories)
    }

    fun cancelSelection() {
        _state.update { currentState ->
            if (currentState !is CategoryManagerState.Ready) {
                return@update currentState
            }

            currentState.copy(
                categories = currentState.categories.map { categoryItem ->
                    categoryItem.copy(isSelected = false)
                }
            )
        }
    }

    fun selectCategory(categoryId: String, selected: Boolean) {
        _state.update { currentState ->
            if (currentState !is CategoryManagerState.Ready) {
                return@update currentState
            }

            currentState.copy(
                categories = currentState.categories.map { categoryItem ->
                    if (categoryItem.category.id == categoryId) {
                        categoryItem.copy(
                            isSelected = selected
                        )
                    } else {
                        categoryItem
                    }
                })
        }
    }
}