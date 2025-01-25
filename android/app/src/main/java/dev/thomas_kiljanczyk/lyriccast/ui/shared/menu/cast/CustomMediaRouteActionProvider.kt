/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 10/01/2025, 01:46
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.menu.cast

import android.content.Context
import android.view.View
import androidx.mediarouter.app.MediaRouteActionProvider
import androidx.mediarouter.app.MediaRouteButton
import androidx.mediarouter.app.MediaRouteDialogFactory

class CustomMediaRouteActionProvider(context: Context) : MediaRouteActionProvider(context) {

    private var factory: MediaRouteDialogFactory = CustomMediaRouteDialogFactory()

    override fun onCreateActionView(): View {
        val castButton = super.onCreateActionView() as MediaRouteButton
        castButton.dialogFactory = dialogFactory
        return castButton
    }

    override fun getDialogFactory(): MediaRouteDialogFactory {
        return factory
    }

    override fun setDialogFactory(factory: MediaRouteDialogFactory) {
        this.factory = factory
    }

}