/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 12:43 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/7/25, 10:03 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.thomas_kiljanczyk.lyriccast.ui.shared.theme.LyricCastTheme

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LyricCastTheme {
                val statusBarColor = MaterialTheme.colorScheme.primary.toArgb()
                val isLightStatusBar = !isSystemInDarkTheme()

                val view = LocalView.current
                if (!view.isInEditMode) {
                    SideEffect {
                        @Suppress("DEPRECATION")
                        this.window.statusBarColor = statusBarColor
                        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                            isLightStatusBar
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsScreen(
                        onNavigateUp = { finish() }
                    )
                }
            }
        }
    }
}

