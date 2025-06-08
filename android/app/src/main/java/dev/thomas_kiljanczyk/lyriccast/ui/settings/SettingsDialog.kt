/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 10:15 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/8/25, 12:46 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.thomas_kiljanczyk.lyriccast.ui.shared.theme.LyricCastTheme

@Composable
fun <T> SettingsDialog(
    title: String,
    options: List<Pair<T, String>>,
    selectedValue: T,
    onOptionSelected: (T) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (optionValue, optionLabel) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOptionSelected(optionValue)
                                onDismiss()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = optionValue == selectedValue,
                            onClick = {
                                onOptionSelected(optionValue)
                                onDismiss()
                            }
                        )
                        Text(
                            text = optionLabel,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

@PreviewLightDark
@Composable
fun PreviewSettingsDialog() {
    LyricCastTheme {
        Surface {
            SettingsDialog(
                title = "Choose Option",
                options = listOf(1 to "Option 1", 2 to "Option 2", 3 to "Option 3"),
                selectedValue = 2,
                onOptionSelected = {},
                onDismiss = {}
            )
        }
    }
}