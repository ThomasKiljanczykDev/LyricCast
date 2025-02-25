/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 25/01/2025, 18:55
 */

package dev.thomas_kiljanczyk.lyriccast.ui.main.songs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.common.helpers.FileHelper
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.DatabaseTransferData
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.CategoriesRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.DataTransferRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SongsRepository
import dev.thomas_kiljanczyk.lyriccast.domain.models.CategoryItem
import dev.thomas_kiljanczyk.lyriccast.domain.models.SongItem
import dev.thomas_kiljanczyk.lyriccast.ui.shared.misc.SongItemFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
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
        songsRepository.getAllSongs().onEach {
            val songItems = it.map { song -> SongItem(song) }.sorted()

            allSongs = songItems
            emitSongs()
        }.flowOn(Dispatchers.Default).launchIn(viewModelScope)

        categoriesRepository.getAllCategories().onEach {
            val categoryItems = it.map { category -> CategoryItem(category) }.sorted()
            _categories.emit(categoryItems)
        }.flowOn(Dispatchers.Default).launchIn(viewModelScope)

        searchValues.songTitleFlow.debounce(500).onEach { emitSongs() }.launchIn(viewModelScope)

        searchValues.categoryIdFlow.onEach { emitSongs() }.launchIn(viewModelScope)
    }

    fun getSelectedSong(): SongItem {
        return allSongs.first { songItem -> songItem.isSelected }
    }

    fun getSelectedSongIds(): List<String> {
        return allSongs.filter { it.isSelected }.map { it.song.id }
    }

    suspend fun deleteSelectedSongs() {
        val selectedSongs = allSongs.filter { it.isSelected }.map { item -> item.song.id }

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
        cacheDir: String, outputStream: OutputStream
    ): Flow<Int> = flow {
        val exportData: DatabaseTransferData = dataTransferRepository.getDatabaseTransferData()

        val exportDir = File(cacheDir, ".export")
        exportDir.deleteRecursively()
        exportDir.mkdirs()

        val selectedSongs = allSongs.filter { it.isSelected }

        val songTitles: Set<String> = selectedSongs.map { it.song.title }.toSet()
        val categoryNames: Set<String> = selectedSongs.mapNotNull { it.song.category?.name }.toSet()

        val filteredSongDtos = exportData.songDtos!!.filter { it.title in songTitles }
        val filteredCategoryDtos = exportData.categoryDtos!!.filter { it.name in categoryNames }

        emit(R.string.main_activity_export_saving_json)

        val songsString = Json.encodeToString(filteredSongDtos)
        val categoriesString = Json.encodeToString(filteredCategoryDtos)
        File(exportDir, "songs.json").writeText(songsString)
        File(exportDir, "categories.json").writeText(categoriesString)

        emit(R.string.main_activity_export_saving_zip)
        FileHelper.zip(outputStream, exportDir.path)

        emit(R.string.main_activity_export_deleting_temp)
        exportDir.deleteRecursively()
    }.flowOn(Dispatchers.Default)

    fun selectSong(songId: Long, selected: Boolean): Boolean {
        val song = filteredSongs.firstOrNull { it.song.idLong == songId } ?: return false

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
