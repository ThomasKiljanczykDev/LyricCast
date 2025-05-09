/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.ui.category_manager

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.thomas_kiljanczyk.lyriccast.databinding.ItemCategoryBinding
import dev.thomas_kiljanczyk.lyriccast.domain.models.CategoryItem
import dev.thomas_kiljanczyk.lyriccast.ui.shared.selection.SelectionViewHolder

class CategoryItemsAdapter(
    context: Context,
) : ListAdapter<CategoryItem, CategoryItemsAdapter.ViewHolder>(DiffCallback()) {

    private companion object {
        const val TAG = "CategoryItemsAdapter"
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: CategoryItem = try {
            currentList[position]
        } catch (e: IndexOutOfBoundsException) {
            Log.w(TAG, e)
            return
        }

        holder.bind(item)
    }

    override fun getItemId(position: Int): Long {
        if (currentList.isEmpty()) {
            return RecyclerView.NO_ID
        }

        return currentList[position].category.idLong
    }

    inner class ViewHolder(
        private val binding: ItemCategoryBinding
    ) : SelectionViewHolder<CategoryItem>(binding.root) {

        override fun bindAction(item: CategoryItem) {
            if (item.hasCheckbox) {
                binding.chkItemCategory.visibility = View.VISIBLE
                binding.chkItemCategory.isChecked = item.isSelected
            } else {
                binding.chkItemCategory.visibility = View.GONE
                binding.chkItemCategory.isChecked = false
            }

            if (item.category != this.item?.category) {
                binding.tvCategoryName.text = currentList[absoluteAdapterPosition].category.name

                if (item.category.color != null) {
                    binding.cdvCategoryColor.setCardBackgroundColor(item.category.color!!)
                }
            }
        }
    }


    private class DiffCallback : DiffUtil.ItemCallback<CategoryItem>() {
        override fun areItemsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean =
            oldItem.category.id == newItem.category.id

        override fun areContentsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean =
            oldItem == newItem
    }

}