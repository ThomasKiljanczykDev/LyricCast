/*
 * Created by Tomasz Kiljanczyk on 4/20/21 1:10 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/19/21 11:20 PM
 */

package pl.gunock.lyriccast.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.common.extensions.getLifecycleOwner
import pl.gunock.lyriccast.listeners.TouchAdapterItemListener
import pl.gunock.lyriccast.misc.SelectionTracker
import pl.gunock.lyriccast.misc.VisibilityObserver
import pl.gunock.lyriccast.models.SongItem

class SetlistSongItemsAdapter(
    context: Context,
    private val mItems: MutableList<SongItem>,
    val showCheckBox: MutableLiveData<Boolean> = MutableLiveData(false),
    private val mSelectionTracker: SelectionTracker<ViewHolder>?,
    private val mOnHandleTouchListener: TouchAdapterItemListener<ViewHolder>? = null
) : RecyclerView.Adapter<SetlistSongItemsAdapter.ViewHolder>() {

    val items: List<SongItem> get() = mItems

    private val mLifecycleOwner: LifecycleOwner = context.getLifecycleOwner()!!
    private var availableId: Long = 0L

    init {
        mItems.forEach { it.id = availableId++ }

        setHasStableIds(true)
    }

    fun removeObservers() {
        showCheckBox.removeObservers(mLifecycleOwner)
        items.forEach { it.isSelected.removeObservers(mLifecycleOwner) }
    }

    fun resetSelection() {
        mItems.forEach { it.isSelected.value = false }
        mSelectionTracker?.reset()
    }

    fun moveItem(from: Int, to: Int) {
        val item = mItems.removeAt(from)
        mItems.add(to, item)
    }

    fun duplicateSelectedItem() {
        val selectedItemIndex = mItems.indexOfFirst { item -> item.isSelected.value!! }
        val selectedItem = mItems[selectedItemIndex].copy()
        selectedItem.id = availableId++

        selectedItem.isSelected.value = false

        mItems.add(selectedItemIndex + 1, selectedItem)
        notifyItemInserted(selectedItemIndex + 1)
    }

    fun removeSelectedItems() {
        @Suppress("ControlFlowWithEmptyBody")
        while (deleteSelectedItem()) {
        }
    }

    private fun deleteSelectedItem(): Boolean {
        val selectedItemIndex = mItems.indexOfFirst { item -> item.isSelected.value!! }
        if (selectedItemIndex == -1) {
            return false
        }
        mItems.removeAt(selectedItemIndex)
        notifyItemRemoved(selectedItemIndex)
        return true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_setlist_song, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        val item = mItems[position]
        return item.id
    }

    override fun getItemCount() = mItems.size

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val mCheckBox: CheckBox = itemView.findViewById(R.id.chk_item_song)
        private val mHandleView: View = itemView.findViewById(R.id.imv_handle)
        private val mTitleTextView: TextView = itemView.findViewById(R.id.tv_item_song_title)

        fun bind(position: Int) {
            val item = mItems[position]
            mSelectionTracker?.attach(this)
            setupListeners()

            item.isSelected.observe(mLifecycleOwner) {
                mCheckBox.isChecked = it
            }

            showCheckBox
                .observe(mLifecycleOwner, VisibilityObserver(mHandleView, true))
            showCheckBox.observe(mLifecycleOwner, VisibilityObserver(mCheckBox))

            mTitleTextView.text = item.song.title
        }

        private fun setupListeners() {
            @SuppressLint("ClickableViewAccessibility")
            if (mOnHandleTouchListener != null) {
                mHandleView.setOnTouchListener { view, event ->
                    mOnHandleTouchListener.execute(this, view, event)
                }
            }
        }
    }
}