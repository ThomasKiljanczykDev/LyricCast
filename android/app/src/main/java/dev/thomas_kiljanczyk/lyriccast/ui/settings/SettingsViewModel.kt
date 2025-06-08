/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 12:43 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/8/25, 12:43 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsUiState {
    val loading: Boolean get() = false

    data object Loading : SettingsUiState {
        override val loading: Boolean = true
    }

    data class Ready(
        val theme: Int = -1,
        val themeOptions: List<Pair<Int, String>> = emptyList(),
        val buttonHeight: Int = 88,
        val buttonHeightOptions: List<Pair<Int, String>> = emptyList(),
        val isBlankEnabled: Boolean = false,
        val backgroundColor: String = "",
        val fontColor: String = "",
        val colorOptions: List<String> = emptyList(),
        val maxFontSize: Int = 90
    ) : SettingsUiState
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState = settingsRepository.getAllSettings().map { settings ->
        SettingsUiState.Ready(
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
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState.Loading
    )

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
