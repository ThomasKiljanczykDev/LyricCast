/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.ui.setlist_editor.setlist

import android.os.Bundle
import android.text.InputFilter
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.SelectionTracker.SelectionPredicate
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.databinding.FragmentSetlistEditorBinding
import dev.thomas_kiljanczyk.lyriccast.shared.enums.NameValidationState
import dev.thomas_kiljanczyk.lyriccast.shared.extensions.hideKeyboard
import dev.thomas_kiljanczyk.lyriccast.ui.shared.listeners.TouchAdapterItemListener
import dev.thomas_kiljanczyk.lyriccast.ui.shared.selection.MappedItemKeyProvider
import dev.thomas_kiljanczyk.lyriccast.ui.shared.selection.SelectionViewHolder
import dev.thomas_kiljanczyk.lyriccast.ui.shared.selection.SimpleItemDetailsLookup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SetlistEditorFragment : Fragment() {

    private val args: SetlistEditorFragmentArgs by navArgs()
    private val viewModel: SetlistEditorModel by activityViewModels()
    private lateinit var binding: FragmentSetlistEditorBinding

    private val setlistNameTextWatcher: SetlistNameTextWatcher by lazy {
        SetlistNameTextWatcher(resources, binding, viewModel)
    }

    private lateinit var setlistSongItemsAdapter: SetlistSongItemsAdapter


    private var toast: Toast? = null

    private var actionMenu: Menu? = null
    private var actionMode: ActionMode? = null
    private val actionModeCallback: ActionMode.Callback = SetlistEditorActionModeCallback()

    private val itemTouchHelper by lazy { ItemTouchHelper(SetlistItemTouchCallback()) }

    private var onBackPressedCallback: OnBackPressedCallback? = null

    private lateinit var tracker: SelectionTracker<Long>

    private var selectionDisabled: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetlistEditorBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroy() {
        itemTouchHelper.attachToRecyclerView(null)
        actionMode?.finish()
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadSetlist()
        setupListeners()
        setupRecyclerView()
    }

    private fun loadSetlist() {
        val intentSetlistId =
            requireActivity().intent.getStringExtra("setlistId")

        val intentSetlistPresentation = requireActivity().intent
            .getStringArrayExtra("setlistSongs")

        binding.edSetlistName.filters = arrayOf(
            InputFilter.LengthFilter(resources.getInteger(R.integer.ed_max_length_setlist_name))
        )

        when {
            args.setlistId != null -> {
                viewModel.loadSetlist(
                    args.setlistId!!,
                    args.setlistName!!,
                    args.presentation!!.toList()
                )
                binding.edSetlistName.setText(viewModel.setlistName)
            }

            intentSetlistId != null -> {
                viewModel.loadEditedSetlist(intentSetlistId)
                binding.edSetlistName.setText(viewModel.setlistName)
            }

            intentSetlistPresentation != null -> {
                viewModel.loadAdhocSetlist(intentSetlistPresentation.toList())
            }
        }
    }

    private fun setupListeners() {
        binding.edSetlistName.addTextChangedListener(setlistNameTextWatcher)

        binding.edSetlistName.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) view.hideKeyboard()
        }

        binding.btnPickSetlistSongs.setOnClickListener {
            actionMode?.finish()

            val presentation: Array<String> = viewModel.songsFlow.value
                .map { it.song.id }
                .toTypedArray()

            val action = SetlistEditorFragmentDirections.actionSetlistEditorToSetlistEditorSongs(
                setlistId = viewModel.setlistId,
                setlistName = viewModel.setlistName,
                presentation = presentation
            )

            findNavController().navigate(action)
        }

        binding.btnSaveSetlist.setOnClickListener {
            saveSetlist()
        }
    }

    private fun setupRecyclerView() {
        val onHandleTouchListener =
            TouchAdapterItemListener { holder: SelectionViewHolder<SetlistSongItem>, view, event ->
                view.requestFocus()
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(holder)
                    selectionDisabled = true
                }

                return@TouchAdapterItemListener true
            }

        setlistSongItemsAdapter = SetlistSongItemsAdapter(
            binding.rcvSongs.context,
            onHandleTouchListener = onHandleTouchListener
        )

        binding.rcvSongs.setHasFixedSize(true)
        binding.rcvSongs.layoutManager = LinearLayoutManager(requireContext())
        binding.rcvSongs.adapter = setlistSongItemsAdapter

        tracker = SelectionTracker.Builder(
            "selection",
            binding.rcvSongs,
            MappedItemKeyProvider(binding.rcvSongs),
            SimpleItemDetailsLookup(binding.rcvSongs),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SetlistSongSelectionPredicate()
        ).build()

        tracker.addObserver(SetlistSelectionObserver())

        itemTouchHelper.attachToRecyclerView(binding.rcvSongs)

        viewModel.songsFlow
            .onEach { setlistSongItemsAdapter.submitList(it) }
            .launchIn(lifecycleScope)
    }

    private fun checkSetlistNameValidity(): Boolean {
        if (viewModel.validateSetlistName(viewModel.setlistName) != NameValidationState.VALID) {
            binding.edSetlistName.setText(viewModel.setlistName)
            binding.tinSetlistName.requestFocus()
            return false
        }
        return true
    }

    private fun saveSetlist() {
        if (!checkSetlistNameValidity()) {
            return
        }

        if (setlistSongItemsAdapter.itemCount > 0) {
            lifecycleScope.launch(Dispatchers.Default) { viewModel.saveSetlist() }
            requireActivity().finish()
        } else {
            toast?.cancel()
            toast = Toast.makeText(
                requireContext(),
                getString(R.string.setlist_editor_empty_warning),
                Toast.LENGTH_SHORT
            ).apply { show() }
            requireView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    private fun onSelectSong() {
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

            2 -> showMenuActions(showDuplicate = false)
        }
    }

    private fun showMenuActions(showDelete: Boolean = true, showDuplicate: Boolean = true) {
        actionMenu?.apply {
            findItem(R.id.action_menu_delete).isVisible = showDelete
            findItem(R.id.menu_duplicate).isVisible = showDuplicate
        }
    }

    private fun removeSelectedSongs() {
        viewModel.removeSelectedSongs()
    }

    private fun duplicateSong() {
        viewModel.duplicateSelectedSong()
    }

    private fun resetSelection() {
        tracker.clearSelection()
        viewModel.hideSelectionCheckboxes()
        notifyAllItemsChanged()
    }

    private fun notifyAllItemsChanged() {
        setlistSongItemsAdapter.notifyItemRangeChanged(0, setlistSongItemsAdapter.itemCount, true)
    }


    private inner class SetlistEditorActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.action_menu_setlist_editor, menu)
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
            showMenuActions()
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val result = when (item.itemId) {
                R.id.action_menu_delete -> {
                    removeSelectedSongs()
                    true
                }

                R.id.menu_duplicate -> {
                    duplicateSong()
                    true
                }

                else -> false
            }

            if (result) {
                mode.finish()
            }

            return result
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            actionMenu = null
            resetSelection()

            onBackPressedCallback?.remove()
            onBackPressedCallback = null
        }

    }


    private inner class SetlistItemTouchCallback :
        ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

        override fun isLongPressDragEnabled(): Boolean {
            return false
        }

        override fun onMove(
            recyclerView: RecyclerView,
            holder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = holder.absoluteAdapterPosition
            val to = target.absoluteAdapterPosition

            viewModel.moveSong(from, to)
            setlistSongItemsAdapter.notifyItemMoved(from, to)

            return true
        }

        override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {}

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            selectionDisabled = false
        }
    }


    private inner class SetlistSelectionObserver : SelectionTracker.SelectionObserver<Long>() {
        override fun onItemStateChanged(key: Long, selected: Boolean) {
            super.onItemStateChanged(key, selected)
            if (viewModel.selectSong(key, selected)) {
                onSelectSong()
            }
        }
    }


    private inner class SetlistSongSelectionPredicate : SelectionPredicate<Long>() {
        override fun canSetStateForKey(key: Long, nextState: Boolean): Boolean {
            return !selectionDisabled
        }

        override fun canSetStateAtPosition(position: Int, nextState: Boolean): Boolean {
            return !selectionDisabled
        }

        override fun canSelectMultiple(): Boolean {
            return true
        }
    }
}
