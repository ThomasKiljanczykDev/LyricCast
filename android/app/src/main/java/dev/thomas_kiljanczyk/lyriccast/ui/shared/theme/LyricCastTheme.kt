/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 12:43 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/7/25, 10:30 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val darkScheme = darkColorScheme()

private val lightScheme = lightColorScheme()

// TODO: the theme should react to changes in user settings
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LyricCastTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = when {
        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) -> {
            val context = LocalContext.current

            if (useDarkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        useDarkTheme -> darkScheme
        else -> lightScheme
    }

    MaterialExpressiveTheme(
        colorScheme = colors,
        motionScheme = MotionScheme.expressive(),
        content = content
    )
}

