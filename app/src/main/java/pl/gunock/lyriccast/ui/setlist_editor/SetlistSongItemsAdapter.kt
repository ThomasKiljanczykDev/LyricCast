/*
 * Created by Tomasz Kiljanczyk on 03/10/2021, 11:38
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 03/10/2021, 10:52
 */

package pl.gunock.lyriccast.ui.setlist_editor

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.common.extensions.getLifecycleOwner
import pl.gunock.lyriccast.databinding.ItemSetlistSongBinding
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.listeners.TouchAdapterItemListener
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker
import pl.gunock.lyriccast.ui.shared.misc.VisibilityObserver

class SetlistSongItemsAdapter(
    context: Context,
    private val mItems: MutableList<SongItem>,
    val showCheckBox: MutableLiveData<Boolean> = MutableLiveData(false),
    private val mSelectionTracker: SelectionTracker<BaseViewHolder>?,
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
    }

    fun resetSelection() {
        mItems.forEach { it.isSelected = false }
        mSelectionTracker?.reset()
    }

    fun moveItem(from: Int, to: Int) {
        val item = mItems.removeAt(from)
        mItems.add(to, item)
        notifyItemMoved(from, to)
    }

    fun duplicateSelectedItem() {
        val selectedItemIndex = mItems.indexOfFirst { item -> item.isSelected }
        val selectedItem = mItems[selectedItemIndex].copy()
        selectedItem.id = availableId++

        selectedItem.isSelected = false

        mItems.add(selectedItemIndex + 1, selectedItem)
        notifyItemInserted(selectedItemIndex + 1)
    }

    fun removeSelectedItems() {
        @Suppress("ControlFlowWithEmptyBody")
        while (deleteSelectedItem()) {
        }
    }

    private fun deleteSelectedItem(): Boolean {
        val selectedItemIndex = mItems.indexOfFirst { item -> item.isSelected }
        if (selectedItemIndex == -1) {
            return false
        }
        mItems.removeAt(selectedItemIndex)

        notifyItemRemoved(selectedItemIndex)
        return true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemSetlistSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
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
        private val mBinding: ItemSetlistSongBinding
    ) : BaseViewHolder(mBinding.root, mSelectionTracker) {

        override fun setUpViewHolder(position: Int) {
            val item = mItems[position]
            setupListeners()

            mBinding.chkItemSong.isChecked = item.isSelected

            showCheckBox.observe(mLifecycleOwner, VisibilityObserver(mBinding.imvHandle, true))
            showCheckBox.observe(mLifecycleOwner, VisibilityObserver(mBinding.chkItemSong))

            mBinding.tvItemSongTitle.text = item.song.title
        }

        private fun setupListeners() {
            @SuppressLint("ClickableViewAccessibility")
            if (mOnHandleTouchListener != null) {
                mBinding.imvHandle.setOnTouchListener { view, event ->
                    mOnHandleTouchListener.execute(this, view, event)
                }
            }
        }
    }
}