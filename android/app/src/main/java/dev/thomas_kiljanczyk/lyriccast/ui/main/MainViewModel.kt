/*
 * Created by Tomasz Kiljanczyk on 5/31/25, 2:51 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 5/31/25, 2:02 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.common.helpers.FileHelper
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.DatabaseTransferData
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.ImportOptions
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.DataTransferRepository
import dev.thomas_kiljanczyk.lyriccast.datatransfer.enums.SongXmlParserType
import dev.thomas_kiljanczyk.lyriccast.datatransfer.factories.ImportSongXmlParserFactory
import dev.thomas_kiljanczyk.lyriccast.datatransfer.models.CategoryDto
import dev.thomas_kiljanczyk.lyriccast.datatransfer.models.SetlistDto
import dev.thomas_kiljanczyk.lyriccast.datatransfer.models.SongDto
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.GmsNearbySessionServerContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataTransferRepository: DataTransferRepository,
    private val gmsNearbySessionServerContext: GmsNearbySessionServerContext
) : ViewModel() {
    private companion object {
        const val TAG = "MainViewModel"
    }

    val isSessionServerRunning: Boolean
        get() = gmsNearbySessionServerContext.serverIsRunning.value

    fun exportAll(
        cacheDir: String,
        outputStream: OutputStream,
    ): Flow<Int> = flow {
        val exportData = dataTransferRepository.getDatabaseTransferData()

        val exportDir = File(cacheDir, ".export")
        exportDir.deleteRecursively()
        exportDir.mkdirs()

        emit(R.string.main_activity_export_saving_json)
        val songsString = Json.encodeToString(exportData.songDtos)
        val categoriesString = Json.encodeToString(exportData.categoryDtos)
        val setlistsString = Json.encodeToString(exportData.setlistDtos)

        File(exportDir, "songs.json").writeText(songsString)
        File(exportDir, "categories.json").writeText(categoriesString)
        File(exportDir, "setlists.json").writeText(setlistsString)

        emit(R.string.main_activity_export_saving_zip)
        FileHelper.zip(outputStream, exportDir.path)

        emit(R.string.main_activity_export_deleting_temp)
        exportDir.deleteRecursively()
    }.flowOn(Dispatchers.Default)

    suspend fun importLyricCast(
        cacheDir: String, inputStream: InputStream, importOptions: ImportOptions
    ): Flow<Int>? {
        val transferData: DatabaseTransferData = getImportData(cacheDir, inputStream) ?: return null

        return dataTransferRepository.importData(transferData, importOptions)
    }

    suspend fun importOpenSong(
        cacheDir: String, inputStream: InputStream, importOptions: ImportOptions
    ): Flow<Int>? {
        val importDir = File(cacheDir)
        val importSongXmlParser =
            ImportSongXmlParserFactory.create(importDir, SongXmlParserType.OPEN_SONG)

        val importedSongs: Set<SongDto> = try {
            importSongXmlParser.parseZip(inputStream)
        } catch (exception: Exception) {
            Log.w(TAG, exception)
            null
        } ?: return null

        return dataTransferRepository.importSongs(importedSongs, importOptions)
    }

    private fun getImportData(
        cacheDir: String, inputStream: InputStream
    ): DatabaseTransferData? {
        val importDir = File(cacheDir, ".import")
        importDir.deleteRecursively()
        importDir.mkdirs()

        FileHelper.unzip(inputStream, importDir.path)

        try {
            val songsJson = File(importDir, "songs.json").readText()
            val categoriesJson = File(importDir, "categories.json").readText()

            val setlistsFile = File(importDir, "setlists.json")
            val setlistsJson: String? = if (setlistsFile.exists()) {
                File(importDir, "setlists.json").readText()
            } else {
                null
            }

            val songDtos = Json.decodeFromString<List<SongDto>>(songsJson)
            val categoryDtos = Json.decodeFromString<List<CategoryDto>>(categoriesJson)
            val setlistDtos = setlistsJson?.let {
                Json.decodeFromString<List<SetlistDto>>(it)
            }

            return DatabaseTransferData(
                songDtos = songDtos,
                categoryDtos = categoryDtos,
                setlistDtos = setlistDtos
            )
        } catch (exception: Exception) {
            Log.e(TAG, exception.stackTraceToString())
            return null
        } finally {
            importDir.deleteRecursively()
        }
    }
}