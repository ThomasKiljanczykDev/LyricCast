/*
 * Created by Tomasz Kiljańczyk on 10/12/20 10:37 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/12/20 10:37 PM
 */

package pl.gunock.lyriccast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.textfield.TextInputLayout
import pl.gunock.lyriccast.listeners.InputTextChangeListener
import pl.gunock.lyriccast.listeners.RecyclerItemClickListener

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SongListFragment : Fragment() {

    private var castContext: CastContext? = null

    private var searchView: TextInputLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_song_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        castContext = CastContext.getSharedInstance()
        searchView = view.findViewById(R.id.text_view_filter_songs)

        SongsContext.songsListAdapter = SongListAdapter(SongsContext.songsList)

        view.findViewById<RecyclerView>(R.id.recycler_view_songs).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SongsContext.songsListAdapter
        }

        setupListeners(view)

        if (SongsContext.songsList.isEmpty()) {
            SongsContext.loadSongsMetadata()
        }
    }

    private fun setupListeners(view: View) {
        view.findViewById<RecyclerView>(R.id.recycler_view_songs).addOnItemTouchListener(
            RecyclerItemClickListener(context) { _, position ->
                SongsContext.pickSong(position)
                findNavController().navigate(R.id.action_SongListFragment_to_ControlsFragment)
            })

        searchView!!.editText!!.addTextChangedListener(InputTextChangeListener {
            SongsContext.filterSongs(it)
        })
    }
}
