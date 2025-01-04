/*
 * Created by Tomasz Kiljanczyk on 04/01/2025, 16:41
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 04/01/2025, 16:37
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.menu.gms_nearby_session

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.view.View
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.GmsNearbySessionServerContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

// TODO: migrate to a custom non-RestrictedApi view
@SuppressLint("RestrictedApi")
@AndroidEntryPoint
class GmsNearbyServerActionMenuItemView(context: Context?) : ActionMenuItemView(context) {
    @Inject
    lateinit var gmsNearbySessionServerContext: GmsNearbySessionServerContext

    var serverIsRunningJob: Job? = null

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        serverIsRunningJob?.cancel()
        serverIsRunningJob = null
    }


    override fun initialize(itemData: MenuItemImpl?, menuType: Int) {
        super.initialize(itemData, menuType)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val lifecycleScope = findViewTreeLifecycleOwner()?.lifecycleScope
        if (serverIsRunningJob == null && lifecycleScope != null) {
            serverIsRunningJob = gmsNearbySessionServerContext.serverIsRunning.onEach {
                val newIconResId =
                    if (it) R.drawable.menu_round_cancel_presentation_24 else R.drawable.menu_round_present_to_all_24

                setIcon(ResourcesCompat.getDrawable(context.resources, newIconResId, null))
                // TODO: localize
                itemData.title = if (it) "Stop session" else "Start session"
            }
                .onCompletion { serverIsRunningJob = null }
                .flowOn(Dispatchers.Main)
                .launchIn(lifecycleScope)
        }
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        if (gmsNearbySessionServerContext.serverIsRunning.value) {
            gmsNearbySessionServerContext.stopServer()
        } else {
            // TODO: show dialog with device name as default (changeable) session name
            val deviceName =
                Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
            try {
                // TODO: wait in the dialog until session started before closing
                gmsNearbySessionServerContext.startServer(deviceName)
            } catch (ex: SecurityException) {
                // TODO: handle exception
            }
        }
    }
}