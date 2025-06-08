/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 10:15 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/8/25, 8:48 PM
 */

package dev.thomas_kiljanczyk.lyriccast.domain.models

import androidx.annotation.ColorInt

data class ColorItem(val name: UiText, @ColorInt val value: Int)