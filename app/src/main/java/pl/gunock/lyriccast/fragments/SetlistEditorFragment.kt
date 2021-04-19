/*
 * Created by Tomasz Kiljanczyk on 4/19/21 5:12 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/18/21 6:22 PM
 */

package pl.gunock.lyriccast.fragments

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import pl.gunock.lyriccast.LyricCastApplication
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.adapters.SetlistSongItemsAdapter
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.datamodel.LyricCastRepository
import pl.gunock.lyriccast.datamodel.entities.Setlist
import pl.gunock.lyriccast.datamodel.entities.SetlistSongCrossRef
import pl.gunock.lyriccast.datamodel.entities.Song
import pl.gunock.lyriccast.datamodel.entities.relations.SetlistWithSongs
import pl.gunock.lyriccast.enums.NameValidationState
import pl.gunock.lyriccast.helpers.KeyboardHelper
import pl.gunock.lyriccast.listeners.TouchAdapterItemListener
import pl.gunock.lyriccast.misc.SelectionTracker
import pl.gunock.lyriccast.models.SongItem


class SetlistEditorFragment : Fragment() {

    private companion object {
        const val TAG = "SetlistEditorFragment"
    }

    private val mArgs: SetlistEditorFragmentArgs by navArgs()
    private lateinit var mRepository: LyricCastRepository
    private val mDatabaseViewModel: DatabaseViewModel by viewModels {
        DatabaseViewModel.Factory(
            requireContext().resources,
            (requireActivity().application as LyricCastApplication).repository
        )
    }

    private val mSetlistNameTextWatcher: SetlistNameTextWatcher = SetlistNameTextWatcher()

    private var mSetlistSongs: List<SongItem> = listOf()
    private var mSongItemsAdapter: SetlistSongItemsAdapter? = null
    private lateinit var mSelectionTracker: SelectionTracker<SetlistSongItemsAdapter.ViewHolder>

    private var mIntentSetlistWithSongs: SetlistWithSongs? = null
    private lateinit var mSetlistNames: Set<String>

