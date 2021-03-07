/*
 * Created by Tomasz Kiljańczyk on 3/7/21 11:44 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/7/21 3:15 PM
 */

package pl.gunock.lyriccast.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.textfield.TextInputLayout
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.activities.SongControlsActivity
import pl.gunock.lyriccast.activities.SongEditorActivity
import pl.gunock.lyriccast.adapters.SongItemsAdapter
import pl.gunock.lyriccast.extensions.normalize
import pl.gunock.lyriccast.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.listeners.InputTextChangedListener
import pl.gunock.lyriccast.listeners.ItemSelectedSpinnerListener
import pl.gunock.lyriccast.listeners.LongClickAdapterItemListener
import pl.gunock.lyriccast.models.SongItem
import kotlin.system.measureTimeMillis


class SongsFragment : Fragment() {
    private companion object {
        const val TAG = "SongsFragment"
    }

    private var castContext: CastContext? = null

    private lateinit var menu: Menu
    private lateinit var searchViewEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var songItemsRecyclerView: RecyclerView

    private var songItems: Set<SongItem> = setOf()
    private lateinit var songItemsAdapter: SongItemsAdapter
    private var selectionCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        castContext = CastContext.getSharedInstance()

        val searchView: TextInputLayout = view.findViewById(R.id.tv_filter_songs)
        searchViewEditText = searchView.editText!!

        categorySpinner = view.findViewById(R.id.spn_category)
        songItemsRecyclerView = view.findViewById(R.id.rcv_songs)

        view.findViewById<SwitchCompat>(R.id.swt_selected_songs).visibility = View.GONE

        with(songItemsRecyclerView) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = null
        }

        setupListeners()
    }

    override fun onResume() {
        super.onResume()

        setupCategorySpinner()
        setupSongs()
        resetSelection()

        searchViewEditText.setText("")
        categorySpinner.setSelection(0)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
        super.onCreateOptionsMenu(menu, inflater)

        showMenuActions(showDelete = false, showEdit = false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete -> deleteSelectedSongs()
            R.id.menu_edit -> editSelectedSong()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupListeners() {
        searchViewEditText.addTextChangedListener(InputTextChangedListener {
            filterSongs(
                searchViewEditText.editableText.toString(),
                category = categorySpinner.selectedItem.toString()
            )
        })

        categorySpinner.onItemSelectedListener = ItemSelectedSpinnerListener { _, _ ->
            filterSongs(
                searchViewEditText.editableText.toString(),
                category = categorySpinner.selectedItem.toString()
            )
        }
    }

    private fun setupCategorySpinner() {
        val categorySpinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listOf("All") + SongsContext.categories.toList()
        )
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categorySpinnerAdapter
    }

    private fun setupSongs() {
        songItems = SongsContext.getSongItems()

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val showAuthor = prefs.getBoolean("showAuthor", true)

        val onLongClickListener =
            LongClickAdapterItemListener { holder: SongItemsAdapter.SongViewHolder, position: Int, _ ->
                val item = songItemsAdapter.songItems[position]
                selectSong(item, holder)
                return@LongClickAdapterItemListener true
            }

        val onClickListener =
            ClickAdapterItemListener { holder: SongItemsAdapter.SongViewHolder, position: Int, _ ->
                val item: SongItem = songItemsAdapter.songItems[position]
                if (selectionCount == 0) {
                    pickSong(item)
                } else {
                    requireView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    selectSong(item, holder)
                }
            }

        songItemsAdapter = SongItemsAdapter(
            songItems.toMutableList(),
            showCheckBox = false,
            showAuthor = showAuthor,
            onItemLongClickListener = onLongClickListener,
            onItemClickListener = onClickListener
        )

        songItemsRecyclerView.adapter = songItemsAdapter
    }

    private fun filterSongs(title: String, category: String = "All") {
        Log.v(TAG, "filterSongs invoked")

        resetSelection()

        val normalizedTitle = title.normalize()

        val predicates: MutableList<(SongItem) -> Boolean> = mutableListOf()

        if (category != "All") {
            predicates.add { songItem -> songItem.category == category }
        }

        predicates.add { songItem ->
            songItem.title.normalize().contains(normalizedTitle, ignoreCase = true)
        }

        val duration = measureTimeMillis {
            songItemsAdapter.songItems = songItems.filter { songItem ->
                predicates.all { predicate -> predicate(songItem) }
            }.toMutableList()
        }
        Log.v(TAG, "Filtering took : ${duration}ms")

        songItemsAdapter.notifyDataSetChanged()
    }

    private fun pickSong(item: SongItem) {
        val songSections = SongsContext.getSongLyrics(item.title)!!.lyrics
        val songMetadata = SongsContext.getSongMetadata(item.title)!!
        val lyrics = songMetadata.presentation.map { sectionName -> songSections[sectionName]!! }

        val intent = Intent(requireContext(), SongControlsActivity::class.java)
        intent.putExtra("songTitle", item.title)
        intent.putExtra("lyrics", lyrics.toTypedArray())
        startActivity(intent)
    }

    private fun selectSong(item: SongItem, holder: SongItemsAdapter.SongViewHolder) {
        if (!item.isSelected) {
            selectionCount++
        } else {
            selectionCount--
        }

        var datasetChanged = false
        when (selectionCount) {
            0 -> {
                datasetChanged = true
                songItemsAdapter.showCheckBox = false

                showMenuActions(showDelete = false, showEdit = false)
            }
            1 -> {
                datasetChanged = true
                songItemsAdapter.showCheckBox = true

                showMenuActions()
            }
            2 -> showMenuActions(showEdit = false)
        }

        item.isSelected = !item.isSelected

        if (datasetChanged) {
            songItemsAdapter.notifyDataSetChanged()
        } else {
            holder.checkBox.isChecked = item.isSelected
        }
    }

    private fun editSelectedSong(): Boolean {
        val selectedSong = songItemsAdapter.songItems.first { songItem -> songItem.isSelected }

        val intent = Intent(requireContext(), SongEditorActivity::class.java)
        intent.putExtra("songTitle", selectedSong.title)
        startActivity(intent)

        resetSelection()

        return true
    }

    private fun deleteSelectedSongs(): Boolean {
        val selectedSongs = songItemsAdapter.songItems
            .filter { song -> song.isSelected }
            .map { song -> song.title }

        SongsContext.deleteSongs(selectedSongs)

        val remainingSongs = songItemsAdapter.songItems
            .filter { songItem -> !selectedSongs.contains(songItem.title) }

        songItemsAdapter.songItems.clear()
        songItemsAdapter.songItems.addAll(remainingSongs)

        resetSelection()

        return true
    }

    private fun resetSelection() {
        songItemsAdapter.showCheckBox = false
        songItemsAdapter.notifyDataSetChanged()
        selectionCount = 0

        if (!this::menu.isInitialized) {
            return
        }

        showMenuActions(showDelete = false, showEdit = false)
    }

    private fun showMenuActions(showDelete: Boolean = true, showEdit: Boolean = true) {
        if (!this::menu.isInitialized) {
            return
        }

        menu.findItem(R.id.menu_delete).isVisible = showDelete
        menu.findItem(R.id.menu_edit).isVisible = showEdit
    }
}
