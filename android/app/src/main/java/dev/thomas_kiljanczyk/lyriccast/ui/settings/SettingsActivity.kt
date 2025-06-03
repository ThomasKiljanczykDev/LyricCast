/*
 * Created by Tomasz Kiljanczyk on 6/3/25, 10:51 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/3/25, 10:51 PM
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
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            LyricCastTheme {
                val view = LocalView.current
                val statusBarColor = MaterialTheme.colorScheme.primary.toArgb()
                val isLightStatusBar = !isSystemInDarkTheme()
                if (!view.isInEditMode) {
                    SideEffect {
                        val window = (view.context as android.app.Activity).window
                        @Suppress("DEPRECATION")
                        window.statusBarColor = statusBarColor
                        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                            isLightStatusBar
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsScreen(
                        viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
                        onNavigateUp = { finish() }
                    )
                }
            }
        }
    }
}

