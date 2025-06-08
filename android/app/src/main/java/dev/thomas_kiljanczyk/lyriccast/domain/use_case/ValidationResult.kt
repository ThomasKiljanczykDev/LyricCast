/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 10:15 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/8/25, 6:35 PM
 */

package dev.thomas_kiljanczyk.lyriccast.domain.use_case

import dev.thomas_kiljanczyk.lyriccast.domain.models.UiText

data class ValidationResult(
    val successful: Boolean,
    val errorMessage: UiText? = null
)