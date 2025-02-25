/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 10/01/2025, 01:46
 */

package dev.thomas_kiljanczyk.lyriccast.ui.main.setlists

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.databinding.FragmentSetlistsBinding
import dev.thomas_kiljanczyk.lyriccast.domain.models.SetlistItem
import dev.thomas_kiljanczyk.lyriccast.shared.extensions.hideKeyboard
import dev.thomas_kiljanczyk.lyriccast.shared.extensions.registerForActivityResult
import dev.thomas_kiljanczyk.lyriccast.shared.utils.DialogFragmentUtils
import dev.thomas_kiljanczyk.lyriccast.ui.setlist_controls.SetlistControlsActivity
import dev.thomas_kiljanczyk.lyriccast.ui.setlist_editor.SetlistEditorActivity
import dev.thomas_kiljanczyk.lyriccast.ui.shared.listeners.InputTextChangedListener
import dev.thomas_kiljanczyk.lyriccast.ui.shared.selection.MappedItemKeyProvider
import dev.thomas_kiljanczyk.lyriccast.ui.shared.selection.SimpleItemDetailsLookup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class SetlistsFragment : Fragment() {
    private companion object {
        const val TAG = "SetlistsFragment"
    }

    private val viewModel: SetlistsModel by activityViewModels()

    private lateinit var binding: FragmentSetlistsBinding

    private lateinit var setlistItemsAdapter: SetlistItemsAdapter

    private var toast: Toast? = null

    private var actionMenu: Menu? = null
    private var actionMode: ActionMode? = null
    private val actionModeCallback: ActionMode.Callback = SetlistsActionModeCallback()

    private val exportChooserResultLauncher =
        registerForActivityResult(this::exportSelectedSetlists)

    private var onBackPressedCallback: OnBackPressedCallback? = null

    private lateinit var tracker: SelectionTracker<Long>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetlistsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
    }

    override fun onDestroy() {
        actionMode?.finish()

        super.onDestroy()
    }

    override fun onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment: Boolean) {
        if (!isPrimaryNavigationFragment) {
            actionMode?.finish()
        }

        super.onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment)
    }

    private fun startExport() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a directory")
        exportChooserResultLauncher.launch(chooserIntent)
    }

    private fun exportSelectedSetlists(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) {
            return
        }

        lifecycleScope.launch(Dispatchers.Default) {
            val uri: Uri = result.data!!.data!!

            val dialogFragment =
                DialogFragmentUtils.createProgressDialogFragment(
                    childFragmentManager,
                    R.string.main_activity_export_preparing_data
                )

            val outputStream = requireActivity().contentResolver.openOutputStream(uri)!!
            val exportMessageFlow = viewModel.exportSelectedSetlists(
                requireActivity().cacheDir.canonicalPath,
                outputStream
            )

            exportMessageFlow.onEach { dialogFragment.setMessage(it) }
                .onCompletion {
                    withContext(Dispatchers.IO) {
                        outputStream.close()
                    }
                    dialogFragment.dismiss()
                }.flowOn(Dispatchers.Main)
                .launchIn(dialogFragment.lifecycleScope)
        }
    }

    private fun setupRecyclerView() {
        setlistItemsAdapter = SetlistItemsAdapter(binding.rcvSetlists.context)
        setlistItemsAdapter.onItemClickListener = this::onPickSetlist

        binding.rcvSetlists.setHasFixedSize(true)
        binding.rcvSetlists.layoutManager = LinearLayoutManager(requireContext())
        binding.rcvSetlists.adapter = setlistItemsAdapter

        tracker = SelectionTracker.Builder(
            "selection",
            binding.rcvSetlists,
            MappedItemKeyProvider(binding.rcvSetlists),
            SimpleItemDetailsLookup(binding.rcvSetlists),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        tracker.addObserver(SetlistSelectionObserver())

        viewModel.setlists
            .onEach { setlistItemsAdapter.submitList(it) }
            .launchIn(lifecycleScope)
    }


    private fun setupListeners() {
        binding.edSetlistNameFilter.addTextChangedListener(InputTextChangedListener { newText ->
            lifecycleScope.launch(Dispatchers.Default) {
                viewModel.searchValues.setlistName = newText
            }
        })

        binding.edSetlistNameFilter.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) view.hideKeyboard()
        }
    }

    private fun onPickSetlist(item: SetlistItem?) {
        if (!tracker.selection.isEmpty) {
            return
        }

        item ?: return

        if (item.setlist.presentation.isEmpty()) {
            toast?.cancel()
            toast = Toast.makeText(
                requireContext(),
                getString(R.string.main_activity_setlist_is_empty),
                Toast.LENGTH_SHORT
            )
            toast!!.show()
            requireView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            return
        }

        val intent = Intent(context, SetlistControlsActivity::class.java)
        intent.putExtra("setlistId", item.setlist.id)
        startActivity(intent)
    }

    private fun onSelectSetlist() {
        when (tracker.selection.size()) {
            0 -> {
                actionMode?.finish()
            }

            1 -> {
                if (actionMode == null) {
                    actionMode = (requireActivity() as AppCompatActivity)
                        .startSupportActionMode(actionModeCallback)
                    viewModel.showSelectionCheckboxes()
                    notifyAllItemsChanged()
                }

                showMenuActions()
            }

            2 -> showMenuActions(showEdit = false)
        }
    }

    private fun editSelectedSetlist() {
        val selectedItem = viewModel.filteredSetlists
            .first { setlistItem -> setlistItem.isSelected }

        Log.v(TAG, "Editing setlist : ${selectedItem.setlist}")
        val intent = Intent(context, SetlistEditorActivity::class.java)
        intent.putExtra("setlistId", selectedItem.setlist.id)
        startActivity(intent)
    }

    private fun showMenuActions(
        showGroupActions: Boolean = true,
        showEdit: Boolean = true
    ) {
        actionMenu?.apply {
            findItem(R.id.action_menu_delete).isVisible = showGroupActions
            findItem(R.id.action_menu_export_selected).isVisible = showGroupActions
            findItem(R.id.action_menu_edit).isVisible = showEdit
        }
    }

    private fun resetSelection() {
        tracker.clearSelection()
        viewModel.hideSelectionCheckboxes()
        setlistItemsAdapter.notifyItemRangeChanged(0, setlistItemsAdapter.itemCount, true)
    }

    private fun notifyAllItemsChanged() {
        setlistItemsAdapter.notifyItemRangeChanged(0, setlistItemsAdapter.itemCount, true)
    }


    private inner class SetlistsActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.action_menu_main, menu)
            menu.findItem(R.id.action_menu_add_setlist).isVisible = false
            mode.title = ""
            actionMenu = menu

            onBackPressedCallback =
                requireActivity().onBackPressedDispatcher.addCallback(requireActivity()) {
                    resetSelection()
                    onBackPressedCallback?.remove()
                    onBackPressedCallback = null
                }

            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            showMenuActions(showGroupActions = false, showEdit = false)
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.action_menu_delete -> {
                    lifecycleScope.launch(Dispatchers.Default) {
                        viewModel.deleteSelectedSetlists()
                        withContext(Dispatchers.Main) { mode.finish() }
                    }
                }

                R.id.action_menu_export_selected -> {
                    startExport()
                    mode.finish()
                }

                R.id.action_menu_edit -> {
                    editSelectedSetlist()
                    mode.finish()
                }

                else -> {}
            }

            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            actionMenu = null
            resetSelection()

            onBackPressedCallback?.remove()
            onBackPressedCallback = null
        }
    }


    private inner class SetlistSelectionObserver : SelectionTracker.SelectionObserver<Long>() {
        override fun onItemStateChanged(key: Long, selected: Boolean) {
            super.onItemStateChanged(key, selected)
            if (viewModel.selectSetlist(key, selected)) {
                onSelectSetlist()
            }
        }
    }
}
