/*
 * Created by Tomasz Kiljańczyk on 2/27/21 4:17 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 1:04 PM
 */

package pl.gunock.lyriccast.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SetlistsContext
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.activities.SetlistControlsActivity
import pl.gunock.lyriccast.activities.SetlistEditorActivity
import pl.gunock.lyriccast.adapters.SetlistListAdapter
import pl.gunock.lyriccast.adapters.listeners.ClickAdapterListener
import pl.gunock.lyriccast.adapters.listeners.LongClickAdapterListener
import pl.gunock.lyriccast.listeners.InputTextChangeListener
import pl.gunock.lyriccast.listeners.SpinnerItemSelectedListener
import pl.gunock.lyriccast.models.SetlistItemModel
import java.util.*


class SetlistsFragment : Fragment() {

    private lateinit var menu: Menu
    private lateinit var searchViewEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var setlistRecyclerView: RecyclerView

    private var selectionCount: Int = 0

    private lateinit var setlistListAdapter: SetlistListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setlists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchView: TextInputLayout = view.findViewById(R.id.text_view_filter_setlists)
        searchViewEditText = searchView.editText!!
        categorySpinner = view.findViewById(R.id.spinner_setlist_category)

        setlistRecyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_setlists).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }

        setupListeners()

        if (SongsContext.categories.toList().isNotEmpty()) {
            setupCategorySpinner()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
        super.onCreateOptionsMenu(menu, inflater)

        val deleteActionItem = menu.findItem(R.id.action_delete)
        deleteActionItem.isVisible = false

        val editActionItem = menu.findItem(R.id.action_edit)
        editActionItem.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> deleteSelectedSetlists()
            R.id.action_edit -> editSelectedSetlist()
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()

        val onLongClickListener =
            LongClickAdapterListener { holder: SetlistListAdapter.SetlistViewHolder, position: Int, _ ->
                val item = setlistListAdapter.setlistItems[position]
                selectSetlist(item, holder)
                return@LongClickAdapterListener true
            }

        val onClickListener =
            ClickAdapterListener { holder: SetlistListAdapter.SetlistViewHolder, position: Int, _ ->
                val item = setlistListAdapter.setlistItems[position]
                if (selectionCount == 0) {
                    pickSetlist(item)
                } else {
                    selectSetlist(item, holder)
                }
            }

        SetlistsContext.setlistList = SetlistsContext.loadSetlists()
        SetlistsContext.setlistItemList = SetlistsContext.setlistList
            .map { setlist -> SetlistItemModel(setlist) }
            .toMutableList()

        setlistListAdapter = SetlistListAdapter(
            SetlistsContext.setlistItemList,
            onLongClickListener = onLongClickListener,
            onClickListener = onClickListener
        )
        setlistListAdapter.setlistItems = SetlistsContext.setlistItemList

        requireView()
            .findViewById<RecyclerView>(R.id.recycler_view_setlists)!!.adapter = setlistListAdapter

        setupCategorySpinner()

        searchViewEditText.setText("")
        categorySpinner.setSelection(0)
    }

    private fun setupCategorySpinner() {
        val categorySpinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            SongsContext.categories.toList()
        )
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categorySpinnerAdapter
    }

    private fun setupListeners() {
        searchViewEditText.addTextChangedListener(InputTextChangeListener { newText ->
            filterSetlists(newText, categorySpinner.selectedItem.toString())
        })

        categorySpinner.onItemSelectedListener = SpinnerItemSelectedListener { _, _ ->
            filterSetlists(
                searchViewEditText.editableText.toString(),
                categorySpinner.selectedItem.toString()
            )
        }
    }

    private fun pickSetlist(item: SetlistItemModel) {
        val intent = Intent(context, SetlistControlsActivity::class.java)
        intent.putExtra("setlistName", item.name)
        startActivity(intent)
    }

    private fun selectSetlist(
        item: SetlistItemModel,
        holder: SetlistListAdapter.SetlistViewHolder
    ) {
        if (!item.isSelected) {
            selectionCount++
        } else {
            selectionCount--
        }

        var datasetChanged = false
        when (selectionCount) {
            0 -> {
                datasetChanged = true
                setlistListAdapter.showCheckBox = false

                val deleteActionItem = menu.findItem(R.id.action_delete)
                deleteActionItem.isVisible = false

                val editActionItem = menu.findItem(R.id.action_edit)
                editActionItem.isVisible = false
            }
            1 -> {
                datasetChanged = true
                setlistListAdapter.showCheckBox = true

                val deleteActionItem = menu.findItem(R.id.action_delete)
                deleteActionItem.isVisible = true

                val editActionItem = menu.findItem(R.id.action_edit)
                editActionItem.isVisible = true
            }
            2 -> {
                val deleteActionItem = menu.findItem(R.id.action_delete)
                deleteActionItem.isVisible = true

                val editActionItem = menu.findItem(R.id.action_edit)
                editActionItem.isVisible = false
            }
        }

        item.isSelected = !item.isSelected

        if (datasetChanged) {
            setlistListAdapter.notifyDataSetChanged()
        } else {
            holder.checkBox.isChecked = item.isSelected
        }

    }

    private fun editSelectedSetlist(): Boolean {
        val selectedSetlist = setlistListAdapter.setlistItems
            .first { setlistItem -> setlistItem.isSelected }

        val intent = Intent(requireContext(), SetlistEditorActivity::class.java)
        intent.putExtra("setlistName", selectedSetlist.name)
        startActivity(intent)

        setlistListAdapter.showCheckBox = false
        setlistListAdapter.notifyDataSetChanged()
        selectionCount = 0

        return true
    }

    private fun deleteSelectedSetlists(): Boolean {
        val selectedSetlists = setlistListAdapter.setlistItems
            .filter { setlist -> setlist.isSelected }
            .map { setlist -> setlist.name }

        SetlistsContext.deleteSetlists(selectedSetlists)

        val remainingSetlists = setlistListAdapter.setlistItems
            .filter { setlistItem -> !selectedSetlists.contains(setlistItem.name) }
        setlistListAdapter.showCheckBox = false

        setlistListAdapter.setlistItems.clear()
        setlistListAdapter.setlistItems.addAll(remainingSetlists)
        setlistListAdapter.notifyDataSetChanged()

        selectionCount = 0

        return true
    }

    private fun filterSetlists(name: String, category: String = "All") {
        setlistListAdapter.setlistItems = SetlistsContext.setlistItemList.filter { setlist ->
            setlist.name.contains(name, ignoreCase = true)
                    && (category == "All" || setlist.category == category)
        }.toMutableList()
        setlistListAdapter.notifyDataSetChanged()
    }
}