    private var mActionMenu: Menu? = null
    private var mActionMode: ActionMode? = null
    private val mActionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.action_menu_setlist_editor, menu)
            mode.title = ""
            mActionMenu = menu
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
            mActionMode = null
            mActionMenu = null
            resetSelection()
        }

    }

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

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

                    val adapter = recyclerView.adapter as SetlistSongItemsAdapter
                    adapter.moveItem(from, to)
                    adapter.notifyItemMoved(from, to)

                    return true
                }

                override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {}
            }

        ItemTouchHelper(simpleItemTouchCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRepository = (requireActivity().application as LyricCastApplication).repository
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setlist_editor, container, false)
    }

    override fun onDestroyView() {
        mDatabaseViewModel.allSetlists.removeObservers(requireActivity())
        itemTouchHelper.attachToRecyclerView(null)
        mSongItemsAdapter!!.removeObservers()
        mSongItemsAdapter = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mIntentSetlistWithSongs = requireActivity().intent.getParcelableExtra("setlistWithSongs")

        val setlistNameInput: TextView = view.findViewById(R.id.tin_setlist_name)
        setlistNameInput.filters = arrayOf(InputFilter.LengthFilter(30))

        mDatabaseViewModel.allSetlists.observe(requireActivity()) { setlists ->
            mSetlistNames = setlists.map { setlist -> setlist.name }.toSet()
        }

        var setlistSongs: Map<Long, Song> = mapOf()
        var setlistSongsCrossRef: List<SetlistSongCrossRef> = listOf()
        if (mArgs.setlistWithSongs != null) {
            setlistSongs = mArgs.setlistWithSongs!!.songs
                .map { it.id to it }
                .toMap()

            setlistSongsCrossRef = mArgs.setlistWithSongs!!.setlistSongCrossRefs

            setlistNameInput.text = mArgs.setlistWithSongs!!.setlist.name
        } else if (mIntentSetlistWithSongs != null) {
            setlistNameInput.text = mIntentSetlistWithSongs!!.setlist.name
            setlistSongs = mIntentSetlistWithSongs!!.songs
                .map { it.id to it }
                .toMap()
            setlistSongsCrossRef = mIntentSetlistWithSongs!!.setlistSongCrossRefs
        }

        // TODO: Rework for MongoDB
//        mSetlistSongs = setlistSongsCrossRef.sorted()
//            .map { SongItem(setlistSongs[it.songId]!!) }

        setupListeners(view)
        setupRecyclerView()
    }

    private fun setupListeners(view: View) {
        val setlistNameInput: TextView = view.findViewById(R.id.tin_setlist_name)
        setlistNameInput.addTextChangedListener(mSetlistNameTextWatcher)

        setlistNameInput.setOnFocusChangeListener { view_, hasFocus ->
            if (!hasFocus) {
                KeyboardHelper.hideKeyboard(view_)
            }
        }

        view.findViewById<Button>(R.id.btn_pick_setlist_songs).setOnClickListener {
            mActionMode?.finish()
            val action = SetlistEditorFragmentDirections.actionSetlistEditorToSetlistEditorSongs(
                setlistWithSongs = createSetlistWithSongs()
            )

            findNavController().navigate(action)
        }

        view.findViewById<Button>(R.id.btn_save_setlist).setOnClickListener {
            if (saveSetlist()) {
                requireActivity().finish()
            }
        }
    }

    private fun setupRecyclerView() {
        val onHandleTouchListener =
            TouchAdapterItemListener { holder: SetlistSongItemsAdapter.ViewHolder, view, event ->
                view.requestFocus()
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(holder)
                }

                return@TouchAdapterItemListener true
            }

        mSelectionTracker = SelectionTracker(this::onSetlistClick)
        mSongItemsAdapter = SetlistSongItemsAdapter(
            requireContext(),
            mSetlistSongs.toMutableList(),
            mSelectionTracker = mSelectionTracker,
            mOnHandleTouchListener = onHandleTouchListener
        )

        val songsRecyclerView: RecyclerView = requireView().findViewById(R.id.rcv_songs)
        songsRecyclerView.setHasFixedSize(true)
        songsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        songsRecyclerView.adapter = mSongItemsAdapter

        itemTouchHelper.attachToRecyclerView(songsRecyclerView)
    }

    private fun onSetlistClick(
        @Suppress("UNUSED_PARAMETER")
        holder: SetlistSongItemsAdapter.ViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        if (isLongClick || mSelectionTracker.count != 0) {
            val item: SongItem = mSongItemsAdapter!!.songItems[position]
            return selectSong(item)
        }
        return false
    }

    private fun createSetlistWithSongs(): SetlistWithSongs {
        val setlistNameInput: TextView = requireView().findViewById(R.id.tin_setlist_name)
        val setlistName = setlistNameInput.text.toString()
        val setlist = if (mArgs.setlistWithSongs != null) {
            Setlist(mArgs.setlistWithSongs!!.setlist.setlistId, setlistName)
        } else {
            Setlist(null, setlistName)
        }

        // TODO: Rework for MongoDB
//        val songs = mSongItemsAdapter!!.songItems
//            .map { item -> item.song }
//            .distinct()
//
//        val crossRef: List<SetlistSongCrossRef> = mSongItemsAdapter!!.songItems
//            .mapIndexed { index, item ->
//                SetlistSongCrossRef(null, setlist.id, item.song.id, index)
//            }

        val songs: List<Song> = listOf()
        val crossRef: List<SetlistSongCrossRef> = listOf()
        return SetlistWithSongs(setlist, songs, crossRef)
    }

    private fun validateSetlistName(name: String): NameValidationState {
        if (name.isBlank()) {
            return NameValidationState.EMPTY
        }

        val isAlreadyInUse =
            mIntentSetlistWithSongs?.setlist?.name != name && mSetlistNames.contains(name)

        return if (isAlreadyInUse) {
            NameValidationState.ALREADY_IN_USE
        } else {
            NameValidationState.VALID
        }
    }

    private fun saveSetlist(): Boolean {
        val setlistNameInput: TextView = requireView().findViewById(R.id.tin_setlist_name)
        val setlistName = setlistNameInput.text.toString().trim()

        if (validateSetlistName(setlistName) != NameValidationState.VALID) {
            setlistNameInput.text = setlistName
            setlistNameInput.requestFocus()
            return false
        }

        if (mSetlistSongs.isEmpty()) {
            val toast = Toast.makeText(
                requireContext(),
                getString(R.string.setlist_editor_empty_warning),
                Toast.LENGTH_SHORT
            )
            toast.show()
            return false
        }

        val setlistWithSongs = createSetlistWithSongs()
        mDatabaseViewModel.upsertSetlist(setlistWithSongs)
        Log.i(TAG, "Created setlist: $setlistWithSongs")
        return true
    }

    private fun selectSong(item: SongItem): Boolean {
        item.isSelected.value = !item.isSelected.value!!

        when (mSelectionTracker.countAfter) {
            0 -> {
                if (mSongItemsAdapter!!.showCheckBox.value!!) {
                    mSongItemsAdapter!!.showCheckBox.value = false
                }
                mActionMode?.finish()
                return false
            }
            1 -> {
                if (!mSongItemsAdapter!!.showCheckBox.value!!) {
                    mSongItemsAdapter!!.showCheckBox.value = true
                }

                if (mActionMode == null) {
                    mActionMode = (requireActivity() as AppCompatActivity).startSupportActionMode(
                        mActionModeCallback
                    )
                }

                showMenuActions()
            }
            2 -> showMenuActions(showDuplicate = false)
        }
        return true
    }

    private fun showMenuActions(showDelete: Boolean = true, showDuplicate: Boolean = true) {
        mActionMenu ?: return
        mActionMenu!!.findItem(R.id.action_menu_delete).isVisible = showDelete
        mActionMenu!!.findItem(R.id.menu_duplicate).isVisible = showDuplicate
    }

    private fun removeSelectedSongs(): Boolean {
        mSongItemsAdapter!!.removeSelectedItems()
        resetSelection()

        return true
    }

    private fun duplicateSong(): Boolean {
        mSongItemsAdapter!!.duplicateSelectedItem()
        resetSelection()

        return true
    }

    private fun resetSelection() {
        if (mSongItemsAdapter!!.showCheckBox.value!!) {
            mSongItemsAdapter!!.showCheckBox.value = false
        }
        mSongItemsAdapter!!.resetSelection()
        showMenuActions(showDelete = false, showDuplicate = false)
    }

    inner class SetlistNameTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            val setlistNameInputLayout: TextInputLayout =
                requireView().findViewById(R.id.tv_setlist_name)

            val newText = s.toString().trim()

            when (validateSetlistName(newText)) {
                NameValidationState.EMPTY -> {
                    setlistNameInputLayout.error = getString(R.string.setlist_editor_enter_name)
                }
                NameValidationState.ALREADY_IN_USE -> {
                    setlistNameInputLayout.error =
                        getString(R.string.setlist_editor_name_already_used)
                }
                NameValidationState.VALID -> {
                    setlistNameInputLayout.error = null
                }
            }
        }
    }

}
