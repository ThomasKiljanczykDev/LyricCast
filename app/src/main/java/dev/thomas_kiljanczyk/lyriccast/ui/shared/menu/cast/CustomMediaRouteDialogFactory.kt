/*
 * Created by Tomasz Kiljanczyk on 05/01/2025, 21:30
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 05/01/2025, 21:30
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.menu.cast

import androidx.mediarouter.app.MediaRouteChooserDialogFragment
import androidx.mediarouter.app.MediaRouteDialogFactory

class CustomMediaRouteDialogFactory : MediaRouteDialogFactory() {
    override fun onCreateChooserDialogFragment(): MediaRouteChooserDialogFragment {
        return CustomMediaRouteChooserDialogFragment()
    }
}