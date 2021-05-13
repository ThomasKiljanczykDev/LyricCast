/*
 * Created by Tomasz Kiljanczyk on 14/05/2021, 00:06
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 14/05/2021, 00:06
 */

package pl.gunock.lyriccast.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import org.bson.types.ObjectId
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.common.extensions.getLifecycleOwner
import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.databinding.ItemSongBinding
import pl.gunock.lyriccast.datamodel.documents.SongDocument
import pl.gunock.lyriccast.misc.SelectionTracker
import pl.gunock.lyriccast.misc.VisibilityObserver
import pl.gunock.lyriccast.models.SongItem
import java.util.*
import kotlin.system.measureTimeMillis

class SongItemsAdapter(
    context: Context,
    val showCheckBox: MutableLiveData<Boolean> = MutableLiveData(false),
    private val mSelectionTracker: SelectionTracker<ViewHolder>?
) : RecyclerView.Adapter<SongItemsAdapter.ViewHolder>() {

    private companion object {
        val CHECKBOX_STATES = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
    }

    init {
        setHasStableIds(true)
    }

    private val mLock = Any()
    private val mLifecycleOwner: LifecycleOwner = context.getLifecycleOwner()!!

    private val mCheckBoxColor = context.getColor(R.color.check_box)
    private val mCheckBoxHighlightColor = context.getColor(R.color.check_box_highlight)
    private val mDefaultItemCardColor = context.getColor(R.color.window_background_2)
    private val mWithCategoryTextColor = context.getColor(R.color.text_item_with_category)
    private val mNoCategoryTextColor = context.getColor(R.color.text_item_no_category)
    private val mCheckBoxColors = intArrayOf(mCheckBoxHighlightColor, mCheckBoxColor)

    private var mItems: SortedSet<SongItem> = sortedSetOf()
    private var mVisibleItems: Set<SongItem> = setOf()
    val songItems: List<SongItem> get() = mVisibleItems.toList()

    fun removeObservers() {
        showCheckBox.removeObservers(mLifecycleOwner)
        mItems.forEach { it.isSelected.removeObservers(mLifecycleOwner) }
    }

    fun submitCollection(songs: Collection<SongDocument>) {
        synchronized(mLock) {
            mItems.clear()
            mItems.addAll(songs.map { SongItem(it) })
            mVisibleItems = mItems
            notifyDataSetChanged()
        }
    }

    fun filterItems(
        songTitle: String,
        categoryId: ObjectId = ObjectId(Date(0), 0),
        isSelected: Boolean? = null
    ) {
        val predicates: MutableList<(SongItem) -> Boolean> = mutableListOf()

        if (isSelected != null) {
            predicates.add { songItem -> songItem.isSelected.value!! }
        }

        if (categoryId != ObjectId(Date(0), 0)) {
            predicates.add { songItem -> songItem.song.category?.id == categoryId }
        }

        val normalizedTitle = songTitle.trim().normalize()
        predicates.add { item ->
            item.normalizedTitle.contains(normalizedTitle, ignoreCase = true)
        }

        val duration = measureTimeMillis {
            mVisibleItems = mItems.filter { songItem ->
                predicates.all { predicate -> predicate(songItem) }
            }.toSortedSet()
        }
        Log.v(SetlistItemsAdapter.TAG, "Filtering took : ${duration}ms")
        notifyDataSetChanged()
    }

    fun resetSelection() {
        mVisibleItems.forEach { it.isSelected.value = false }
        mSelectionTracker?.reset()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        return mVisibleItems.toList()[position].song.idLong
    }

    override fun getItemCount() = mVisibleItems.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mBinding = ItemSongBinding.bind(itemView)

        init {
            mBinding.tvSongCategory.setTextColor(this@SongItemsAdapter.mWithCategoryTextColor)
        }

        fun bind(position: Int) {
            val item = mVisibleItems.toList()[position]
            mSelectionTracker?.attach(this)
            showCheckBox.observe(mLifecycleOwner, VisibilityObserver(mBinding.chkItemSong))
            item.isSelected.observe(mLifecycleOwner) {
                mBinding.chkItemSong.isChecked = it
            }

            mBinding.tvItemSongTitle.text = item.song.title

            if (item.song.category != null) {
                mBinding.tvSongCategory.text = item.song.category?.name

                mBinding.chkItemSong.buttonTintList =
                    ColorStateList.valueOf(this@SongItemsAdapter.mWithCategoryTextColor)

                mBinding.tvItemSongTitle.setTextColor(this@SongItemsAdapter.mWithCategoryTextColor)
                mBinding.root.setCardBackgroundColor(item.song.category?.color!!)
            } else {
                mBinding.tvSongCategory.text = ""

                mBinding.chkItemSong.buttonTintList =
                    ColorStateList(CHECKBOX_STATES, mCheckBoxColors)

                mBinding.tvItemSongTitle.setTextColor(mNoCategoryTextColor)
                mBinding.root.setCardBackgroundColor(this@SongItemsAdapter.mDefaultItemCardColor)
            }
        }
    }
}