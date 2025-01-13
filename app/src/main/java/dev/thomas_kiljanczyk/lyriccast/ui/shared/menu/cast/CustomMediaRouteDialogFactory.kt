/*
 * Created by Tomasz Kiljanczyk on 13/01/2025, 09:48
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 13/01/2025, 09:34
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.menu.cast

import androidx.mediarouter.app.MediaRouteChooserDialogFragment
import androidx.mediarouter.app.MediaRouteControllerDialogFragment
import androidx.mediarouter.app.MediaRouteDialogFactory

class CustomMediaRouteDialogFactory : MediaRouteDialogFactory() {
    override fun onCreateControllerDialogFragment(): MediaRouteControllerDialogFragment {
        return CustomMediaRouteControllerDialogFragment()
    }

    override fun onCreateChooserDialogFragment(): MediaRouteChooserDialogFragment {
        return CustomMediaRouteChooserDialogFragment()
    }
}