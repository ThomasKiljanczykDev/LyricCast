/*
 * Created by Tomasz Kiljanczyk on 05/01/2025, 21:30
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 05/01/2025, 21:03
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
        val dialog = MediaRouteChooserDialog(context)
        dialog.window?.setBackgroundDrawableResource(R.drawable.media_route_chooser_dialog_background)

        return dialog
    }
}