/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 10:15 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/8/25, 7:44 PM
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

sealed interface SettingsState {
    val loading: Boolean get() = false

    data object Loading : SettingsState {
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
    ) : SettingsState
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val state = settingsRepository.getAllSettings().map { settings ->
        SettingsState.Ready(
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
        viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState.Loading
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
