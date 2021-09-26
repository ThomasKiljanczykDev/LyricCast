/*
 * Created by Tomasz Kiljanczyk on 26/09/2021, 17:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 26/09/2021, 17:19
 */

package pl.gunock.lyriccast.ui.main.songs

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class SongsViewModel @Inject constructor(
    val categoriesRepository: CategoriesRepository,
    private val songsRepository: SongsRepository,
    val dataTransferRepository: DataTransferRepository
) : ViewModel() {
    private companion object {
        const val TAG: String = "SongsViewModel"
    }

    val songs: LiveData<List<SongItem>>
        get() = _songs

    private val _songs: MutableLiveData<List<SongItem>> = MutableLiveData(listOf())

    val pickedItem: LiveData<SongItem>
        get() = _pickedItem

    private val _pickedItem: MutableLiveData<SongItem> = MutableLiveData()


    val numberOfSelectedItems: LiveData<Int>
        get() = _numberOfSelectedItems

    private val _numberOfSelectedItems: MutableLiveData<Int> = MutableLiveData(0)


    private var allSongs: Iterable<SongItem> = listOf()

    val selectionTracker: SelectionTracker<BaseViewHolder> =
        SelectionTracker(this::onItemSelection)

    private var songsSubscription: Disposable? = null

    init {
        songsSubscription = songsRepository.getAllSongs()
            .subscribe {
                viewModelScope.launch(Dispatchers.Default) {
                    val songItems = it.map { song -> SongItem(song) }
                    allSongs = songItems
                    _songs.postValue(songItems)
                }
            }
    }

    override fun onCleared() {
        songsSubscription?.dispose()
        super.onCleared()
    }

    fun deleteSelected() {
        val selectedSongs = allSongs.filter { item -> item.isSelected.value!! }
            .map { item -> item.song.id }

        songsRepository.deleteSongs(selectedSongs)
    }

    // TODO: Move filter to separate class (functional interface?)
    suspend fun filter(
        songTitle: String,
        categoryId: String? = null,
        isSelected: Boolean? = null
    ) {
        withContext(Dispatchers.Default) {
            val predicates: MutableList<(SongItem) -> Boolean> = mutableListOf()

            if (isSelected != null) {
                predicates.add { songItem -> songItem.isSelected.value!! }
            }

            if (!categoryId.isNullOrBlank()) {
                predicates.add { songItem -> songItem.song.category?.id == categoryId }
            }

            val normalizedTitle = songTitle.trim().normalize()
            predicates.add { item ->
                item.normalizedTitle.contains(normalizedTitle, ignoreCase = true)
            }

            val duration = measureTimeMillis {
                val filteredItems = allSongs.filter { songItem ->
                    predicates.all { predicate -> predicate(songItem) }
                }.toSortedSet().toList()

                _songs.postValue(filteredItems)
            }
            Log.v(TAG, "Filtering took : ${duration}ms")
        }
    }

    private fun onItemSelection(
        @Suppress("UNUSED_PARAMETER")
        holder: BaseViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        val item = _songs.value!![position]

        if (!isLongClick && selectionTracker.count == 0) {
            _pickedItem.postValue(item)
        } else {
            item.isSelected.postValue(!item.isSelected.value!!)
            _numberOfSelectedItems.postValue(selectionTracker.countAfter)
        }

        return true
    }

    fun resetSelection() {
        _songs.value?.forEach { it.isSelected.postValue(false) }
        selectionTracker.reset()
    }

}