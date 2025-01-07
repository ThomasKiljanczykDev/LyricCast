/*
 * Created by Tomasz Kiljanczyk on 07/01/2025, 20:26
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 07/01/2025, 20:16
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.menu.gms_nearby_session.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.databinding.DialogFragmentStartSessionServerBinding
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.GmsNearbySessionServerContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SessionNameTextWatcher(
    resources: Resources,
    private val binding: DialogFragmentStartSessionServerBinding,
    private val viewModel: StartSessionServerDialogModel
) : TextWatcher {

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

@AndroidEntryPoint
class StartSessionServerDialogFragment : DialogFragment() {
    companion object {
        const val TAG = "StartServerSessionDialogFragment"
    }

    private val viewModel: StartSessionServerDialogModel by viewModels()

    private lateinit var binding: DialogFragmentStartSessionServerBinding

    private val setlistNameTextWatcher: SessionNameTextWatcher by lazy {
        SessionNameTextWatcher(resources, binding, viewModel)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogFragmentStartSessionServerBinding.inflate(layoutInflater)

        val deviceName =
            Settings.Global.getString(requireContext().contentResolver, Settings.Global.DEVICE_NAME)

        setupListeners()

        binding.edSessionName.setText(deviceName)

        viewModel.sessionName = deviceName
        viewModel.serverAdvertisingState.onEach {
            when (it) {
                GmsNearbySessionServerContext.AdvertisingState.ADVERTISING -> {
                    Toast.makeText(requireContext(), "Session started", Toast.LENGTH_SHORT).show()
                    dismiss()
                }

                GmsNearbySessionServerContext.AdvertisingState.FAILED -> {
                    setPositiveButtonEnabled(true)
                    Toast.makeText(requireContext(), "Failed to start session", Toast.LENGTH_SHORT)
                        .show()
                }

                GmsNearbySessionServerContext.AdvertisingState.NOT_ADVERTISING -> {
                    setPositiveButtonEnabled(true)
                }
            }
        }.flowOn(Dispatchers.Main).launchIn(lifecycleScope)

        return MaterialAlertDialogBuilder(
            requireContext(), R.style.ThemeOverlay_LyricCast_MaterialAlertDialog
        )
            .setTitle(R.string.dialog_fragment_start_session_title)
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

        val positiveButton =
            (dialog as androidx.appcompat.app.AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            setPositiveButtonEnabled(false)
            viewModel.startSessionServer()
        }


        viewModel.sessionNameIsValid.onEach(::setPositiveButtonEnabled).flowOn(Dispatchers.Main)
            .launchIn(lifecycleScope)
    }

    private fun setPositiveButtonEnabled(isEnabled: Boolean) {
        val positiveButton =
            (dialog as androidx.appcompat.app.AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.isEnabled = isEnabled
    }

    private fun setupListeners() {
        binding.edSessionName.addTextChangedListener(setlistNameTextWatcher)
    }
}