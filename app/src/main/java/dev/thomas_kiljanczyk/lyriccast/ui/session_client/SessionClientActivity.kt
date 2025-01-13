/*
 * Created by Tomasz Kiljanczyk on 12/01/2025, 23:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 12/01/2025, 23:25
 */

package dev.thomas_kiljanczyk.lyriccast.ui.session_client

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.application.LyricCastApplication
import dev.thomas_kiljanczyk.lyriccast.databinding.ActivitySessionClientBinding
import dev.thomas_kiljanczyk.lyriccast.databinding.ContentSessionClientBinding
import dev.thomas_kiljanczyk.lyriccast.ui.session_client.choose_session.ChooseSessionDialogFragment
import dev.thomas_kiljanczyk.lyriccast.ui.session_client.choose_session.ChooseSessionDialogModel
import dev.thomas_kiljanczyk.lyriccast.ui.shared.fragments.PermissionsRejectedDialogFragment
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

    private lateinit var pickDeviceDialogViewModel: ChooseSessionDialogModel

    private var chooseSessionDialog: ChooseSessionDialogFragment? = null

    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            if (isGranted.values.any { !it }) {
                chooseSessionDialog?.dismiss()
                chooseSessionDialog = null

                PermissionsRejectedDialogFragment(
                    R.string.dialog_fragment_start_session_missing_permissions,
                    onIgnore = {
                        showChooseSessionDialog()
                    }).show(
                    supportFragmentManager, TAG
                )
            } else {
                showChooseSessionDialog()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val rootBinding = ActivitySessionClientBinding.inflate(layoutInflater)
        setContentView(rootBinding.root)
        setSupportActionBar(rootBinding.toolbarControls)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        pickDeviceDialogViewModel = ViewModelProvider(this)[ChooseSessionDialogModel::class.java]

        binding = ContentSessionClientBinding.bind(rootBinding.contentSessionClient.root)

        viewModel.currentSlide.onEach {
            binding.tvControlsSongTitle.text = it.songTitle
            binding.tvSongSlideNumber.text = it.slideNumber
            binding.tvSlidePreview.text = it.slideText

            val showSlideInformation = it.songTitle.isNotBlank() || it.slideNumber.isNotBlank()
            binding.cstlSlideInformation.visibility =
                if (showSlideInformation) View.VISIBLE else View.GONE

        }.flowOn(Dispatchers.Main).launchIn(lifecycleScope)

        viewModel.connectionState.onEach { connectionState ->
            when (connectionState) {
                SessionClientModel.ConnectionState.CONNECTED -> {
                    Toast.makeText(
                        baseContext, R.string.session_client_connected, Toast.LENGTH_SHORT
                    ).show()
                }

                SessionClientModel.ConnectionState.DISCONNECTED -> {
                    Toast.makeText(
                        baseContext, R.string.session_client_disconnected, Toast.LENGTH_SHORT
                    ).show()
                    showChooseSessionDialog()
                }

                SessionClientModel.ConnectionState.FAILED -> {
                    Toast.makeText(
                        baseContext, R.string.session_client_failed_to_connect, Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.flowOn(Dispatchers.Main).launchIn(lifecycleScope)

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
    }

    private fun setupObservers() {
        pickDeviceDialogViewModel.serverEndpointId.onEach(this::observeDialogBluetoothDevice)
            .launchIn(lifecycleScope)
    }

    override fun onStart() {
        super.onStart()
        permissionRequestLauncher.launch(LyricCastApplication.PERMISSIONS)
    }

    override fun onResume() {
        super.onResume()

        viewModel.requestLatestSlide()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopClient()
    }

    private fun showChooseSessionDialog() {
        if (chooseSessionDialog != null && chooseSessionDialog?.isVisible == true) {
            return
        }

        val dialog = ChooseSessionDialogFragment(onClose = { finish() })
        chooseSessionDialog = dialog

        dialog.show(
            supportFragmentManager, ChooseSessionDialogFragment.TAG
        )
    }

    private fun observeDialogBluetoothDevice(endpointId: String?) {
        chooseSessionDialog = null
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