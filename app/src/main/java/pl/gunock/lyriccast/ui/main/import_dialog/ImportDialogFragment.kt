/*
 * Created by Tomasz Kiljanczyk on 29/12/2021, 14:52
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 26/12/2021, 11:17
 */

package pl.gunock.lyriccast.ui.main.import_dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.AndroidEntryPoint
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.DialogFragmentImportBinding
import pl.gunock.lyriccast.datatransfer.enums.ImportFormat


@AndroidEntryPoint
class ImportDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "ImportDialogFragment"
    }

    private val viewModel: ImportDialogModel by activityViewModels()

    private lateinit var binding: DialogFragmentImportBinding

    val isAccepted: MutableLiveData<Boolean> = MutableLiveData(false)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFragmentImportBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDialogTitle.text = getString(R.string.main_activity_import_dialog_title)

        setupColorSpinner()
        setupListeners()
        viewModel.importFormat =
            ImportFormat.getByName(binding.spnImportFormat.selectedItem as String)
    }

    private fun setupColorSpinner() {
        binding.spnImportFormat.adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.import_formats,
            android.R.layout.simple_list_item_1
        )
    }

    private fun setupListeners() {
        binding.chkDeleteAll.setOnCheckedChangeListener { _, isChecked ->
            viewModel.deleteAll = isChecked
            if (isChecked) {
                binding.chkReplaceOnConflict.isChecked = false
                binding.chkReplaceOnConflict.isEnabled = false
            } else {
                binding.chkReplaceOnConflict.isEnabled = true
            }
        }

        binding.btnImport.setOnClickListener {
            viewModel.deleteAll = binding.chkDeleteAll.isChecked
            viewModel.replaceOnConflict = binding.chkReplaceOnConflict.isChecked
            viewModel.importFormat =
                ImportFormat.getByName(binding.spnImportFormat.selectedItem as String)
            isAccepted.postValue(true)
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

}