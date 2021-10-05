/*
 * Created by Tomasz Kiljanczyk on 05/10/2021, 10:03
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 05/10/2021, 09:02
 */

package pl.gunock.lyriccast.ui.category_manager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.gunock.lyriccast.databinding.ItemCategoryBinding
import pl.gunock.lyriccast.domain.models.CategoryItem
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker

class CategoryItemsAdapter(
    private val selectionTracker: SelectionTracker<BaseViewHolder>?
) : RecyclerView.Adapter<CategoryItemsAdapter.ViewHolder>() {

    private var _items: MutableList<CategoryItem> = mutableListOf()

    init {
        setHasStableIds(true)
    }

    suspend fun submitCollection(categories: List<CategoryItem>) {
        withContext(Dispatchers.Main) {
            notifyItemRangeRemoved(0, _items.size)
        }
        withContext(Dispatchers.Default) {
            _items.clear()
            _items.addAll(categories)
        }
        withContext(Dispatchers.Main) {
            notifyItemRangeInserted(0, _items.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        if (_items.isEmpty()) {
            return -1L
        }

        return _items[position].category.idLong
    }

    override fun getItemCount() = _items.size

    inner class ViewHolder(
        private val binding: ItemCategoryBinding
    ) : BaseViewHolder(binding.root, selectionTracker) {
        override fun setUpViewHolder(position: Int) {
            val item: CategoryItem = _items[position]

            if (item.hasCheckbox) {
                binding.chkItemCategory.visibility = View.VISIBLE
                binding.chkItemCategory.isChecked = item.isSelected
            } else {
                binding.chkItemCategory.visibility = View.GONE
            }

            binding.tvCategoryName.text = _items[absoluteAdapterPosition].category.name

            if (item.category.color != null) {
                binding.cdvCategoryColor.setCardBackgroundColor(item.category.color!!)
            }
        }
    }

}