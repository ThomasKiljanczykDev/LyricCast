/*
 * Created by Tomasz Kiljanczyk on 12/01/2025, 23:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 12/01/2025, 23:20
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.menu.gms_nearby_session.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.application.LyricCastApplication
import dev.thomas_kiljanczyk.lyriccast.databinding.DialogFragmentStartSessionServerBinding
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.GmsNearbySessionServerContext
import dev.thomas_kiljanczyk.lyriccast.ui.shared.fragments.PermissionsRejectedDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class StartSessionServerDialogFragment : DialogFragment() {
    companion object {
        const val TAG = "StartServerSessionDialogFragment"
    }

    inner class SessionNameTextWatcher : TextWatcher {
        private val enterNameErrorText =
            resources.getString(R.string.dialog_fragment_start_session_error_empty_name)

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            val newText = s.toString().trim()

            viewModel.sessionName = newText

            if (newText.isBlank()) {
                binding.tinSessionName.error = enterNameErrorText
                viewModel.sessionNameIsValid.value = false
                return
            }

            binding.tinSessionName.error = null
            viewModel.sessionNameIsValid.value = true
        }
    }

    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            if (isGranted.values.any { !it }) {
                PermissionsRejectedDialogFragment(R.string.dialog_fragment_start_session_missing_permissions).show(
                    requireActivity().supportFragmentManager,
                    TAG
                )
            }
        }

    private val viewModel: StartSessionServerDialogModel by viewModels()

    private lateinit var binding: DialogFragmentStartSessionServerBinding

    private val setlistNameTextWatcher: SessionNameTextWatcher by lazy {
        SessionNameTextWatcher()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogFragmentStartSessionServerBinding.inflate(layoutInflater)

        val deviceName =
            Settings.Global.getString(requireContext().contentResolver, Settings.Global.DEVICE_NAME)

        setupListeners()

        binding.edSessionName.setText(deviceName)

        viewModel.sessionName = deviceName
        viewModel.serverAdvertisingState.onEach {
            when (it.state) {
                GmsNearbySessionServerContext.AdvertisingState.ADVERTISING -> {
                    Toast.makeText(
                        requireContext(),
                        R.string.dialog_fragment_start_session_session_started,
                        Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                }

                GmsNearbySessionServerContext.AdvertisingState.FAILED -> {
                    setButtonsEnabled(true)
                    Toast.makeText(
                        requireContext(),
                        it.explanationResId
                            ?: R.string.dialog_fragment_start_session_session_start_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                GmsNearbySessionServerContext.AdvertisingState.NOT_ADVERTISING -> {
                    setButtonsEnabled(true)
                }
            }
        }.flowOn(Dispatchers.Main).launchIn(lifecycleScope)

        return MaterialAlertDialogBuilder(
            requireContext(), R.style.ThemeOverlay_LyricCast_MaterialAlertDialog
        ).setTitle(R.string.dialog_fragment_start_session_title)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.dialog_fragment_start_session_start, null)
            .setView(binding.root).create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        permissionRequestLauncher.launch(LyricCastApplication.PERMISSIONS)

        val positiveButton =
            (dialog as androidx.appcompat.app.AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            setButtonsEnabled(false)
            viewModel.startSessionServer()
        }

        viewModel.sessionNameIsValid.onEach(::setButtonsEnabled).flowOn(Dispatchers.Main)
            .launchIn(lifecycleScope)
    }

    private fun setButtonsEnabled(isEnabled: Boolean) {
        val positiveButton =
            (dialog as androidx.appcompat.app.AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.isEnabled = isEnabled

        val negativeButton =
            (dialog as androidx.appcompat.app.AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE)
        negativeButton.isEnabled = isEnabled
    }

    private fun setupListeners() {
        binding.edSessionName.addTextChangedListener(setlistNameTextWatcher)
    }
}