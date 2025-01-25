/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.common.extensions

import org.apache.commons.lang3.StringUtils

fun String.normalize(): String {
    return StringUtils.stripAccents(this)
}
