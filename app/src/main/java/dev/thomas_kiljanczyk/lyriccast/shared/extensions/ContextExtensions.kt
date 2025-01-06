/*
 * Created by Tomasz Kiljanczyk on 06/01/2025, 18:29
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 06/01/2025, 17:46
 */

package dev.thomas_kiljanczyk.lyriccast.shared.extensions

import android.content.Context
import android.content.ContextWrapper
import androidx.fragment.app.FragmentActivity

tailrec fun Context.findParentFragmentActivity(): FragmentActivity? =
    this as? FragmentActivity
        ?: (this as? ContextWrapper)?.baseContext?.findParentFragmentActivity()
