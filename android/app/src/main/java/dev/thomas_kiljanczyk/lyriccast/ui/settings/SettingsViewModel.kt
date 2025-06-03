/*
 * Created by Tomasz Kiljanczyk on 6/3/25, 10:51 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/3/25, 10:51 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO: migrate default into a single static object
data class SettingsUiState(
    val theme: Int = -1,
    val themeOptions: List<Pair<Int, String>> = emptyList(),
    val buttonHeight: Int = 88,
    val buttonHeightOptions: List<Pair<Int, String>> = emptyList(),
    val isBlankEnabled: Boolean = false,
    val backgroundColor: String = "",
    val fontColor: String = "",
    val colorOptions: List<String> = emptyList(),
    val maxFontSize: Int = 90
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getAllSettings()
                .map { settings ->
                    SettingsUiState(
                        theme = settings.appTheme,
                        themeOptions = settingsRepository.getThemeOptions(),
                        buttonHeight = settings.controlButtonsHeight.toInt(),
                        buttonHeightOptions = settingsRepository.getButtonHeightOptions(),
                        isBlankEnabled = settings.blankOnStart,
                        backgroundColor = settings.backgroundColor,
                        fontColor = settings.fontColor,
                        colorOptions = settingsRepository.getColorOptions(),
                        maxFontSize = settings.maxFontSize
                    )
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun updateTheme(theme: Int) {
        viewModelScope.launch {
            settingsRepository.updateTheme(theme)
        }
    }

    fun updateButtonHeight(height: Int) {
        viewModelScope.launch {
            settingsRepository.updateButtonHeight(height.toFloat())
        }
    }

    fun updateBlankEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateBlankEnabled(enabled)
        }
    }

    fun updateBackgroundColor(color: String) {
        viewModelScope.launch {
            settingsRepository.updateBackgroundColor(color)
        }
    }

    fun updateFontColor(color: String) {
        viewModelScope.launch {
            settingsRepository.updateFontColor(color)
        }
    }

    fun updateMaxFontSize(size: Int) {
        viewModelScope.launch {
            settingsRepository.updateMaxFontSize(size)
        }
    }
}
