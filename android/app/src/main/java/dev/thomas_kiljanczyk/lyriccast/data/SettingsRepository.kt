/*
 * Created by Tomasz Kiljanczyk on 6/3/25, 10:51 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/3/25, 9:37 PM
 */

package dev.thomas_kiljanczyk.lyriccast.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.application.AppSettings
import dev.thomas_kiljanczyk.lyriccast.application.settingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getTheme(): Flow<Int> = context.settingsDataStore.data
        .map { settings ->
            settings.appTheme
        }

    fun getButtonHeight(): Flow<Float> = context.settingsDataStore.data
        .map { settings ->
            settings.controlButtonsHeight
        }

    fun getBlankEnabled(): Flow<Boolean> = context.settingsDataStore.data
        .map { settings ->
            settings.blankOnStart
        }

    fun getBackgroundColor(): Flow<String> = context.settingsDataStore.data
        .map { settings ->
            settings.backgroundColor
        }

    fun getFontColor(): Flow<String> = context.settingsDataStore.data
        .map { settings ->
            settings.fontColor
        }

    fun getMaxFontSize(): Flow<Int> = context.settingsDataStore.data
        .map { settings ->
            settings.maxFontSize
        }

    fun getAllSettings(): Flow<AppSettings> = context.settingsDataStore.data

    suspend fun updateTheme(theme: Int) {
        context.settingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setAppTheme(theme)
                .build()
        }
    }

    suspend fun updateButtonHeight(height: Float) {
        context.settingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setControlButtonsHeight(height)
                .build()
        }
    }

    suspend fun updateBlankEnabled(enabled: Boolean) {
        context.settingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setBlankOnStart(enabled)
                .build()
        }
    }

    suspend fun updateBackgroundColor(color: String) {
        context.settingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setBackgroundColor(color)
                .build()
        }
    }

    suspend fun updateFontColor(color: String) {
        context.settingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setFontColor(color)
                .build()
        }
    }

    suspend fun updateMaxFontSize(size: Int) {
        context.settingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setMaxFontSize(size)
                .build()
        }
    }

    fun getThemeOptions(): List<Pair<Int, String>> {
        val entries = context.resources.getStringArray(R.array.theme_entries)
        val values = context.resources.getStringArray(R.array.theme_values).map { it.toInt() }
        return values.zip(entries).toList()
    }

    fun getButtonHeightOptions(): List<Pair<Int, String>> {
        val entries = context.resources.getStringArray(R.array.control_buttons_height_entries)
        val values = context.resources.getStringArray(R.array.control_buttons_height_values)
            .map { it.toInt() }
        return values.zip(entries).toList()
    }

    fun getColorOptions(): List<String> =
        context.resources.getStringArray(R.array.color_entries).toList()
}

