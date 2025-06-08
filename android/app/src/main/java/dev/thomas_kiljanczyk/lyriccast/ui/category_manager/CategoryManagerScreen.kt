/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 10:15 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/8/25, 10:15 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.category_manager

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Category
import dev.thomas_kiljanczyk.lyriccast.domain.models.CategoryItem
import dev.thomas_kiljanczyk.lyriccast.ui.category_manager.edit_category.AddOrEditCategoryDialog
import dev.thomas_kiljanczyk.lyriccast.ui.shared.theme.LyricCastTheme
import kotlinx.coroutines.launch

@Composable
fun CategoryManagerScreen(
    onNavigateUp: () -> Unit, viewModel: CategoryManagerViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (state) {
        is CategoryManagerState.Ready -> {
            CategoryManagerScreen(
                state = state as CategoryManagerState.Ready,
                onCancelSelection = { viewModel.cancelSelection() },
                onItemSelected = { categoryItem, isSelected ->
                    viewModel.selectCategory(categoryItem.category.id, isSelected)
                },
                onDelete = {
                    scope.launch {
                        viewModel.deleteSelectedCategories()
                    }
                },
                onNavigateUp = onNavigateUp
            )
        }
    }
}

@Composable
fun CategoryManagerScreen(
    state: CategoryManagerState.Ready,
    onItemSelected: (CategoryItem, Boolean) -> Unit = { _, _ -> },
    onCancelSelection: () -> Unit = { },
    onDelete: () -> Unit = { },
    onNavigateUp: () -> Unit = { }
) {
    var categoryToEdit by remember { mutableStateOf<CategoryItem?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(), topBar = {
            CategoryManagerTopBar(
                onNavigateUp = onNavigateUp,
                numberOfSelectedItems = state.categories.count { it.isSelected },
                onCancelSelection = onCancelSelection,
                onDelete = onDelete,
                onEdit = {
                    val selectedCategory = state.categories.firstOrNull { it.isSelected }
                    categoryToEdit = selectedCategory
                    if (selectedCategory != null) {
                        showAddCategoryDialog = true
                        onItemSelected(
                            selectedCategory,
                            false
                        )
                    }
                },
                onAdd = {
                    showAddCategoryDialog = true
                })
        }) { paddingValues ->
        CategoryList(
            categories = state.categories,
            onCategorySelected = onItemSelected,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 4.dp)
        )
        if (showAddCategoryDialog) {
            AddOrEditCategoryDialog(
                category = categoryToEdit?.category,
                onDismiss = {
                    showAddCategoryDialog = false
                })
        }
    }
}


@PreviewLightDark
@Composable
fun CategoryManagerScreenPreview() {
    LyricCastTheme {
        CategoryManagerScreen(
            state = CategoryManagerState.Ready(
                categories = List(20) { index ->
                    CategoryItem(
                        category = Category(
                            id = index.toString(), name = "Category $index", color = R.color.red
                        )
                    )
                },
            )
        )
    }
}