/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 10:15 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/8/25, 1:20 AM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.category_manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import dagger.hilt.android.AndroidEntryPoint
import dev.thomas_kiljanczyk.lyriccast.ui.shared.theme.LyricCastTheme

@AndroidEntryPoint
class CategoryManagerActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            LyricCastTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    CategoryManagerScreen(
                        onNavigateUp = { finish() }
                    )
                }
            }
        }
    }
}