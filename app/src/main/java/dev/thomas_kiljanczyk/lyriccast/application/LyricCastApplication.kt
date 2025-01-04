/*
 * Created by Tomasz Kiljanczyk on 04/01/2025, 16:41
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 03/01/2025, 00:53
 */

package dev.thomas_kiljanczyk.lyriccast.application

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import dev.thomas_kiljanczyk.lyriccast.shared.cast.CastMessagingContext
import dev.thomas_kiljanczyk.lyriccast.shared.cast.CastSessionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltAndroidApp
class LyricCastApplication : Application() {

    companion object {
        val PERMISSIONS = preparePermissionArray()

        private fun preparePermissionArray(): Array<String> {
            val result = mutableListOf(
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
            )

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                result.add(Manifest.permission.BLUETOOTH)
                result.add(Manifest.permission.BLUETOOTH_ADMIN)
            }

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                result.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                result.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                result.add(Manifest.permission.BLUETOOTH_ADVERTISE)
                result.add(Manifest.permission.BLUETOOTH_CONNECT)
                result.add(Manifest.permission.BLUETOOTH_SCAN)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }

            return result.toTypedArray()
        }
    }

    @Inject
    lateinit var dataStore: DataStore<AppSettings>

    @Inject
    lateinit var castMessagingContext: CastMessagingContext

    @SuppressLint("WrongConstant")
    override fun onCreate() {
        super.onCreate()

        // Initializes CastContext
        CastContext.getSharedInstance(applicationContext, Executors.newSingleThreadExecutor())
            .addOnCompleteListener { task ->
                val listener = CastSessionListener(
                    onStarted = {
                        CoroutineScope(Dispatchers.Default).launch {
                            val blankOnStart = dataStore.data.first().blankOnStart
                            castMessagingContext.sendBlank(blankOnStart)
                        }
                    },
                    onEnded = { castMessagingContext.onSessionEnded() }
                )

                CoroutineScope(Dispatchers.Main).launch {
                    task.result.sessionManager.addSessionManagerListener(listener)
                }
            }


        DynamicColors.applyToActivitiesIfAvailable(this)

        // TODO: Add color harmonization

        dataStore.data
            .onEach {
                var appTheme: Int? = it.appTheme
                appTheme = if (appTheme == 0) null else appTheme
                if (appTheme != null) {
                    AppCompatDelegate.setDefaultNightMode(appTheme)
                }
            }.launchIn(CoroutineScope(Dispatchers.Main))


        val isDebuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        if (isDebuggable) {
            setupStrictMode()
        }
    }

    private fun setupStrictMode() {
        val threadPolicy = StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .permitCustomSlowCalls()
            .penaltyLog()
            .penaltyDialog()
            .build()

        StrictMode.setThreadPolicy(threadPolicy)

        val vmPolicy = StrictMode.VmPolicy.Builder()
            .detectActivityLeaks()
            .detectFileUriExposure()
            .penaltyLog()
            .build()

        StrictMode.setVmPolicy(vmPolicy)
    }
}