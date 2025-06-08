/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 10:15 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/8/25, 6:40 PM
 */

package dev.thomas_kiljanczyk.lyriccast.domain.use_case

interface UseCase<T, C> {
    operator fun invoke(input: T, context: C? = null): ValidationResult
}