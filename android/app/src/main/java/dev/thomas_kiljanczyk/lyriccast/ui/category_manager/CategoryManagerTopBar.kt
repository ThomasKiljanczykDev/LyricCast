/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 10:15 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/8/25, 9:49 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.category_manager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.ui.shared.theme.LyricCastTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagerTopBar(
    numberOfSelectedItems: Int,
    onAdd: () -> Unit = {},
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
    onCancelSelection: () -> Unit = {},
    onNavigateUp: () -> Unit = {}
) {
    val hasSelection = numberOfSelectedItems > 0

    TopAppBar(title = {
        AnimatedVisibility(
            !hasSelection,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            Text(stringResource(R.string.title_categories))
        }

    }, navigationIcon = {
        AnimatedVisibility(
            !hasSelection,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_up)
                )
            }
        }
        AnimatedVisibility(
            hasSelection,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            IconButton(onClick = onCancelSelection) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(android.R.string.cancel)
                )
            }
        }
    }, actions = {
        AnimatedVisibility(hasSelection) {
            Row {
                AnimatedVisibility(numberOfSelectedItems == 1) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(android.R.string.cancel)
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = stringResource(android.R.string.cancel)
                    )
                }
            }
        }
        AnimatedVisibility(!hasSelection) {
            TextButton(
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                ), onClick = {
                    onAdd()
                }, modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.editor_button_add),
                )
            }
        }

    })
}

@PreviewLightDark
@Composable
fun CategoryManagerTopBarPreview_NotHasSelection() {
    LyricCastTheme {
        Surface {
            CategoryManagerTopBar(numberOfSelectedItems = 0)
        }
    }
}

@PreviewLightDark
@Composable
fun CategoryManagerTopBarPreview_HasSingleSelection() {
    LyricCastTheme {
        Surface {
            CategoryManagerTopBar(numberOfSelectedItems = 1)
        }
    }
}

@PreviewLightDark
@Composable
fun CategoryManagerTopBarPreview_HasMultipleSelections() {
    LyricCastTheme {
        Surface {
            CategoryManagerTopBar(numberOfSelectedItems = 5)
        }
    }
}