/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 15/01/2025, 19:32
 */

package dev.thomas_kiljanczyk.lyriccast.application

fun AppSettings.getCastConfiguration(): CastConfiguration {
    return CastConfiguration(
        this.backgroundColor,
        this.fontColor,
        this.maxFontSize
    )
}

fun AppSettings.Builder.setValue(key: String, value: Any?): AppSettings.Builder {
    val preferenceValue: String = value?.toString() ?: ""
    if (preferenceValue.isBlank()) {
        return this
    }

    when (key) {
        "appTheme" -> {
            val appThemeValue = preferenceValue.toInt()
            this.appTheme = appThemeValue
        }

        "controlsButtonHeight" -> {
            this.controlButtonsHeight = preferenceValue.toFloat()
        }

        "blankedOnStart" -> {
            this.blankOnStart = preferenceValue.toBooleanStrict()
        }

        "backgroundColor" -> {
            this.backgroundColor = preferenceValue
        }

        "fontColor" -> {
            this.fontColor = preferenceValue
        }

        "fontMaxSize" -> {
            this.maxFontSize = preferenceValue.toInt()
        }
    }

    return this
}