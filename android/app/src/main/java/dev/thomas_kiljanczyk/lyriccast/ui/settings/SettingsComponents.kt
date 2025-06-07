/*
 * Created by Tomasz Kiljanczyk on 6/7/25, 7:10 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/7/25, 7:07 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.settings

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.thomas_kiljanczyk.lyriccast.ui.shared.theme.LyricCastTheme

@Composable
fun SettingsCategory(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}

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
                Text("Close")
            }
        }
    )
}

@Composable
fun <T> SettingsRowWithDialog(
    title: String,
    value: T,
    options: List<Pair<T, String>>,
    onValueChange: (T) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(
                onClick = { showDialog = true },
                indication = LocalIndication.current,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 4.dp, horizontal = 32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = options.find { it.first == value }?.second ?: value.toString(),
            style = MaterialTheme.typography.bodySmall,
        )
    }

    if (showDialog) {
        SettingsDialog(
            title = title,
            options = options,
            selectedValue = value,
            onOptionSelected = { onValueChange(it) },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun SettingsCheckbox(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onCheckedChange(!checked)
            }
            .padding(vertical = 12.dp, horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Checkbox(
            checked = checked,
            onCheckedChange = null
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSlider(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(vertical = 8.dp, horizontal = 32.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                interactionSource = interactionSource,
                modifier = Modifier.weight(1f),
                track = { sliderState ->
                    SliderDefaults.Track(
                        modifier = Modifier.height(4.dp),
                        sliderState = sliderState
                    )
                },
                thumb = {
                    SliderDefaults.Thumb(
                        modifier = Modifier.height(24.dp),
                        interactionSource = interactionSource,
                    )
                }
            )
            Text(
                text = value.toInt().toString(),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewSettingsCategory() {
    LyricCastTheme {
        Surface {
            SettingsCategory(title = "General") {
                Text(
                    "Sample content inside category",
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
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

@PreviewLightDark
@Composable
fun PreviewSettingsRowWithDialog() {
    LyricCastTheme {
        Surface {
            SettingsRowWithDialog(
                title = "Choose Option",
                value = 1,
                options = listOf(1 to "Option 1", 2 to "Option 2", 3 to "Option 3"),
                onValueChange = { }
            )
        }
    }
}


@PreviewLightDark
@Composable
fun PreviewSettingsCheckbox() {
    LyricCastTheme {
        Surface {
            SettingsCheckbox(
                title = "Enable Feature",
                checked = true,
                onCheckedChange = { }
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewSettingsSlider() {
    LyricCastTheme {
        Surface {
            SettingsSlider(
                title = "Volume",
                value = 6f,
                valueRange = 0f..10f,
                onValueChange = { }
            )
        }
    }
}
