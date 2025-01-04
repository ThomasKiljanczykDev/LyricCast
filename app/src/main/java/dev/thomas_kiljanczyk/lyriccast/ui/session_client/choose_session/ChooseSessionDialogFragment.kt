/*
 * Created by Tomasz Kiljanczyk on 04/01/2025, 16:41
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 04/01/2025, 16:11
 */

package dev.thomas_kiljanczyk.lyriccast.ui.session_client.choose_session

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Strategy
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.databinding.DialogFragmentChooseSessionBinding
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.GmsNearbyConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ChooseSessionDialogFragment(
    private val onClose: () -> Unit
) : DialogFragment() {
    companion object {
        const val TAG = "PickDeviceDialogFrag"
    }

    private lateinit var viewModel: ChooseSessionDialogViewModel

    private lateinit var binding: DialogFragmentChooseSessionBinding

    private lateinit var recyclerViewAdapter: GmsNearbySessionItemsAdapter

    @Inject
    lateinit var connectionsClient: ConnectionsClient

    private val deviceMap = mutableMapOf<String, GmsNearbySessionItem>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel =
            ViewModelProvider(requireActivity())[ChooseSessionDialogViewModel::class.java]

        binding = DialogFragmentChooseSessionBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(
            requireActivity(),
            R.style.ThemeOverlay_LyricCast_MaterialAlertDialog
        )
            .setTitle(R.string.dialog_fragment_choose_session_title)
            .setCancelable(false)
            .setNegativeButton(R.string.dialog_fragment_close) { _, _ ->
                onClose()
            }
            .setView(binding.root)
            .create()

        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupRecyclerView()

        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        connectionsClient
            .startDiscovery(
                GmsNearbyConstants.SERVICE_UUID.toString(),
                object : EndpointDiscoveryCallback() {
                    override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                        val deviceItem = GmsNearbySessionItem(info.endpointName, endpointId)
                        deviceMap[endpointId] = deviceItem

                        val devices = deviceMap.values.toList()
                        recyclerViewAdapter.submitList(devices)

                        CoroutineScope(Dispatchers.Main).launch {
                            binding.pbGmsNearbyServerDevices.visibility =
                                if (devices.isNotEmpty()) View.GONE else View.VISIBLE
                        }
                    }

                    override fun onEndpointLost(endpointId: String) {
                        deviceMap.remove(endpointId)

                        val devices = deviceMap.values.toList()
                        recyclerViewAdapter.submitList(devices)

                        CoroutineScope(Dispatchers.Main).launch {
                            binding.pbGmsNearbyServerDevices.visibility =
                                if (devices.isNotEmpty()) View.GONE else View.VISIBLE
                        }
                    }

                },
                discoveryOptions
            )
            .addOnFailureListener { e ->
                // We're unable to start discovering.
                Log.e(TAG, "Failed to start discovering", e)
                // TODO: show a explanatory message to the user
            }

        return binding.root
    }

    override fun onDestroy() {
        connectionsClient.stopDiscovery()
        super.onDestroy()
    }

    private fun setupRecyclerView() {
        recyclerViewAdapter = GmsNearbySessionItemsAdapter(
            binding.rcvGmsNearbyServerDevices.context
        ) { item: GmsNearbySessionItem ->
            viewModel.pickDevice(item)
            dismiss()
        }

        binding.rcvGmsNearbyServerDevices.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = recyclerViewAdapter
        }
    }
}