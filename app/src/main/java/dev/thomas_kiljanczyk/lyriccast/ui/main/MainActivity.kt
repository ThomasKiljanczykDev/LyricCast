/*
 * Created by Tomasz Kiljanczyk on 12/01/2025, 23:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 12/01/2025, 23:55
 */

package dev.thomas_kiljanczyk.lyriccast.ui.main

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.application.LyricCastApplication
import dev.thomas_kiljanczyk.lyriccast.databinding.ActivityMainBinding
import dev.thomas_kiljanczyk.lyriccast.databinding.ContentMainBinding
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.ImportOptions
import dev.thomas_kiljanczyk.lyriccast.datatransfer.enums.ImportFormat
import dev.thomas_kiljanczyk.lyriccast.shared.extensions.registerForActivityResult
import dev.thomas_kiljanczyk.lyriccast.shared.utils.DialogFragmentUtils
import dev.thomas_kiljanczyk.lyriccast.ui.category_manager.CategoryManagerActivity
import dev.thomas_kiljanczyk.lyriccast.ui.main.import_dialog.ImportDialogFragment
import dev.thomas_kiljanczyk.lyriccast.ui.main.import_dialog.ImportDialogModel
import dev.thomas_kiljanczyk.lyriccast.ui.main.setlists.SetlistsFragment
import dev.thomas_kiljanczyk.lyriccast.ui.session_client.SessionClientActivity
import dev.thomas_kiljanczyk.lyriccast.ui.setlist_editor.SetlistEditorActivity
import dev.thomas_kiljanczyk.lyriccast.ui.settings.SettingsActivity
import dev.thomas_kiljanczyk.lyriccast.ui.shared.fragments.ProgressDialogFragment
import dev.thomas_kiljanczyk.lyriccast.ui.shared.listeners.ItemSelectedTabListener
import dev.thomas_kiljanczyk.lyriccast.ui.shared.menu.cast.CustomMediaRouteActionProvider
import dev.thomas_kiljanczyk.lyriccast.ui.song_editor.SongEditorActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Closeable
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "MainActivity"

        var wifiStateChecked = false
    }

    private val viewModel: MainModel by viewModels()
    private val importDialogModel: ImportDialogModel by viewModels()

    private lateinit var binding: ContentMainBinding

    private val exportChooserResultLauncher = registerForActivityResult(this::exportAll)
    private val importChooserResultLauncher = registerForActivityResult(this::import)
    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    @Inject
    lateinit var castContext: CastContext

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val rootBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(rootBinding.root)
        setSupportActionBar(rootBinding.toolbarMain)

        binding = rootBinding.contentMain

        binding.cstlFabContainer.visibility = View.GONE

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navh_main) as NavHostFragment
        val currentFragment = navHostFragment.childFragmentManager.fragments.first()
        if (currentFragment is SetlistsFragment) {
            binding.tblMainFragments.getTabAt(1)?.select()
        }

        setupListeners()

        checkPermissions()

        setOnApplyWindowInsetsListener(rootBinding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            rootBinding.toolbarMain.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
                bottomMargin = 0
            }

            binding.tblMainFragments.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                insets.bottom
            )

            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val castActionProvider =
            MenuItemCompat.getActionProvider(menu.findItem(R.id.menu_cast)) as CustomMediaRouteActionProvider

        castActionProvider.routeSelector = castContext.mergedSelector!!

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_add_category -> {
                goToCategoryManager()
                true
            }

            R.id.menu_settings -> {
                goToSettings()
                true
            }

            R.id.menu_import_songs -> {
                lifecycleScope.launch(Dispatchers.Default) { showImportDialog() }
                true
            }

            R.id.menu_export_all -> {
                startExport()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupListeners() {
        binding.tblMainFragments.addOnTabSelectedListener(
            ItemSelectedTabListener { tab ->
                tab ?: return@ItemSelectedTabListener

                val navController = findNavController(R.id.navh_main)
                when (tab.position) {
                    0 -> {
                        Log.v(TAG, "Switching to song list")
                        navController.navigate(R.id.action_Setlists_to_Songs)
                    }

                    1 -> {
                        Log.v(TAG, "Switching to setlists")
                        navController.navigate(R.id.action_Songs_to_Setlists)
                    }

                    2 -> {
                        if (!viewModel.isSessionServerRunning) {
                            Log.v(TAG, "Switching to join session")
                            val intent = Intent(baseContext, SessionClientActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                baseContext,
                                R.string.main_activity_cannot_join_session,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        // TODO: nice to have - find a better way to handle this
                        // Prevents the tab from being selected
                        recreate()
                    }
                }
            })

        val fabContainer = binding.cstlFabContainer
        binding.fabAdd.setOnClickListener {
            if (fabContainer.isVisible) {
                fabContainer.visibility = View.GONE
                binding.fabAdd.clearFocus()
            } else {
                fabContainer.visibility = View.VISIBLE
                binding.fabAdd.requestFocus()
            }
        }

        binding.fabAdd.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                fabContainer.visibility = View.VISIBLE
                binding.imvMainDim.visibility = View.VISIBLE
            } else {
                fabContainer.visibility = View.GONE
                binding.imvMainDim.visibility = View.GONE
            }
        }

        binding.fabAddSetlist.setOnClickListener {
            val intent = Intent(baseContext, SetlistEditorActivity::class.java)
            startActivity(intent)
            binding.fabAdd.clearFocus()
        }

        binding.fabAddSong.setOnClickListener {
            val intent = Intent(baseContext, SongEditorActivity::class.java)
            startActivity(intent)
            binding.fabAdd.clearFocus()
        }
    }

    private fun startExport() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a directory")
        exportChooserResultLauncher.launch(chooserIntent)
    }

    private fun import(result: ActivityResult) {
        if (result.resultCode != RESULT_OK) {
            return
        }

        val uri: Uri = result.data!!.data!!

        Log.d(TAG, "Handling import result")
        Log.d(TAG, "Import parameters $importDialogModel")
        Log.d(TAG, "Selected file URI: $uri")
        if (importDialogModel.importFormat == ImportFormat.OPEN_SONG) {
            importOpenSong(uri)
        } else if (importDialogModel.importFormat == ImportFormat.LYRIC_CAST) {
            importLyricCast(uri)
        }
    }

    private fun exportAll(result: ActivityResult) {
        if (result.resultCode != RESULT_OK) {
            return
        }

        lifecycleScope.launch(Dispatchers.Default) {
            val uri: Uri = result.data!!.data!!

            val dialogFragment =
                DialogFragmentUtils.createProgressDialogFragment(
                    supportFragmentManager,
                    R.string.main_activity_export_preparing_data
                )

            val outputStream = contentResolver.openOutputStream(uri)!!

            val exportMessageFlow = viewModel.exportAll(
                cacheDir.canonicalPath,
                outputStream
            )

            handleDialogMessages(dialogFragment, exportMessageFlow, outputStream)
        }
    }

    private suspend fun showImportDialog() {
        val importDialog = ImportDialogFragment()
        importDialog.show(this@MainActivity.supportFragmentManager, ImportDialogFragment.TAG)

        while (!importDialog.isAdded) {
            delay(10)
        }

        importDialog.isAccepted
            .onEach { if (it) startImport() }
            .flowOn(Dispatchers.Default)
            .launchIn(importDialog.lifecycleScope)
    }

    private fun importLyricCast(uri: Uri) =
        lifecycleScope.launch(Dispatchers.Default) {
            val dialogFragment =
                DialogFragmentUtils.createProgressDialogFragment(
                    supportFragmentManager,
                    R.string.main_activity_loading_file
                )

            val importOptions = ImportOptions(
                deleteAll = importDialogModel.deleteAll,
                replaceOnConflict = importDialogModel.replaceOnConflict
            )

            val inputStream = contentResolver.openInputStream(uri)!!

            val importMessageFlow =
                viewModel.importLyricCast(
                    cacheDir.path,
                    inputStream,
                    importOptions
                )

            handleDialogMessages(dialogFragment, importMessageFlow, inputStream)
        }

    private fun importOpenSong(uri: Uri) =
        lifecycleScope.launch(Dispatchers.Default) {
            val dialogFragment =
                DialogFragmentUtils.createProgressDialogFragment(
                    supportFragmentManager,
                    R.string.main_activity_loading_file
                )

            val colors: IntArray = resources.getIntArray(R.array.category_color_values)
            val importOptions = ImportOptions(
                deleteAll = importDialogModel.deleteAll,
                replaceOnConflict = importDialogModel.replaceOnConflict,
                colors = colors
            )

            val inputStream = contentResolver.openInputStream(uri)!!

            val importMessageFlow =
                viewModel.importOpenSong(
                    cacheDir.path,
                    inputStream,
                    importOptions
                )

            handleDialogMessages(dialogFragment, importMessageFlow, inputStream)
        }

    private suspend fun handleDialogMessages(
        dialogFragment: ProgressDialogFragment,
        messageFlow: Flow<Int>?,
        stream: Closeable
    ) = withContext(Dispatchers.Main) {
        if (messageFlow != null) {
            messageFlow.onEach { dialogFragment.setMessage(it) }
                .onCompletion {
                    withContext(Dispatchers.IO) {
                        stream.close()
                    }
                    dialogFragment.dismiss()
                }.flowOn(Dispatchers.Main)
                .launchIn(dialogFragment.lifecycleScope)
        } else {
            withContext(Dispatchers.IO) { stream.close() }
            dialogFragment.setErrorState(true)
            dialogFragment.setMessage(R.string.main_activity_import_incorrect_file_format)
        }
    }


    private fun startImport() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a file")
        importChooserResultLauncher.launch(chooserIntent)
    }

    private fun goToSettings() {
        val intent = Intent(baseContext, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun goToCategoryManager() {
        val intent = Intent(baseContext, CategoryManagerActivity::class.java)
        startActivity(intent)
    }

    private fun checkPermissions() {
        when {
            LyricCastApplication.PERMISSIONS.all {
                this.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
            } -> {
                if (!wifiStateChecked) {
                    checkWifiEnabled()
                    wifiStateChecked = true
                }
                return
            }

            LyricCastApplication.PERMISSIONS.any(::shouldShowRequestPermissionRationale) -> {
                // TODO: handle permission rationale
                Log.d(TAG, "Permission rationale")
            }

            else -> {
                permissionRequestLauncher.launch(LyricCastApplication.PERMISSIONS)
            }
        }
    }

    private fun checkWifiEnabled() {
        val wifiManager = baseContext.getSystemService(WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            turnOnWifi()
        }
    }

    private fun turnOnWifi() {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_LyricCast_MaterialAlertDialog_NoTitle)
            .setMessage(getString(R.string.launch_activity_turn_on_wifi))
            .setPositiveButton(R.string.launch_activity_go_to_settings) { _, _ ->
                openWifiSettings()
            }
            .setNegativeButton(R.string.ignore, null)
            .create()
            .show()
    }

    private fun openWifiSettings() {
        val wifiIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
        } else {
            Intent(Settings.ACTION_WIRELESS_SETTINGS)
        }

        startActivity(wifiIntent)
    }
}