/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.shared.extensions

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

fun ComponentActivity.registerForActivityResult(
    callback: ActivityResultCallback<ActivityResult>
): ActivityResultLauncher<Intent> {
    return this.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        callback
    )
}