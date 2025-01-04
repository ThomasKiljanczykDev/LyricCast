/*
 * Created by Tomasz Kiljanczyk on 04/01/2025, 16:41
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 04/01/2025, 16:41
 */

package dev.thomas_kiljanczyk.lyriccast.di

import android.content.Context
import androidx.datastore.core.DataStore
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionsClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.thomas_kiljanczyk.lyriccast.application.AppSettings
import dev.thomas_kiljanczyk.lyriccast.application.settingsDataStore
import dev.thomas_kiljanczyk.lyriccast.datamodel.RepositoryFactory
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.CategoriesRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.DataTransferRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SetlistsRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SongsRepository
import dev.thomas_kiljanczyk.lyriccast.shared.cast.CastMessagingContext
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.GmsNearbySessionServerContext
import dev.thomas_kiljanczyk.lyriccast.shared.misc.LyricCastMessagingContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideConnectionsClient(@ApplicationContext context: Context): ConnectionsClient {
        return Nearby.getConnectionsClient(context)
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext appContext: Context): DataStore<AppSettings> {
        return appContext.settingsDataStore
    }

    @Provides
    @Singleton
    fun provideDataTransferRepository(): DataTransferRepository {
        return RepositoryFactory.createDataTransferRepository(
            RepositoryFactory.RepositoryProvider.MONGO
        )
    }

    @Provides
    @Singleton
    fun provideSongsRepository(): SongsRepository {
        return RepositoryFactory.createSongsRepository(
            RepositoryFactory.RepositoryProvider.MONGO
        )
    }

    @Provides
    @Singleton
    fun provideSetlistsRepository(): SetlistsRepository {
        return RepositoryFactory.createSetlistsRepository(
            RepositoryFactory.RepositoryProvider.MONGO
        )
    }

    @Provides
    @Singleton
    fun provideCategoriesRepository(): CategoriesRepository {
        return RepositoryFactory.createCategoriesRepository(
            RepositoryFactory.RepositoryProvider.MONGO
        )
    }

    @Provides
    @Singleton
    fun provideCastMessagingContext(): CastMessagingContext {
        return CastMessagingContext()
    }

    @Provides
    @Singleton
    fun provideGmsNearbyServerContext(
        connectionsClient: ConnectionsClient
    ): GmsNearbySessionServerContext {
        return GmsNearbySessionServerContext(connectionsClient)
    }

    @Provides
    @Singleton
    fun provideLyricCastMessagingContext(
        castMessagingContext: CastMessagingContext,
        gmsNearbySessionServerContext: GmsNearbySessionServerContext
    ): LyricCastMessagingContext {
        return LyricCastMessagingContext(castMessagingContext, gmsNearbySessionServerContext)
    }
}