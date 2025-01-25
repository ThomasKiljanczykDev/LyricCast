/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.selection

import android.view.View
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

abstract class SelectionViewHolder<T>(
    itemView: View,
) : RecyclerView.ViewHolder(itemView) {

    var item: T?
        get() = _item
        private set(value) {
            _item = value
        }
    private var _item: T? = null

    fun bind(item: T) {
        this.bindAction(item)
        this.item = item
    }

    protected abstract fun bindAction(item: T)

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
        object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int = absoluteAdapterPosition
            override fun getSelectionKey(): Long = itemId
        }

}