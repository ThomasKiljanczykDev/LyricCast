/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.listeners

import android.view.View
import androidx.recyclerview.widget.RecyclerView

fun interface ClickAdapterItemListener<T> where T : RecyclerView.ViewHolder {

    fun execute(holder: T, position: Int, view: View)

}