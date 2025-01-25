/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 10/01/2025, 01:46
 */

package dev.thomas_kiljanczyk.lyriccast.modules

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.nearby.connection.ConnectionsClient
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import dev.thomas_kiljanczyk.lyriccast.application.AppSettings
import dev.thomas_kiljanczyk.lyriccast.application.AppSettingsSerializer
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.CategoriesRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.DataTransferRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SetlistsRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SongsRepository
import dev.thomas_kiljanczyk.lyriccast.di.AppModule
import dev.thomas_kiljanczyk.lyriccast.repositories.CategoriesRepositoryFakeImpl
import dev.thomas_kiljanczyk.lyriccast.repositories.DataTransferRepositoryFakeImpl
import dev.thomas_kiljanczyk.lyriccast.repositories.SetlistsRepositoryFakeImpl
import dev.thomas_kiljanczyk.lyriccast.repositories.SongsRepositoryFakeImpl
import dev.thomas_kiljanczyk.lyriccast.shared.cast.CastMessagingContext
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.ConnectionsClientFakeImpl
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.GmsNearbySessionServerContext
import dev.thomas_kiljanczyk.lyriccast.shared.misc.LyricCastMessagingContext
import java.io.File
import java.util.UUID
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
class FakeAppModule {

    companion object {
        private const val TEST_DATASTORE_FILENAME = "settings"

        var dataStore: DataStore<AppSettings>? = null
        var dataStoreFile: File? = null

        fun initializeDataStore(appContext: Context) {
            if (dataStoreFile == null) {
                cleanupDataStore()
            }

            val newDataStoreFile =
                appContext.dataStoreFile("$TEST_DATASTORE_FILENAME-${UUID.randomUUID()}")
            dataStoreFile = newDataStoreFile

            dataStore = DataStoreFactory.create(
                produceFile = { newDataStoreFile },
                serializer = AppSettingsSerializer
            )
        }

        fun cleanupDataStore() {
            dataStoreFile?.delete()
            dataStoreFile = null
            dataStore = null
        }
    }

    @Provides
    fun provideCastContext(@ApplicationContext context: Context): CastContext {
        return CastContext.getSharedInstance(context)
    }

    @Provides
    fun provideConnectionsClient(): ConnectionsClient {
        return ConnectionsClientFakeImpl()
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext appContext: Context): DataStore<AppSettings> {
        if (dataStore == null) {
            initializeDataStore(appContext)
        }

        return dataStore!!
    }

    @Provides
    @Singleton
    fun provideDataTransferRepository(): DataTransferRepository =
        DataTransferRepositoryFakeImpl(
            provideSongsRepository(),
            provideSetlistsRepository(),
            provideCategoriesRepository()
        )

    @Provides
    @Singleton
    fun provideSongsRepository(): SongsRepository = SongsRepositoryFakeImpl()

    @Provides
    @Singleton
    fun provideSetlistsRepository(): SetlistsRepository = SetlistsRepositoryFakeImpl()

    @Provides
    @Singleton
    fun provideCategoriesRepository(): CategoriesRepository = CategoriesRepositoryFakeImpl()

    @Provides
    @Singleton
    fun provideCastMessagingContext(castContext: CastContext): CastMessagingContext {
        return CastMessagingContext(castContext)
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