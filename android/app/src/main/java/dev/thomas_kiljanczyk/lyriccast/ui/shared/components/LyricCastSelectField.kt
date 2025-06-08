/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 10:15 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/8/25, 10:15 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.thomas_kiljanczyk.lyriccast.ui.shared.theme.LyricCastTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> LyricCastSpinner(
    options: List<T>,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onOptionSelected: (T) -> Unit = {},
    optionContent: @Composable (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier
    ) {
        TextField(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            value = value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 280.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { optionContent(option) },
                    onClick = {
                        expanded = false
                        onOptionSelected(option)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewLyricCastSpinner() {
    LyricCastTheme {
        Surface(modifier = Modifier.height(500.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                LyricCastSpinner(
                    value = "Option 1",
                    options = List(10) { index ->
                        "Option ${index + 1}"
                    },
                    label = "Label",
                    optionContent = { option ->
                        Text(
                            option,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                    })
            }
        }
    }
}
