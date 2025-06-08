/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 12:43 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/7/25, 10:19 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun Loading(modifier: Modifier = Modifier) {
    Surface(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            CircularProgressIndicator(
                Modifier.align(Alignment.Center),
            )
        }
    }
}
