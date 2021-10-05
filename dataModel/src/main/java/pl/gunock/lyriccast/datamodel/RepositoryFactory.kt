/*
 * Created by Tomasz Kiljanczyk on 03/10/2021, 22:40
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 03/10/2021, 22:32
 */

package pl.gunock.lyriccast.datamodel

import android.content.Context
import io.realm.Realm
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.datamodel.repositiories.impl.mongo.CategoriesRepositoryMongoImpl
import pl.gunock.lyriccast.datamodel.repositiories.impl.mongo.DataTransferRepositoryMongoImpl
import pl.gunock.lyriccast.datamodel.repositiories.impl.mongo.SetlistsRepositoryMongoImpl
import pl.gunock.lyriccast.datamodel.repositiories.impl.mongo.SongsRepositoryMongoImpl

object RepositoryFactory {

    fun initialize(context: Context, provider: RepositoryProvider) {
        return when (provider) {
            RepositoryProvider.MONGO -> Realm.init(context)
        }
    }

    fun createSongsRepository(provider: RepositoryProvider): SongsRepository {
        return when (provider) {
            RepositoryProvider.MONGO -> SongsRepositoryMongoImpl()
        }
    }

    fun createSetlistsRepository(provider: RepositoryProvider): SetlistsRepository {
        return when (provider) {
            RepositoryProvider.MONGO -> SetlistsRepositoryMongoImpl()
        }
    }

    fun createCategoriesRepository(provider: RepositoryProvider): CategoriesRepository {
        return when (provider) {
            RepositoryProvider.MONGO -> CategoriesRepositoryMongoImpl()
        }
    }

    fun createDataTransferRepository(
        provider: RepositoryProvider
    ): DataTransferRepository {
        return when (provider) {
            RepositoryProvider.MONGO -> DataTransferRepositoryMongoImpl()
        }
    }


    enum class RepositoryProvider {
        MONGO
    }

}