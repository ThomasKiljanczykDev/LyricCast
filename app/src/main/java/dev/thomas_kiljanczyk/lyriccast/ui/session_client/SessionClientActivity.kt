/*
 * Created by Tomasz Kiljanczyk on 04/01/2025, 16:41
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 04/01/2025, 16:11
 */

package dev.thomas_kiljanczyk.lyriccast.ui.session_client

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.thomas_kiljanczyk.lyriccast.databinding.ActivitySessionClientBinding
import dev.thomas_kiljanczyk.lyriccast.databinding.ContentSessionClientBinding
import dev.thomas_kiljanczyk.lyriccast.ui.session_client.choose_session.ChooseSessionDialogFragment
import dev.thomas_kiljanczyk.lyriccast.ui.session_client.choose_session.ChooseSessionDialogViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class SessionClientActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "SessionClientActivity"
    }

    private val viewModel: SessionClientModel by viewModels()

    private lateinit var binding: ContentSessionClientBinding

    private lateinit var pickDeviceDialogViewModel: ChooseSessionDialogViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val rootBinding = ActivitySessionClientBinding.inflate(layoutInflater)
        setContentView(rootBinding.root)
        setSupportActionBar(rootBinding.toolbarControls)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        pickDeviceDialogViewModel =
            ViewModelProvider(this)[ChooseSessionDialogViewModel::class.java]

        binding = ContentSessionClientBinding.bind(rootBinding.contentSessionClient.root)

        viewModel.songTitle
            .onEach { binding.tvControlsSongTitle.text = it }
            .flowOn(Dispatchers.Main)
            .launchIn(lifecycleScope)

        viewModel.currentSlideText
            .onEach { binding.tvSlidePreview.text = it }
            .flowOn(Dispatchers.Main)
            .launchIn(lifecycleScope)

        viewModel.currentSlideNumber
            .onEach { binding.tvSongSlideNumber.text = it }
            .flowOn(Dispatchers.Main)
            .launchIn(lifecycleScope)

        setupObservers()

        setOnApplyWindowInsetsListener(rootBinding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            rootBinding.toolbarControls.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
                bottomMargin = 0
            }

            binding.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom
            }

            WindowInsetsCompat.CONSUMED
        }

        ChooseSessionDialogFragment(
            onClose = { finish() }
        ).show(supportFragmentManager, ChooseSessionDialogFragment.TAG)
    }

    private fun setupObservers() {
        pickDeviceDialogViewModel.serverEndpointId
            .onEach(this::observeDialogBluetoothDevice)
            .launchIn(lifecycleScope)

        pickDeviceDialogViewModel.message
            .onEach {
                if (it.isNotBlank()) {
                    Toast.makeText(baseContext, it, Toast.LENGTH_SHORT).show()
                }
            }.launchIn(lifecycleScope)
    }

    override fun onResume() {
        super.onResume()

        // TODO: poll server for slide updates
        // TODO: exit activity if server disconnected
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopClient()
    }

    private fun observeDialogBluetoothDevice(endpointId: String?) {
        try {
            if (endpointId != null) {
                val deviceName =
                    Settings.Global.getString(contentResolver, Settings.Global.DEVICE_NAME)
                viewModel.startClient(endpointId, deviceName)
            }
        } catch (ex: SecurityException) {
            Log.e(TAG, "Failed to start client", ex)
            finish()
            return
        }
    }

}