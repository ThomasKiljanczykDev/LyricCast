/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 10:15 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/8/25, 10:08 PM
 */

package dev.thomas_kiljanczyk.lyriccast.domain.use_case

import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.domain.models.UiText

data class ValidateCategoryNameContext(
    val existingNames: Set<String> = emptySet(),
)

class ValidateCategoryName : UseCase<String, ValidateCategoryNameContext> {
    companion object {
        const val MAX_LENGTH = 30
    }

    override operator fun invoke(
        input: String,
        context: ValidateCategoryNameContext?
    ): ValidationResult {
        if (input.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = UiText.StringResource(R.string.category_manager_enter_name)
            )
        }

        if (context != null && input in context.existingNames) {
            return ValidationResult(
                successful = false,
                errorMessage = UiText.StringResource(R.string.category_manager_name_already_used)
            )
        }

        if (input.length > MAX_LENGTH) {
            return ValidationResult(
                successful = false,
                errorMessage = UiText.StringResource(
                    R.string.category_manager_name_too_long,
                    MAX_LENGTH
                )
            )
        }

        return ValidationResult(
            successful = true
        )
    }
}