/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 10/01/2025, 01:46
 */

package dev.thomas_kiljanczyk.lyriccast.ui.session_client.choose_session

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.thomas_kiljanczyk.lyriccast.databinding.ItemSessionBinding

class GmsNearbySessionItemsAdapter(
    context: Context,
    private val onItemClick: (item: GmsNearbySessionItem) -> Unit,
) : ListAdapter<GmsNearbySessionItem, GmsNearbySessionItemsAdapter.ViewHolder>(
    DiffCallback()
) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemSessionBinding.inflate(inflater, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        return -1
    }

    override fun getItemCount() = currentList.size

    inner class ViewHolder(
        private val binding: ItemSessionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val item: GmsNearbySessionItem = currentList[position]

            binding.tvItemDeviceName.text = item.deviceName
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<GmsNearbySessionItem>() {
        override fun areItemsTheSame(
            oldItem: GmsNearbySessionItem,
            newItem: GmsNearbySessionItem
        ): Boolean =
            oldItem.endpointId == newItem.endpointId

        override fun areContentsTheSame(
            oldItem: GmsNearbySessionItem,
            newItem: GmsNearbySessionItem
        ): Boolean =
            oldItem == newItem
    }
}