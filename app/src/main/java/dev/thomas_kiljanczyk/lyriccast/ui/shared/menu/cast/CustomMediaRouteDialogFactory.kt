/*
 * Created by Tomasz Kiljanczyk on 04/01/2025, 16:41
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 04/01/2025, 16:11
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.menu.cast

import androidx.mediarouter.app.MediaRouteControllerDialogFragment
import androidx.mediarouter.app.MediaRouteDialogFactory

class CustomMediaRouteDialogFactory : MediaRouteDialogFactory() {

    override fun onCreateControllerDialogFragment(): MediaRouteControllerDialogFragment {
        return CustomMediaRouteControllerDialogFragment()
    }

}