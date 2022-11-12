/*
 * Created by Tomasz Kiljanczyk on 12/11/2022, 20:29
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 12/11/2022, 20:13
 */

package pl.gunock.lyriccast.ui.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.cast.framework.CastContext
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.ActivityMainBinding
import pl.gunock.lyriccast.databinding.ContentMainBinding
import pl.gunock.lyriccast.datamodel.models.ImportOptions
import pl.gunock.lyriccast.datatransfer.enums.ImportFormat
import pl.gunock.lyriccast.shared.cast.CustomMediaRouteActionProvider
import pl.gunock.lyriccast.shared.extensions.loadAd
import pl.gunock.lyriccast.shared.extensions.registerForActivityResult
import pl.gunock.lyriccast.shared.utils.DialogFragmentUtils
import pl.gunock.lyriccast.ui.category_manager.CategoryManagerActivity
import pl.gunock.lyriccast.ui.main.import_dialog.ImportDialogFragment
import pl.gunock.lyriccast.ui.main.import_dialog.ImportDialogModel
import pl.gunock.lyriccast.ui.main.setlists.SetlistsFragment
import pl.gunock.lyriccast.ui.setlist_editor.SetlistEditorActivity
import pl.gunock.lyriccast.ui.settings.SettingsActivity
import pl.gunock.lyriccast.ui.shared.fragments.ProgressDialogFragment
import pl.gunock.lyriccast.ui.shared.listeners.ItemSelectedTabListener
import pl.gunock.lyriccast.ui.song_editor.SongEditorActivity
import java.io.Closeable

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "MainActivity"
    }

    private val viewModel: MainModel by viewModels()
    private val importDialogModel: ImportDialogModel by viewModels()

    private lateinit var binding: ContentMainBinding

    private val exportChooserResultLauncher = registerForActivityResult(this::exportAll)
    private val importChooserResultLauncher = registerForActivityResult(this::import)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.init()

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
    }

    override fun onResume() {
        binding.advMain.loadAd()
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val castActionProvider =
            MenuItemCompat.getActionProvider(menu.findItem(R.id.menu_cast)) as CustomMediaRouteActionProvider

        castActionProvider.routeSelector = CastContext.getSharedInstance()!!.mergedSelector!!

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_add_category -> goToCategoryManager()
            R.id.menu_settings -> goToSettings()
            R.id.menu_import_songs -> {
                lifecycleScope.launch(Dispatchers.Default) { showImportDialog() }
                true
            }
            R.id.menu_export_all -> startExport()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupListeners() {
        binding.tblMainFragments.addOnTabSelectedListener(
            ItemSelectedTabListener { tab ->
                tab ?: return@ItemSelectedTabListener

                val navController = findNavController(R.id.navh_main)
                if (tab.text == getString(R.string.title_songs)) {
                    Log.v(TAG, "Switching to song list")
                    navController.navigate(R.id.action_Setlists_to_Songs)
                } else if (tab.text == getString(R.string.title_setlists)) {
                    Log.v(TAG, "Switching to setlists")
                    navController.navigate(R.id.action_Songs_to_Setlists)
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
                binding.imvMainDimm.visibility = View.VISIBLE
            } else {
                fabContainer.visibility = View.GONE
                binding.imvMainDimm.visibility = View.GONE
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

    private fun startExport(): Boolean {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a directory")
        exportChooserResultLauncher.launch(chooserIntent)

        return true
    }

    private fun import(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) {
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
        if (result.resultCode != Activity.RESULT_OK) {
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

            withContext(Dispatchers.Main) {
                handleDialogMessages(dialogFragment, exportMessageFlow, outputStream)
            }
        }
    }

    private suspend fun showImportDialog() {
        val importDialog = ImportDialogFragment().apply {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_LyricCast_Dialog_NoTitle)
            show(this@MainActivity.supportFragmentManager, ImportDialogFragment.TAG)
        }

        while (!importDialog.isAdded) {
            delay(10)
        }

        withContext(Dispatchers.Main) {
            importDialog.isAccepted
                .onEach { if (it) startImport() }
                .flowOn(Dispatchers.Default)
                .launchIn(importDialog.lifecycleScope)
        }
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

            withContext(Dispatchers.Main) {
                handleDialogMessages(dialogFragment, importMessageFlow, inputStream)
            }
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

            withContext(Dispatchers.Main) {
                handleDialogMessages(dialogFragment, importMessageFlow, inputStream)
            }
        }

    private fun handleDialogMessages(
        dialogFragment: ProgressDialogFragment,
        messageFlow: Flow<Int>?,
        stream: Closeable
    ) {
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
            stream.close()
            dialogFragment.setErrorState(true)
            dialogFragment.setMessage(R.string.main_activity_import_incorrect_file_format)
        }
    }


    private fun startImport(): Boolean {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a file")
        importChooserResultLauncher.launch(chooserIntent)
        return true
    }

    private fun goToSettings(): Boolean {
        val intent = Intent(baseContext, SettingsActivity::class.java)
        startActivity(intent)
        return true
    }

    private fun goToCategoryManager(): Boolean {
        val intent = Intent(baseContext, CategoryManagerActivity::class.java)
        startActivity(intent)
        return true
    }
}