/*
 * Created by Tomasz Kiljanczyk on 07/01/2025, 20:26
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 07/01/2025, 20:25
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.menu.gms_nearby_session

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.shared.extensions.findParentFragmentActivity
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.GmsNearbySessionServerContext
import dev.thomas_kiljanczyk.lyriccast.ui.shared.menu.gms_nearby_session.dialog.StartSessionServerDialogFragment
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

    private var serverIsRunningJob: Job? = null

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

                setIcon(ResourcesCompat.getDrawable(resources, newIconResId, null))

                val newTitleResId =
                    if (it) R.string.option_menu_stop_session else R.string.option_menu_start_session
                itemData.title = resources.getString(newTitleResId)
            }
                .onCompletion { serverIsRunningJob = null }
                .flowOn(Dispatchers.Main)
                .launchIn(lifecycleScope)
        }
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        if (gmsNearbySessionServerContext.serverIsRunning.value) {
            // TODO: show dialog with connected devices and ask if user wants to stop the session
            gmsNearbySessionServerContext.stopServer()
        } else {
            val activity = context.findParentFragmentActivity()
            if (activity != null) {
                StartSessionServerDialogFragment().show(
                    activity.supportFragmentManager,
                    StartSessionServerDialogFragment.TAG
                )
            }
        }
    }
}