/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 10:15 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/8/25, 9:31 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.misc

import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.domain.models.ColorItem
import dev.thomas_kiljanczyk.lyriccast.domain.models.UiText


val colorItems = listOf(
    ColorItem(
        UiText.StringResource(R.string.category_color_maroon),
        BaseColors.Maroon
    ),
    ColorItem(
        UiText.StringResource(R.string.category_color_tomato),
        BaseColors.Tomato
    ),
    ColorItem(
        UiText.StringResource(R.string.category_color_golden),
        BaseColors.GoldenRod
    ),
    ColorItem(
        UiText.StringResource(R.string.category_color_olive),
        BaseColors.OliveDrab
    ),
    ColorItem(
        UiText.StringResource(R.string.category_color_cyan),
        BaseColors.DarkCyan
    ),
    ColorItem(
        UiText.StringResource(R.string.category_color_turquoise),
        BaseColors.Turquoise
    ),
    ColorItem(
        UiText.StringResource(R.string.category_color_royal_blue),
        BaseColors.RoyalBlue
    ),
    ColorItem(
        UiText.StringResource(R.string.category_color_violet),
        BaseColors.BlueViolet
    ),
    ColorItem(
        UiText.StringResource(R.string.category_color_pink),
        BaseColors.DeepPink
    ),
    ColorItem(
        UiText.StringResource(R.string.category_color_navajo_white),
        BaseColors.NavajoWhite
    ),
    ColorItem(
        UiText.StringResource(R.string.category_color_slate_gray),
        BaseColors.SlateGray
    ),
    ColorItem(
        UiText.StringResource(R.string.category_color_gray),
        BaseColors.Gray
    ),
    ColorItem(
        UiText.StringResource(R.string.category_color_steel_blue),
        BaseColors.LightSteelBlue
    )
)
