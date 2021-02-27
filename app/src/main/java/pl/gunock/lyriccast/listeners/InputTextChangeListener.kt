/*
 * Created by Tomasz Kiljańczyk on 2/27/21 4:17 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 12:16 PM
 */

package pl.gunock.lyriccast.listeners

import android.text.Editable
import android.text.TextWatcher

class InputTextChangeListener(
    private val mListener: (newText: String) -> Unit
) : TextWatcher {

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable?) {
        mListener(s.toString())
    }

}