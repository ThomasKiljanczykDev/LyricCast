/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 12:43 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/8/25, 1:58 AM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.ui.shared.components.Loading
import dev.thomas_kiljanczyk.lyriccast.ui.shared.theme.LyricCastTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(onNavigateUp: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(R.string.settings_title)) },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_up)
                )
            }
        }
    )
}

@PreviewLightDark
@Composable
fun PreviewSettingsTopBar() {
    LyricCastTheme {
        SettingsTopBar(onNavigateUp = {})
    }
}

@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showLoading = remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState !is SettingsUiState.Loading) {
            showLoading.value = false
            return@LaunchedEffect
        }

        showLoading.value = false
        delay(500)
        showLoading.value = true
    }

    // TODO: replace loading animation with holding on previous view until loaded
    Crossfade(uiState.loading) {
        if (it || uiState is SettingsUiState.Loading) {
            AnimatedVisibility(
                visible = showLoading.value,
                enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
                exit = fadeOut()
            ) {
                Loading(Modifier.fillMaxSize())
            }
        } else if (uiState is SettingsUiState.Ready) {
            SettingsScreen(
                uiState = uiState as SettingsUiState.Ready,
                onNavigateUp = onNavigateUp,
                onThemeChange = { viewModel.updateTheme(it) },
                onButtonHeightChange = { viewModel.updateButtonHeight(it) },
                onBlankEnabledChange = { viewModel.updateBlankEnabled(it) },
                onBackgroundColorChange = { viewModel.updateBackgroundColor(it) },
                onFontColorChange = { viewModel.updateFontColor(it) },
                onMaxFontSizeChange = { viewModel.updateMaxFontSize(it) }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState.Ready,
    onNavigateUp: () -> Unit,
    onThemeChange: (Int) -> Unit,
    onButtonHeightChange: (Int) -> Unit,
    onBlankEnabledChange: (Boolean) -> Unit,
    onBackgroundColorChange: (String) -> Unit,
    onFontColorChange: (String) -> Unit,
    onMaxFontSizeChange: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            SettingsTopBar(onNavigateUp = onNavigateUp)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // App Settings Section
            SettingsCategory(title = stringResource(R.string.preference_section_app)) {
                SettingsRowWithDialog(
                    title = stringResource(R.string.preference_theme_title),
                    value = uiState.theme,
                    options = uiState.themeOptions,
                    onValueChange = onThemeChange
                )

                SettingsRowWithDialog(
                    title = stringResource(R.string.preference_controls_button_height_title),
                    value = uiState.buttonHeight,
                    options = uiState.buttonHeightOptions,
                    onValueChange = onButtonHeightChange
                )
            }

            HorizontalDivider(thickness = 1.dp)

            // Chromecast Settings Section
            SettingsCategory(title = stringResource(R.string.preference_section_chromecast)) {
                SettingsCheckbox(
                    title = stringResource(R.string.preference_blank_title),
                    checked = uiState.isBlankEnabled,
                    onCheckedChange = onBlankEnabledChange
                )

                SettingsRowWithDialog(
                    title = stringResource(R.string.preference_cast_background_title),
                    value = uiState.backgroundColor,
                    options = uiState.colorOptions.map { it to it },
                    onValueChange = onBackgroundColorChange
                )

                SettingsRowWithDialog(
                    title = stringResource(R.string.preference_cast_font_color_title),
                    value = uiState.fontColor,
                    options = uiState.colorOptions.map { it to it },
                    onValueChange = onFontColorChange
                )

                SettingsSlider(
                    title = stringResource(R.string.preference_cast_max_font_size_title),
                    value = uiState.maxFontSize.toFloat(),
                    valueRange = 30f..100f,
                    onValueChange = { onMaxFontSizeChange(it.toInt()) }
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewSettingsScreen() {
    val previewUiState = SettingsUiState.Ready(
        theme = -1,
        themeOptions = listOf(-1 to "System default", 1 to "Light", 2 to "Dark"),
        buttonHeight = 88,
        buttonHeightOptions = listOf(88 to "Small", 104 to "Medium", 128 to "Large"),
        isBlankEnabled = true,
        backgroundColor = "Black",
        fontColor = "White",
        colorOptions = listOf("Maroon", "Tomato", "Black", "White", "Gray"),
        maxFontSize = 90
    )

    LyricCastTheme {
        SettingsScreen(
            uiState = previewUiState,
            onNavigateUp = {},
            onThemeChange = {},
            onButtonHeightChange = {},
            onBlankEnabledChange = {},
            onBackgroundColorChange = {},
            onFontColorChange = {},
            onMaxFontSizeChange = {}
        )
    }
}