/*
 * Created by Tomasz Kiljanczyk on 06/01/2025, 01:11
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 06/01/2025, 00:34
 */

package dev.thomas_kiljanczyk.lyriccast.datamodel

import dev.thomas_kiljanczyk.lyriccast.datamodel.models.mongo.CategoryDocument
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.mongo.SetlistDocument
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.mongo.SongDocument
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.mongo.embedded.LyricsSectionDocument
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.CategoriesRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.DataTransferRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SetlistsRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SongsRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.impl.mongo.CategoriesRepositoryMongoImpl
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.impl.mongo.DataTransferRepositoryMongoImpl
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.impl.mongo.SetlistsRepositoryMongoImpl
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.impl.mongo.SongsRepositoryMongoImpl
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RepositoryFactory {
    private val schema = setOf(
        LyricsSectionDocument::class,
        CategoryDocument::class,
        SongDocument::class,
        SetlistDocument::class,
    )

    private lateinit var realm: Realm

    fun createSongsRepository(provider: RepositoryProvider): SongsRepository {
        return when (provider) {
            RepositoryProvider.MONGO -> {
                SongsRepositoryMongoImpl(realm)
            }
        }
    }

    fun createSetlistsRepository(provider: RepositoryProvider): SetlistsRepository {
        return when (provider) {
            RepositoryProvider.MONGO -> {
                SetlistsRepositoryMongoImpl(realm)
            }
        }
    }

    fun createCategoriesRepository(provider: RepositoryProvider): CategoriesRepository {
        return when (provider) {
            RepositoryProvider.MONGO -> {
                CategoriesRepositoryMongoImpl(realm)
            }
        }
    }

    fun createDataTransferRepository(
        provider: RepositoryProvider
    ): DataTransferRepository {
        return when (provider) {
            RepositoryProvider.MONGO -> {
                DataTransferRepositoryMongoImpl(realm)
            }
        }
    }

    suspend fun initializeMongoDbRealm() = withContext(Dispatchers.IO) {
        val realmConfiguration = RealmConfiguration.Builder(schema).build()
        val realm = Realm.open(realmConfiguration)
        this@RepositoryFactory.realm = realm
    }

    enum class RepositoryProvider {
        MONGO
    }

}