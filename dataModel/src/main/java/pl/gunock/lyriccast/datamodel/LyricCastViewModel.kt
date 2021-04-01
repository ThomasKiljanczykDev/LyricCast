/*
 * Created by Tomasz Kiljanczyk on 4/1/21 8:54 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/1/21 8:45 PM
 */

package pl.gunock.lyriccast.datamodel

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pl.gunock.lyriccast.datamodel.entities.Category
import pl.gunock.lyriccast.datamodel.entities.Setlist
import pl.gunock.lyriccast.datamodel.entities.relations.SetlistWithSongs
import pl.gunock.lyriccast.datamodel.entities.relations.SongAndCategory
import pl.gunock.lyriccast.datamodel.entities.relations.SongWithLyricsSections


class LyricCastViewModel(
    private val repository: LyricCastRepository
) : ViewModel() {
    val allSongs: LiveData<List<SongAndCategory>> = repository.allSongs.asLiveData()
    val allSetlists: LiveData<List<Setlist>> = repository.allSetlists.asLiveData()
    val allCategories: LiveData<List<Category>> = repository.allCategories.asLiveData()

    fun upsertSong(song: SongWithLyricsSections, order: List<Pair<String, Int>>) =
        viewModelScope.launch { repository.upsertSong(song, order) }

    fun deleteSongs(songIds: List<Long>) =
        viewModelScope.launch { repository.deleteSongs(songIds) }

    fun upsertSetlist(setlist: SetlistWithSongs) =
        viewModelScope.launch { repository.upsertSetlist(setlist) }

    fun deleteSetlists(setlistIds: List<Long>) =
        viewModelScope.launch { repository.deleteSetlists(setlistIds) }

    fun upsertCategories(categories: Collection<Category>): List<Long> =
        runBlocking { repository.upsertCategories(categories) }

    fun deleteCategories(categoryIds: Collection<Long>) =
        viewModelScope.launch { repository.deleteCategories(categoryIds) }
}

class LyricCastViewModelFactory(
    private val repository: LyricCastRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LyricCastViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LyricCastViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}