/*
 * Created by Tomasz Kiljanczyk on 06/01/2025, 01:11
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 06/01/2025, 00:24
 */

package dev.thomas_kiljanczyk.lyriccast.ui.main.setlists

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.common.helpers.FileHelper
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.DatabaseTransferData
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.DataTransferRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SetlistsRepository
import dev.thomas_kiljanczyk.lyriccast.domain.models.SetlistItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.OutputStream
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class SetlistsModel @Inject constructor(
    private val setlistsRepository: SetlistsRepository,
    private val dataTransferRepository: DataTransferRepository
) : ViewModel() {
    private companion object {
        const val TAG = "SetlistsViewModel"
    }

    private val _setlists: MutableSharedFlow<List<SetlistItem>> = MutableSharedFlow(replay = 1)

    val setlists: Flow<List<SetlistItem>>
        get() = _setlists


    private var _filteredSetlists: List<SetlistItem> = listOf()
    val filteredSetlists: List<SetlistItem>
        get() = _filteredSetlists

    private var allSetlists: List<SetlistItem> = listOf()

    val searchValues get() = itemFilter.values

    private val itemFilter = SetlistItemFilter()

    init {
        setlistsRepository.getAllSetlists().onEach { setlists ->
            val setlistItems = setlists.map { SetlistItem(it) }.sorted()

            allSetlists = setlistItems
            emitSetlists()
        }.flowOn(Dispatchers.Default).launchIn(viewModelScope)

        searchValues.setlistNameFlow.debounce(500).onEach { emitSetlists() }
            .launchIn(viewModelScope)
    }

    suspend fun deleteSelectedSetlists() {
        val selectedSetlists =
            allSetlists.filter { item -> item.isSelected }.map { item -> item.setlist.id }
        setlistsRepository.deleteSetlists(selectedSetlists)
    }

    fun hideSelectionCheckboxes() {
        allSetlists.forEach {
            it.hasCheckbox = false
            it.isSelected = false
        }
    }

    fun showSelectionCheckboxes() {
        allSetlists.forEach { it.hasCheckbox = true }
    }

    fun exportSelectedSetlists(
        cacheDir: String, outputStream: OutputStream
    ): Flow<Int> = flow {
        val exportData: DatabaseTransferData =
            dataTransferRepository.getDatabaseTransferData()

        val exportDir = File(cacheDir, ".export")
        exportDir.deleteRecursively()
        exportDir.mkdirs()

        val selectedSetlists = allSetlists.filter { it.isSelected }

        val setlistNames: Set<String> = selectedSetlists.map { it.setlist.name }.toSet()

        val exportSetlists = exportData.setlistDtos!!.filter { it.name in setlistNames }

        val songTitles: Set<String> = exportSetlists.flatMap { it.songs }.toSet()
        val categoryNames: Set<String> =
            exportData.songDtos!!.filter { it.title in songTitles }.mapNotNull { it.category }
                .toSet()

        val filteredSongDtos = exportData.songDtos!!.filter { it.title in songTitles }
        val filteredCategoryDtos = exportData.categoryDtos!!.filter { it.name in categoryNames }

        emit(R.string.main_activity_export_saving_json)

        val songsString = Json.encodeToString(filteredSongDtos)
        val categoriesString = Json.encodeToString(filteredCategoryDtos)
        val setlistsString = Json.encodeToString(exportSetlists)
        File(exportDir, "songs.json").writeText(songsString)
        File(exportDir, "categories.json").writeText(categoriesString)
        File(exportDir, "setlists.json").writeText(setlistsString)

        emit(R.string.main_activity_export_saving_zip)
        withContext(Dispatchers.IO) {
            FileHelper.zip(outputStream, exportDir.path)
        }

        emit(R.string.main_activity_export_deleting_temp)
        exportDir.deleteRecursively()
        hideSelectionCheckboxes()
    }.flowOn(Dispatchers.Default)

    fun selectSetlist(setlistId: Long, selected: Boolean): Boolean {
        val setlist = allSetlists.firstOrNull { it.setlist.idLong == setlistId } ?: return false

        setlist.isSelected = selected
        return true
    }

    private suspend fun emitSetlists() = withContext(Dispatchers.Default) {
        Log.v(TAG, "Setlist filtering invoked")
        val duration = measureTimeMillis {
            _filteredSetlists = itemFilter.apply(allSetlists).toList()
            _setlists.emit(_filteredSetlists)
        }
        Log.v(TAG, "Filtering took : ${duration}ms")
    }

}