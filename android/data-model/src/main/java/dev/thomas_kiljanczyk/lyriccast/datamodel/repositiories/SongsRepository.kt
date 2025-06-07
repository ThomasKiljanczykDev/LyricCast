/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories

import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Song
import kotlinx.coroutines.flow.Flow

interface SongsRepository {

    fun getAllSongs(): Flow<List<Song>>

    fun getSong(id: String): Song?

    suspend fun upsertSong(song: Song)

    suspend fun deleteSongs(songIds: Collection<String>)

}