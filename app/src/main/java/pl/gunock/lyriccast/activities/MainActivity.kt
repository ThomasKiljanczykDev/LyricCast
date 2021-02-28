/*
 * Created by Tomasz Kiljańczyk on 2/28/21 11:18 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/28/21 11:18 PM
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
        const val EXPORT_RESULT_CODE = 1
        const val IMPORT_RESULT_CODE = 2
    }

    private var castContext: CastContext? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MessageHelper.initialize(applicationContext)

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        findViewById<LinearLayout>(R.id.fab_view_add_song).visibility = View.GONE
        findViewById<LinearLayout>(R.id.fab_view_add_setlist).visibility = View.GONE

        val wifiManager = baseContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            val turnWifiOn = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(turnWifiOn)
        }

        setUpListeners()

        SongsContext.songsDirectory = "${filesDir.path}/songs/"
        SetlistsContext.setlistsDirectory = "${filesDir.path}/setlists/"

        castContext = CastContext.getSharedInstance(this)

        CoroutineScope(Dispatchers.Main).launch {
            loadData()
            goToSongListFragment()
        }
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
            R.id.menu_import_songs -> import()
            R.id.menu_export_all -> exportFiles()
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        val uri = data?.data!!
        when (requestCode) {
            IMPORT_RESULT_CODE -> {
                Log.d(TAG, "Selected file URI: $uri")
                Log.d(TAG, "Target path: ${SongsContext.songsDirectory}")

                File(SongsContext.songsDirectory).deleteRecursively()

                FileHelper.unzip(
                    contentResolver,
                    contentResolver.openInputStream(uri)!!,
                    filesDir.path
                )

                SongsContext.loadSongsMetadata()
                finish()
                val intent = Intent(baseContext, this.javaClass)
                startActivity(intent)
            }
            EXPORT_RESULT_CODE -> {
                FileHelper.zip(contentResolver.openOutputStream(uri)!!, filesDir.path)
                finish()
                val intent = Intent(baseContext, this.javaClass)
                startActivity(intent)
            }
        }
    }

    private fun setUpListeners() {
        findViewById<TabLayout>(R.id.tab_layout_song_section).addOnTabSelectedListener(
            TabItemSelectedListener { tab ->
                tab ?: return@TabItemSelectedListener

                val navController = findNavController(R.id.main_nav_host)

                if (tab.text == getString(R.string.songs)) {
                    Log.d(TAG, "Switching to song list")
                    navController.navigate(R.id.action_SetlistsFragment_to_SongListFragment)
                } else if (tab.text == getString(R.string.setlists)) {
                    Log.d(TAG, "Switching to setlists")
                    navController.navigate(R.id.action_SongListFragment_to_SetlistsFragment)
                }
            })

        val addSongFab = findViewById<LinearLayout>(R.id.fab_view_add_song)
        val addSetlistFab = findViewById<LinearLayout>(R.id.fab_view_add_setlist)
        findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener {
            if (addSongFab.isVisible) {
                addSongFab.visibility = View.GONE
                addSetlistFab.visibility = View.GONE
            } else {
                addSongFab.visibility = View.VISIBLE
                addSetlistFab.visibility = View.VISIBLE
            }
        }

        findViewById<FloatingActionButton>(R.id.fab_add_setlist).setOnClickListener {
            val intent = Intent(baseContext, SetlistEditorActivity::class.java)
            startActivity(intent)
            addSongFab.visibility = View.GONE
            addSetlistFab.visibility = View.GONE
        }

        findViewById<FloatingActionButton>(R.id.fab_add_song).setOnClickListener {
            val intent = Intent(baseContext, SongEditorActivity::class.java)
            startActivity(intent)
            addSongFab.visibility = View.GONE
            addSetlistFab.visibility = View.GONE
        }
    }

    private fun exportFiles(): Boolean {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a directory")
        startActivityForResult(chooserIntent, EXPORT_RESULT_CODE)
        return true
    }

    private fun import(): Boolean {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a file")
        startActivityForResult(chooserIntent, IMPORT_RESULT_CODE)
        return true
    }

    private fun goToSettings(): Boolean {
        val intent = Intent(this, SettingsActivity::class.java)

        startActivity(intent)
        return true
    }

    private fun loadData() {
        if (SongsContext.getSongMap().isNotEmpty() && SetlistsContext.getSetlistItems()
                .isNotEmpty()
        ) {
            return
        }

        // TODO: Potential leak
        val alertDialog: AlertDialog = AlertDialog.Builder(this)
            .setMessage("Loading...")
            .create()
        alertDialog.show()

        if (SongsContext.getSongMap().isEmpty()) {
            SongsContext.loadSongsMetadata()
        }

        if (SetlistsContext.getSetlistItems().isEmpty()) {
            SetlistsContext.loadSetlists()
        }

        alertDialog.hide()
    }

    private suspend fun goToSongListFragment() {
        while (findViewById<FragmentContainerView>(R.id.main_nav_host) == null) {
            delay(100)
        }

        val navController = findNavController(R.id.main_nav_host)
        navController.navigate(R.id.action_EmptyFragment_to_SongListFragment)
    }
}