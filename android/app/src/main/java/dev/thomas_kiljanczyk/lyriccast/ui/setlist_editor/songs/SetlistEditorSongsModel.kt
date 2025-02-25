/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.ui.setlist_editor.songs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.CategoriesRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SongsRepository
import dev.thomas_kiljanczyk.lyriccast.domain.models.CategoryItem
import dev.thomas_kiljanczyk.lyriccast.domain.models.SongItem
import dev.thomas_kiljanczyk.lyriccast.ui.shared.misc.SongItemFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class SetlistEditorSongsModel @Inject constructor(
    private val songsRepository: SongsRepository,
    private val categoriesRepository: CategoriesRepository
) : ViewModel() {

    private companion object {
        const val TAG = "SetlistEditorSongsModel"
    }

    private val _songs: MutableSharedFlow<List<SongItem>> = MutableSharedFlow(replay = 1)
    val songs: Flow<List<SongItem>> get() = _songs

    private var filteredSongs: List<SongItem> = listOf()
    private var allSongs: List<SongItem> = listOf()

    private var setlistSongIds: List<String> = listOf()

    private val _categories: MutableStateFlow<List<CategoryItem>> = MutableStateFlow(listOf())
    val categories: Flow<List<CategoryItem>> get() = _categories

    val searchValues get() = itemFilter.values

    private val itemFilter = SongItemFilter()

    private var initialized: Boolean = false

    fun init() {
        if (initialized) {
            return
        }
        initialized = true

        songsRepository.getAllSongs()
            .onEach {
                val songItems = it.map { song ->
                    SongItem(song, hasCheckbox = true, isSelected = song.id in setlistSongIds)
                }.sorted()

                allSongs = songItems
                emitSongs()
            }.flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)

        categoriesRepository.getAllCategories()
            .onEach {
                val categoryItems = it.map { category -> CategoryItem(category) }.sorted()
                _categories.value = categoryItems
            }.flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)

        searchValues.songTitleFlow
            .debounce(500)
            .onEach { emitSongs() }
            .launchIn(viewModelScope)

        searchValues.categoryIdFlow
            .onEach { emitSongs() }
            .launchIn(viewModelScope)

        searchValues.isSelectedFlow
            .onEach { emitSongs() }
            .launchIn(viewModelScope)
    }

    suspend fun updateSetlistSongIds(newSetlistSongIds: List<String>) {
        setlistSongIds = newSetlistSongIds

        withContext(Dispatchers.Default) {
            allSongs.forEach { it.isSelected = it.song.id in setlistSongIds }

            emitSongs()
        }
    }

    fun updatePresentation(presentation: Array<String>): List<String> {
        val selectedSongIds = allSongs
            .filter { it.isSelected }
            .map { it.song.id }
            .toSet()

        val setlistSongIdsSet = setlistSongIds.toSet()

        val removedSongIds = setlistSongIdsSet.filter { it !in selectedSongIds }
        val addedSongIds = selectedSongIds.filter { it !in setlistSongIdsSet }

        val newSetlistSongIds = setlistSongIds.filter { it !in removedSongIds } + addedSongIds

        val newPresentation: List<String> = presentation.filter { it in newSetlistSongIds }

        return newPresentation + addedSongIds
    }

    fun selectSong(songItem: SongItem): Int {
        songItem.isSelected = !songItem.isSelected
        return filteredSongs.indexOf(songItem)
    }

    private suspend fun emitSongs() = withContext(Dispatchers.Default) {
        Log.v(TAG, "Song filtering invoked")
        val duration = measureTimeMillis {
            filteredSongs = itemFilter.apply(allSongs).toList()
            _songs.emit(filteredSongs)
        }
        Log.v(TAG, "Filtering took : ${duration}ms")
    }
}