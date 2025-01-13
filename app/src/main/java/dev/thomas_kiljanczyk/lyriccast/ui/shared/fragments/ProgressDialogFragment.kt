/*
 * Created by Tomasz Kiljanczyk on 12/01/2025, 23:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 12/01/2025, 23:54
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.fragments

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.databinding.DialogFragmentProgressBinding


class ProgressDialogFragment(
    @StringRes private val initialMessageResId: Int? = null
) : DialogFragment() {

    companion object {
        const val TAG = "ProgressDialogFragment"
    }

    private lateinit var binding: DialogFragmentProgressBinding

    private lateinit var defaultTextColor: ColorStateList
    private lateinit var defaultProgressIndicatorColor: IntArray

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogFragmentProgressBinding.inflate(layoutInflater)

        binding.btnProgressOk.visibility = View.GONE
        binding.btnProgressOk.setOnClickListener { dismiss() }

        if (initialMessageResId != null) {
            setMessage(initialMessageResId)
        }

        return MaterialAlertDialogBuilder(requireActivity()).setView(binding.root)
            .setCancelable(false).create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        defaultProgressIndicatorColor = binding.pgbProgress.indicatorColor
        defaultTextColor = binding.btnProgressOk.textColors
        return binding.root
    }

    fun setMessage(@StringRes stringResourceId: Int) {
        if (stringResourceId == 0) {
            return
        }

        binding.tvProgressMessage.text = getString(stringResourceId)
    }

    fun setErrorState(isError: Boolean) {
        setErrorColor(isError)
        setShowOkButton(isError)
    }

    private fun setErrorColor(errorColor: Boolean) {
        if (errorColor) {
            val errorProgressColor =
                requireContext().getColor(R.color.error_Indeterminate_progress_bar)
            binding.pgbProgress.let {
                it.setIndicatorColor(errorProgressColor)
                it.isIndeterminate = false
                it.setProgress(100, true)
            }

            binding.btnProgressOk.setTextColor(errorProgressColor)
        } else {
            binding.pgbProgress.let {
                it.setIndicatorColor(*defaultProgressIndicatorColor)
                it.isIndeterminate = true
            }
            binding.btnProgressOk.setTextColor(defaultTextColor)
        }
    }

    private fun setShowOkButton(showOkButton: Boolean) {
        if (showOkButton) {
            binding.btnProgressOk.visibility = View.VISIBLE
        } else {
            binding.btnProgressOk.visibility = View.GONE
        }
    }

}