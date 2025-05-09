/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 25/01/2025, 18:54
 */

package dev.thomas_kiljanczyk.lyriccast.ui.song_editor

import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.databinding.ContentSongEditorBinding
import dev.thomas_kiljanczyk.lyriccast.shared.enums.NameValidationState

class SongTitleTextWatcher(
    resources: Resources,
    private val binding: ContentSongEditorBinding,
    private val viewModel: SongEditorModel
) : TextWatcher {

    private val enterTitleErrorText = resources.getString(R.string.song_editor_enter_title)

    private val titleAlreadyUsedErrorText =
        resources.getString(R.string.song_editor_title_already_used)

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable?) {
        val newText = s.toString().trim()

        when (viewModel.validateSongTitle(newText)) {
            NameValidationState.EMPTY -> {
                binding.tinSongTitle.error = enterTitleErrorText
            }

            NameValidationState.ALREADY_IN_USE -> {
                binding.tinSongTitle.error = titleAlreadyUsedErrorText
            }

            NameValidationState.VALID -> {
                binding.tinSongTitle.error = null
            }
        }
        viewModel.songTitle = newText.trim()
    }
}