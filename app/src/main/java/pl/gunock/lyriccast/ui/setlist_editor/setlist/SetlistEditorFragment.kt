/*
 * Created by Tomasz Kiljanczyk on 21/12/2021, 16:28
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 21/12/2021, 16:27
 */

package pl.gunock.lyriccast.ui.setlist_editor.setlist

import android.os.Bundle
import android.text.InputFilter
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.FragmentSetlistEditorBinding
import pl.gunock.lyriccast.shared.enums.NameValidationState
import pl.gunock.lyriccast.shared.extensions.hideKeyboard
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.listeners.TouchAdapterItemListener

@AndroidEntryPoint
class SetlistEditorFragment : Fragment() {

    private val args: SetlistEditorFragmentArgs by navArgs()
    private val viewModel: SetlistEditorModel by activityViewModels()
    private lateinit var binding: FragmentSetlistEditorBinding

    private val setlistNameTextWatcher: SetlistNameTextWatcher by lazy {
        SetlistNameTextWatcher(resources, binding, viewModel)
    }

    private lateinit var songItemsAdapter: SetlistSongItemsAdapter


    private var toast: Toast? = null

    private var actionMenu: Menu? = null
    private var actionMode: ActionMode? = null
    private val actionModeCallback: ActionMode.Callback = SetlistEditorActionModeCallback()

    private val itemTouchHelper by lazy { ItemTouchHelper(SetlistItemTouchCallback()) }

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
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel // Initializes viewModel

        loadSetlist()
        setupListeners()
        setupRecyclerView()

        viewModel.numberOfSelectedSongs.observe(viewLifecycleOwner, this::onSelectSong)
        viewModel.selectedSongPosition.observe(viewLifecycleOwner) {
            songItemsAdapter.notifyItemChanged(it)
        }
        viewModel.removedSongPosition.observe(viewLifecycleOwner) {
            songItemsAdapter.notifyItemRemoved(it)
        }
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

        binding.edSetlistName.setOnFocusChangeListener { view_, hasFocus ->
            if (!hasFocus) view_.hideKeyboard()
        }

        binding.btnPickSetlistSongs.setOnClickListener {
            actionMode?.finish()

            val presentation: Array<String> = viewModel.songs
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
            if (checkSetlistNameValidity()) {
                return@setOnClickListener
            }
            lifecycleScope.launch(Dispatchers.Default) {
                if (saveSetlist()) {
                    requireActivity().finish()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        val onHandleTouchListener =
            TouchAdapterItemListener { holder: BaseViewHolder, view, event ->
                view.requestFocus()
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(holder)
                }

                return@TouchAdapterItemListener true
            }

        songItemsAdapter = SetlistSongItemsAdapter(
            items = viewModel.songs,
            selectionTracker = viewModel.selectionTracker,
            onHandleTouchListener = onHandleTouchListener
        )

        binding.rcvSongs.setHasFixedSize(true)
        binding.rcvSongs.layoutManager = LinearLayoutManager(requireContext())
        binding.rcvSongs.adapter = songItemsAdapter

        itemTouchHelper.attachToRecyclerView(binding.rcvSongs)
    }

    private fun checkSetlistNameValidity(): Boolean {
        if (viewModel.validateSetlistName(viewModel.setlistName) != NameValidationState.VALID) {
            binding.edSetlistName.setText(viewModel.setlistName)
            binding.tinSetlistName.requestFocus()
            return false
        }
        return true
    }

    private suspend fun saveSetlist(): Boolean {
        if (viewModel.songs.isEmpty()) {
            withContext(Dispatchers.Main) {
                toast?.cancel()
                toast = Toast.makeText(
                    requireContext(),
                    getString(R.string.setlist_editor_empty_warning),
                    Toast.LENGTH_SHORT
                )
                toast!!.show()
            }
            requireView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            return false
        }

        viewModel.saveSetlist()
        return true
    }

    private fun onSelectSong(numberOfSelectedSongs: Pair<Int, Int>): Boolean {
        val (countBefore: Int, countAfter: Int) = numberOfSelectedSongs

        if ((countBefore == 0 && countAfter == 1) || (countBefore == 1 && countAfter == 0)) {
            songItemsAdapter.notifyItemRangeChanged(0, viewModel.songs.size)
        }

        when (countAfter) {
            0 -> {
                actionMode?.finish()
                return false
            }
            1 -> {
                if (actionMode == null) {
                    actionMode = (requireActivity() as AppCompatActivity).startSupportActionMode(
                        actionModeCallback
                    )
                }

                showMenuActions()
            }
            2 -> showMenuActions(showDuplicate = false)
        }
        return true
    }

    private fun showMenuActions(showDelete: Boolean = true, showDuplicate: Boolean = true) {
        actionMenu ?: return
        actionMenu!!.findItem(R.id.action_menu_delete).isVisible = showDelete
        actionMenu!!.findItem(R.id.menu_duplicate).isVisible = showDuplicate
    }

    private fun removeSelectedSongs(): Boolean {
        viewModel.removeSelectedSongs()
        resetSelection()

        return true
    }

    private fun duplicateSong(): Boolean {
        val insertedPosition = viewModel.duplicateSelectedSong()
        songItemsAdapter.notifyItemInserted(insertedPosition)
        resetSelection()

        return true
    }

    private fun resetSelection() {
        viewModel.resetSongSelection()
        songItemsAdapter.notifyItemRangeChanged(0, viewModel.songs.size)
        showMenuActions(showDelete = false, showDuplicate = false)
    }

    private inner class SetlistEditorActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.action_menu_setlist_editor, menu)
            mode.title = ""
            actionMenu = menu
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            showMenuActions()
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val result = when (item.itemId) {
                R.id.action_menu_delete -> removeSelectedSongs()
                R.id.menu_duplicate -> duplicateSong()
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
            songItemsAdapter.notifyItemMoved(from, to)

            return true
        }

        override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {}
    }
}
