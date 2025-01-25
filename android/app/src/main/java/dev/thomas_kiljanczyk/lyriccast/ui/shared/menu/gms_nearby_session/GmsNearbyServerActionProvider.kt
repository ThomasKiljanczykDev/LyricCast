/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 10/01/2025, 01:46
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.menu.gms_nearby_session

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.core.view.ActionProvider

class GmsNearbyServerActionProvider(
    context: Context
) : ActionProvider(context) {
    private var menuItemView: GmsNearbyServerActionMenuItemView? = null

    @SuppressLint("RestrictedApi")
    override fun onCreateActionView(forItem: MenuItem): View {
        if (menuItemView != null) {
            return menuItemView!!
        }

        val newMenuItemView = GmsNearbyServerActionMenuItemView(context)
        menuItemView = newMenuItemView
        newMenuItemView.initialize(forItem as MenuItemImpl, 0)

        newMenuItemView.width = TypedValue.applyDimension(
            COMPLEX_UNIT_DIP, 48f, context.resources.displayMetrics
        ).toInt()
        newMenuItemView.height = TypedValue.applyDimension(
            COMPLEX_UNIT_DIP, 56f, context.resources.displayMetrics
        ).toInt()

        return newMenuItemView
    }

    override fun onCreateActionView(): View {
        return menuItemView!!
    }
}
