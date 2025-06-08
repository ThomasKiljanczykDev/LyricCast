/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 10:15 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/8/25, 10:15 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.ui.shared.theme.LyricCastTheme

@Composable
fun LyricCastTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    maxLength: Int? = null,
    singleLine: Boolean = false,
) {
    val hasError = errorText != null

    Column(modifier = modifier) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(text = label, modifier = Modifier.fillMaxWidth())

            },
            trailingIcon = {
                if (hasError) {
                    Icon(
                        imageVector = Icons.Rounded.Error,
                        contentDescription = stringResource(R.string.navigate_up),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            isError = errorText != null,
            singleLine = singleLine,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            if (hasError) {
                Text(
                    text = errorText ?: "",
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
            if (maxLength != null) {
                Text(
                    text = "${value.length} / $maxLength",
                    color = if (hasError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
        }
    }
}


@PreviewLightDark
@Composable
fun PreviewLyricCastTextField_Default() {
    LyricCastTheme {
        Surface {
            LyricCastTextField(
                value = "",
                onValueChange = {},
                label = "Label"
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewLyricCastTextField_Error() {
    LyricCastTheme {
        Surface {
            LyricCastTextField(
                value = "Invalid input",
                onValueChange = {},
                label = "Label",
                errorText = "Error message",
                maxLength = 20
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewLyricCastTextField_MaxLength() {
    LyricCastTheme {
        Surface {
            LyricCastTextField(
                value = "12345 67890",
                onValueChange = {},
                label = "Label",
                maxLength = 20
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewLyricCastTextField_Multiline() {
    LyricCastTheme {
        Surface {
            LyricCastTextField(
                value = "Line 1\nLine 2",
                onValueChange = {},
                label = "Multiline Label"
            )
        }
    }
}
// endregion
