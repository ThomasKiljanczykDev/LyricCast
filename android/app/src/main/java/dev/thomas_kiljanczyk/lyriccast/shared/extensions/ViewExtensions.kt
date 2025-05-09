/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.shared.extensions

import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat

fun View.hideKeyboard() {
    val inputMethodManager: InputMethodManager? =
        ContextCompat.getSystemService(this.context, InputMethodManager::class.java)
    inputMethodManager?.hideSoftInputFromWindow(
        this.applicationWindowToken,
        InputMethodManager.HIDE_NOT_ALWAYS
    )
}