/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 12/01/2025, 23:55
 */

package dev.thomas_kiljanczyk.lyriccast.shared.extensions

import android.content.Context
import android.content.ContextWrapper
import androidx.fragment.app.FragmentActivity

fun Context.findParentFragmentActivity(): FragmentActivity? = this as? FragmentActivity
    ?: (this as? ContextWrapper)?.baseContext?.findParentFragmentActivity()
