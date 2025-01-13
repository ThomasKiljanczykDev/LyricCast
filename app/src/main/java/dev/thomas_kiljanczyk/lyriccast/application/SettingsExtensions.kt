/*
 * Created by Tomasz Kiljanczyk on 04/01/2025, 16:41
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 03/01/2025, 00:43
 */

package dev.thomas_kiljanczyk.lyriccast.application

fun AppSettings.getCastConfiguration(): CastConfiguration {
    return CastConfiguration(
        this.backgroundColor,
        this.fontColor,
        this.maxFontSize
    )
}