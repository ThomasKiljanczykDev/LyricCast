/*
 * Created by Tomasz Kiljańczyk on 2/25/21 10:00 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/25/21 9:31 PM
 */

package pl.gunock.lyriccast.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SetlistsContext
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.listeners.TabItemSelectedListener
import pl.gunock.lyriccast.utils.FileHelper
import pl.gunock.lyriccast.utils.MessageHelper
import java.io.File

class MainActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "MainActivity"
        const val EXPORT_SONGS_RESULT_CODE = 1
        const val EXPORT_SETLISTS_RESULT_CODE = 2
        const val SELECT_FILE_RESULT_CODE = 3
    }

    private var castContext: CastContext? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        MessageHelper.initialize(applicationContext)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        val wifiManager = baseContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            val turnWifiOn = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(turnWifiOn)
        }

        setUpListeners()

        SongsContext.songsDirectory = "${filesDir.path}/songs/"
        SetlistsContext.setlistsDirectory = "${filesDir.path}/setlists/"

        castContext = CastContext.getSharedInstance(this)
    }

    override fun onStart() {
        super.onStart()

        findViewById<LinearLayout>(R.id.fab_view_add_song).visibility = View.GONE
        findViewById<LinearLayout>(R.id.fab_view_add_setlist).visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        CastButtonFactory.setUpMediaRouteButton(
            baseContext,
            menu,
            R.id.menu_cast
        )

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> goToSettings()
            R.id.menu_import_songs -> importSongs()
            R.id.menu_export_songs -> exportFiles(EXPORT_SONGS_RESULT_CODE)
            R.id.menu_export_setlists -> exportFiles(EXPORT_SETLISTS_RESULT_CODE)
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            SELECT_FILE_RESULT_CODE -> {
                val uri = data?.data!!

                Log.d(TAG, "Selected file URI: $uri")
                Log.d(TAG, "Target path: ${SongsContext.songsDirectory}")

                File(SongsContext.songsDirectory).deleteRecursively()

                FileHelper.unzip(
                    contentResolver,
                    contentResolver.openInputStream(uri)!!,
                    SongsContext.songsDirectory
                )

                SongsContext.loadSongsMetadata()
            }
            EXPORT_SONGS_RESULT_CODE -> {
                val uri = data?.data!!

                FileHelper.zip(
                    contentResolver.openOutputStream(uri)!!,
                    SongsContext.songsDirectory
                )
            }
            EXPORT_SETLISTS_RESULT_CODE -> {
                val uri = data?.data!!

                FileHelper.zip(
                    contentResolver.openOutputStream(uri)!!,
                    SetlistsContext.setlistsDirectory
                )
            }
        }
    }

    private fun setUpListeners() {
        findViewById<TabLayout>(R.id.tab_layout_song_section).addOnTabSelectedListener(
            TabItemSelectedListener {
                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
                val navController = navHostFragment.navController

                if (it!!.text == getString(R.string.songs)) {
                    Log.d(TAG, "Switching to song list")
                    navController.navigate(R.id.action_SetlistsFragment_to_SongListFragment)
                } else if (it.text == getString(R.string.setlists)) {
                    Log.d(TAG, "Switching to setlists")
                    navController.navigate(R.id.action_SongListFragment_to_SetlistsFragment)
                }
            })

        findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener {
            if (findViewById<LinearLayout>(R.id.fab_view_add_song).isVisible) {
                findViewById<LinearLayout>(R.id.fab_view_add_song).visibility = View.GONE
                findViewById<LinearLayout>(R.id.fab_view_add_setlist).visibility = View.GONE
            } else {
                findViewById<LinearLayout>(R.id.fab_view_add_song).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.fab_view_add_setlist).visibility = View.VISIBLE
            }
        }

        findViewById<FloatingActionButton>(R.id.fab_add_setlist).setOnClickListener {
            val intent = Intent(baseContext, SetlistEditorActivity::class.java)
            startActivity(intent)
        }

        findViewById<FloatingActionButton>(R.id.fab_add_song).setOnClickListener {
            val intent = Intent(baseContext, SongEditorActivity::class.java)
            startActivity(intent)
        }
    }

    private fun exportFiles(resultCode: Int): Boolean {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a directory")
        startActivityForResult(chooserIntent, resultCode)
        return true
    }

    private fun importSongs(): Boolean {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a file")
        startActivityForResult(chooserIntent, SELECT_FILE_RESULT_CODE)
        return true
    }

    private fun goToSettings(): Boolean {
        val intent = Intent(this, SettingsActivity::class.java)

        startActivity(intent)
        return true
    }

}