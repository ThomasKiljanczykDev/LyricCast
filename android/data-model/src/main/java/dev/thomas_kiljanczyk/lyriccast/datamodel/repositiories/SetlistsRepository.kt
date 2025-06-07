/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories

import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Setlist
import kotlinx.coroutines.flow.Flow

interface SetlistsRepository {

    fun getAllSetlists(): Flow<List<Setlist>>

    fun getSetlist(id: String): Setlist?

    suspend fun upsertSetlist(setlist: Setlist)

    suspend fun deleteSetlists(setlistIds: Collection<String>)

}