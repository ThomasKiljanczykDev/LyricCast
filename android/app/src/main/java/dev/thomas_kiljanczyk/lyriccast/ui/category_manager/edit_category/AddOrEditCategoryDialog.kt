/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 10:15 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/8/25, 10:15 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.category_manager.edit_category

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Category
import dev.thomas_kiljanczyk.lyriccast.domain.models.ColorItem
import dev.thomas_kiljanczyk.lyriccast.domain.models.UiText
import dev.thomas_kiljanczyk.lyriccast.domain.use_case.ValidateCategoryName
import dev.thomas_kiljanczyk.lyriccast.ui.shared.components.LyricCastSpinner
import dev.thomas_kiljanczyk.lyriccast.ui.shared.components.LyricCastTextField
import dev.thomas_kiljanczyk.lyriccast.ui.shared.misc.colorItems
import dev.thomas_kiljanczyk.lyriccast.ui.shared.theme.LyricCastTheme
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun AddOrEditCategoryForm(
    state: AddOrEditCategoryState.Ready,
    onColorChange: (ColorItem) -> Unit = {},
    onNameChange: (String) -> Unit = {}
) {
    val colorItems = remember { colorItems }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LyricCastTextField(
            label = stringResource(R.string.category_manager_hint_category_name),
            value = state.name,
            onValueChange = { onNameChange(it) },
            maxLength = ValidateCategoryName.MAX_LENGTH,
            errorText = state.nameError?.asString(),
            singleLine = true
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val animatedColor by animateColorAsState(
                targetValue = Color(state.color.value)
            )
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = animatedColor
                ), modifier = Modifier
                    .padding(start = 8.dp)
                    .height(30.dp)
                    .width(30.dp)
            ) {}
            LyricCastSpinner(
                options = colorItems,
                value = state.color.name.asString(),
                label = stringResource(R.string.category_manager_hint_category_color),
                modifier = Modifier.fillMaxWidth(),
                onOptionSelected = {
                    onColorChange(it)
                }) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(it.value)
                        ), modifier = Modifier
                            .height(30.dp)
                            .width(30.dp)
                    ) {}
                    Text(text = it.name.asString())
                }
            }
        }
    }
}


@PreviewLightDark
@Composable
fun PreviewAddOrEditCategoryForm() {
    LyricCastTheme {
        Surface(modifier = Modifier.height(500.dp)) {
            AddOrEditCategoryForm(
                state = AddOrEditCategoryState.Ready(
                    id = "12345", name = "Sample Category", color = ColorItem(
                        name = UiText.DynamicString("Red"), value = Color.Red.toArgb()
                    )
                )
            )
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun AddOrEditCategoryDialog(
    category: Category?, onDismiss: () -> Unit = {}
) {
    val dialogKey = remember { Uuid.random().toString() }
    val viewModel: AddOrEditCategoryDialogViewModel = hiltViewModel(key = dialogKey)

    val state = viewModel.state
    val scope = rememberCoroutineScope()

    LaunchedEffect(category) {
        if (category != null) {
            viewModel.onEvent(AddOrEditCategoryFormEvent.CategoryInitialized(category))
        }
    }

    when (state) {
        is AddOrEditCategoryState.Ready -> AddOrEditCategoryDialog(
            state = state,
            onColorChange = { colorItem ->
                scope.launch {
                    viewModel.onEvent(AddOrEditCategoryFormEvent.CategoryColorChanged(colorItem))
                }
            },
            onNameChange = { name ->
                scope.launch {
                    viewModel.onEvent(AddOrEditCategoryFormEvent.CategoryNameChanged(name))
                }
            },
            onSubmit = {
                scope.launch {
                    viewModel.onEvent(AddOrEditCategoryFormEvent.Submit)
                    onDismiss()
                }
            },
            onDismiss = onDismiss
        )
    }
}


@Composable
fun AddOrEditCategoryDialog(
    state: AddOrEditCategoryState.Ready,
    onColorChange: (ColorItem) -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    AlertDialog(onDismissRequest = { onDismiss() }, title = {
        Text(
            text = if (state.id != null) {
                stringResource(R.string.category_manager_edit_category)
            } else {
                stringResource(R.string.category_manager_add_category)
            }
        )
    }, confirmButton = {
        TextButton(
            enabled = state.isValid, onClick = {
                onSubmit()
            }) {
            Text(
                text = stringResource(R.string.editor_button_save)
            )
        }
    }, dismissButton = {
        TextButton(onClick = { onDismiss() }) {
            Text(
                text = stringResource(android.R.string.cancel)
            )
        }
    }, text = {
        AddOrEditCategoryForm(
            state = state, onColorChange = onColorChange, onNameChange = onNameChange
        )
    })
}

@PreviewLightDark
@Composable
fun PreviewAddOrEditCategoryDialog_Add() {
    LyricCastTheme {
        Surface {
            AddOrEditCategoryDialog(
                state = AddOrEditCategoryState.Ready(
                    id = null, name = "", color = ColorItem(
                        name = UiText.DynamicString("Red"), value = Color.Red.toArgb()
                    )
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewAddOrEditCategoryDialog_Edit() {
    LyricCastTheme {
        Surface {
            AddOrEditCategoryDialog(
                state = AddOrEditCategoryState.Ready(
                    id = "12345", name = "Sample Category", color = ColorItem(
                        name = UiText.DynamicString("Red"), value = Color.Red.toArgb()
                    )
                ),
            )
        }
    }
}
