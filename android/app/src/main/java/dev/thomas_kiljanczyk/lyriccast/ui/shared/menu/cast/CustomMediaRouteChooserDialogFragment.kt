/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 13/01/2025, 09:48
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.menu.cast

import android.content.Context
import android.os.Bundle
import androidx.mediarouter.app.MediaRouteChooserDialog
import androidx.mediarouter.app.MediaRouteChooserDialogFragment
import dev.thomas_kiljanczyk.lyriccast.R

class CustomMediaRouteChooserDialogFragment : MediaRouteChooserDialogFragment() {
    override fun onCreateChooserDialog(
        context: Context,
        savedInstanceState: Bundle?
    ): MediaRouteChooserDialog {
        val dialog = super.onCreateChooserDialog(context, savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(R.drawable.media_route_dialog_background)

        return dialog
    }
}