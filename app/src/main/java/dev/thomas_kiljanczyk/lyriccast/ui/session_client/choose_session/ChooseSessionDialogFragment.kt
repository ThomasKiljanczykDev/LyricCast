/*
 * Created by Tomasz Kiljanczyk on 12/01/2025, 23:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 12/01/2025, 23:55
 */

package dev.thomas_kiljanczyk.lyriccast.ui.session_client.choose_session

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.databinding.DialogFragmentChooseSessionBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


@AndroidEntryPoint
class ChooseSessionDialogFragment(
    private val onClose: () -> Unit
) : DialogFragment() {
    companion object {
        const val TAG = "PickDeviceDialogFrag"
    }

    private lateinit var viewModel: ChooseSessionDialogModel

    private lateinit var binding: DialogFragmentChooseSessionBinding

    private lateinit var recyclerViewAdapter: GmsNearbySessionItemsAdapter

    private lateinit var defaultProgressIndicatorColor: IntArray

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel = ViewModelProvider(requireActivity())[ChooseSessionDialogModel::class.java]
        viewModel.reset()

        binding = DialogFragmentChooseSessionBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(
            requireActivity(), R.style.ThemeOverlay_LyricCast_MaterialAlertDialog
        ).setTitle(R.string.dialog_fragment_choose_session_title).setCancelable(false)
            .setNegativeButton(R.string.close) { _, _ ->
                onClose()
            }.setView(binding.root).create()

        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        defaultProgressIndicatorColor = binding.pbGmsNearbyServerDevices.indicatorColor

        setupRecyclerView()

        viewModel.devices
            .debounce(500)
            .onEach {
                setSubViewVisibility(it.isNotEmpty())
                recyclerViewAdapter.submitList(it)
            }.flowOn(Dispatchers.Main).launchIn(lifecycleScope)
        viewModel.startDiscovery()

        viewModel.sessionStartError.onEach {
            if (it) {
                val errorProgressColor =
                    requireContext().getColor(R.color.error_Indeterminate_progress_bar)
                binding.pbGmsNearbyServerDevices.let { progressBar ->
                    progressBar.setIndicatorColor(errorProgressColor)
                    progressBar.isIndeterminate = false
                    progressBar.setProgress(100, true)
                }
                binding.tvLookingForSession.setText(R.string.session_client_failed_to_start_lookup)
            } else {
                binding.pbGmsNearbyServerDevices.let { progressBar ->
                    progressBar.setIndicatorColor(*defaultProgressIndicatorColor)
                    progressBar.isIndeterminate = true
                }
                binding.tvLookingForSession.setText(R.string.dialog_fragment_choose_session_looking_for_session)
            }
        }.flowOn(Dispatchers.Main).launchIn(lifecycleScope)

        return binding.root
    }

    override fun onDestroy() {
        viewModel.stopDiscovery()
        super.onDestroy()
    }

    private fun setSubViewVisibility(hasDevices: Boolean) {
        binding.pbGmsNearbyServerDevices.visibility = if (hasDevices) View.GONE else View.VISIBLE
        binding.tvLookingForSession.visibility = if (hasDevices) View.GONE else View.VISIBLE

        binding.rcvGmsNearbySessions.visibility = if (hasDevices) View.VISIBLE else View.INVISIBLE
    }

    private fun setupRecyclerView() {
        recyclerViewAdapter = GmsNearbySessionItemsAdapter(
            binding.rcvGmsNearbySessions.context
        ) { item: GmsNearbySessionItem ->
            viewModel.pickDevice(item)
            dismiss()
        }

        binding.rcvGmsNearbySessions.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = recyclerViewAdapter
        }
    }
}