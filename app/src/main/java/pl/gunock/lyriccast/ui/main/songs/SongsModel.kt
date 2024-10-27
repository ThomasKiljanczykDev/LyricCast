/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 19:17
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 19:15
 */

package pl.gunock.lyriccast.ui.main.songs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import org.json.JSONArray
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.common.helpers.FileHelper
import pl.gunock.lyriccast.datamodel.models.DatabaseTransferData
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.domain.models.CategoryItem
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.ui.shared.misc.SongItemFilter
import java.io.File
import java.io.OutputStream
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class SongsModel @Inject constructor(
    categoriesRepository: CategoriesRepository,
    private val songsRepository: SongsRepository,
    private val dataTransferRepository: DataTransferRepository
) : ViewModel() {
    private companion object {
        const val TAG = "SongsViewModel"
    }

    private val _songs: MutableSharedFlow<List<SongItem>> = MutableSharedFlow(replay = 1)
    val songs: Flow<List<SongItem>> = _songs

    private var filteredSongs: List<SongItem> = listOf()
    private var allSongs: List<SongItem> = listOf()

    private val _categories: MutableSharedFlow<List<CategoryItem>> = MutableSharedFlow(replay = 1)
    val categories: Flow<List<CategoryItem>> = _categories

    val searchValues get() = itemFilter.values

    private val itemFilter = SongItemFilter()

    init {
        songsRepository.getAllSongs()
            .onEach {
                val songItems = it.map { song -> SongItem(song) }.sorted()

                allSongs = songItems
                emitSongs()
            }.flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)

        categoriesRepository.getAllCategories()
            .onEach {
                val categoryItems = it.map { category -> CategoryItem(category) }.sorted()
                _categories.emit(categoryItems)
            }.flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)

        searchValues.songTitleFlow
            .debounce(500)
            .onEach { emitSongs() }
            .launchIn(viewModelScope)

        searchValues.categoryIdFlow
            .onEach { emitSongs() }
            .launchIn(viewModelScope)
    }

    fun getSelectedSong(): SongItem {
        return allSongs.first { songItem -> songItem.isSelected }
    }

    fun getSelectedSongIds(): List<String> {
        return allSongs
            .filter { it.isSelected }
            .map { it.song.id }
    }

    suspend fun deleteSelectedSongs() {
        val selectedSongs = allSongs.filter { it.isSelected }
            .map { item -> item.song.id }

        songsRepository.deleteSongs(selectedSongs)
    }

    fun hideSelectionCheckboxes() {
        allSongs.forEach {
            it.hasCheckbox = false
            it.isSelected = false
        }
    }

    fun showSelectionCheckboxes() {
        allSongs.forEach { it.hasCheckbox = true }
    }

    fun exportSongs(
        cacheDir: String,
        outputStream: OutputStream
    ): Flow<Int> = flow {
        val exportData: DatabaseTransferData = dataTransferRepository.getDatabaseTransferData()

        val exportDir = File(cacheDir, ".export")
        exportDir.deleteRecursively()
        exportDir.mkdirs()

        val selectedSongs = allSongs.filter { it.isSelected }

        val songTitles: Set<String> = selectedSongs.map { it.song.title }.toSet()
        val categoryNames: Set<String> =
            selectedSongs.mapNotNull { it.song.category?.name }.toSet()


        val songJsons = exportData.songDtos!!
            .filter { it.title in songTitles }
            .map { it.toJson() }

        val categoryJsons = exportData.categoryDtos!!
            .filter { it.name in categoryNames }
            .map { it.toJson() }

        emit(R.string.main_activity_export_saving_json)

        val songsString = JSONArray(songJsons).toString()
        val categoriesString = JSONArray(categoryJsons).toString()
        File(exportDir, "songs.json").writeText(songsString)
        File(exportDir, "categories.json").writeText(categoriesString)

        emit(R.string.main_activity_export_saving_zip)
        FileHelper.zip(outputStream, exportDir.path)

        emit(R.string.main_activity_export_deleting_temp)
        exportDir.deleteRecursively()
    }.flowOn(Dispatchers.Default)

    fun selectSong(songId: Long, selected: Boolean): Boolean {
        val song = filteredSongs
            .firstOrNull { it.song.idLong == songId } ?: return false

        song.isSelected = selected
        return true
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
