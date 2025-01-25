/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 13/01/2025, 09:48
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.menu.cast

import android.content.Context
import android.os.Bundle
import androidx.mediarouter.app.MediaRouteControllerDialog
import androidx.mediarouter.app.MediaRouteControllerDialogFragment

class CustomMediaRouteControllerDialogFragment : MediaRouteControllerDialogFragment() {
    override fun onCreateControllerDialog(
        context: Context,
        savedInstanceState: Bundle?
    ): MediaRouteControllerDialog {
        val dialog = super.onCreateControllerDialog(context, savedInstanceState)
        dialog.isVolumeControlEnabled = false
        dialog.setTitle("")

        return dialog
    }
}