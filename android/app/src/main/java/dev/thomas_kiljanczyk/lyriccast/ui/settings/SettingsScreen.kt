/*
 * Created by Tomasz Kiljanczyk on 6/3/25, 10:51 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/3/25, 10:51 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.ui.components.SettingsCategory
import dev.thomas_kiljanczyk.lyriccast.ui.components.SettingsCheckbox
import dev.thomas_kiljanczyk.lyriccast.ui.components.SettingsRowWithDialog
import dev.thomas_kiljanczyk.lyriccast.ui.components.SettingsSlider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // App Settings Section
            SettingsCategory(title = stringResource(R.string.preference_section_app)) {
                SettingsRowWithDialog(
                    title = stringResource(R.string.preference_theme_title),
                    value = uiState.theme,
                    options = uiState.themeOptions,
                    onValueChange = { selectedValue ->
                        viewModel.updateTheme(selectedValue)
                    }
                )

                SettingsRowWithDialog(
                    title = stringResource(R.string.preference_controls_button_height_title),
                    value = uiState.buttonHeight.toInt(),
                    options = uiState.buttonHeightOptions,
                    onValueChange = { selectedValue ->
                        viewModel.updateButtonHeight(selectedValue)
                    }
                )
            }

            // Chromecast Settings Section
            SettingsCategory(title = stringResource(R.string.preference_section_chromecast)) {
                SettingsCheckbox(
                    title = stringResource(R.string.preference_blank_title),
                    checked = uiState.isBlankEnabled,
                    onCheckedChange = { viewModel.updateBlankEnabled(it) }
                )

                SettingsRowWithDialog(
                    title = stringResource(R.string.preference_cast_background_title),
                    value = uiState.backgroundColor,
                    options = uiState.colorOptions.map { it to it },
                    onValueChange = { viewModel.updateBackgroundColor(it) }
                )

                SettingsRowWithDialog(
                    title = stringResource(R.string.preference_cast_font_color_title),
                    value = uiState.fontColor,
                    options = uiState.colorOptions.map { it to it },
                    onValueChange = { viewModel.updateFontColor(it) }
                )

                SettingsSlider(
                    title = stringResource(R.string.preference_cast_max_font_size_title),
                    value = uiState.maxFontSize.toFloat(),
                    valueRange = 30f..100f,
                    onValueChange = { viewModel.updateMaxFontSize(it.toInt()) }
                )
            }
        }
    }
}

