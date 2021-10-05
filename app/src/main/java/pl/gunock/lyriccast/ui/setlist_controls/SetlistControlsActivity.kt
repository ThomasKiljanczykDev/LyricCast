/*
 * Created by Tomasz Kiljanczyk on 05/10/2021, 19:46
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 05/10/2021, 19:34
 */

package pl.gunock.lyriccast.ui.setlist_controls

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.cast.framework.CastContext
import dagger.hilt.android.AndroidEntryPoint
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.ActivitySetlistControlsBinding
import pl.gunock.lyriccast.databinding.ContentSetlistControlsBinding
import pl.gunock.lyriccast.shared.cast.CustomMediaRouteActionProvider
import pl.gunock.lyriccast.shared.extensions.loadAd
import pl.gunock.lyriccast.ui.settings.SettingsActivity
import pl.gunock.lyriccast.ui.shared.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.ui.shared.listeners.LongClickAdapterItemListener

@AndroidEntryPoint
class SetlistControlsActivity : AppCompatActivity() {

    private val viewModel: SetlistControlsViewModel by viewModels()

    private lateinit var binding: ContentSetlistControlsBinding

    private lateinit var songItemsAdapter: ControlsSongItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootBinding = ActivitySetlistControlsBinding.inflate(layoutInflater)
        binding = rootBinding.contentSetlistControls
        setContentView(rootBinding.root)
        setSupportActionBar(rootBinding.toolbarControls)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding.advSetlistControls.loadAd()

        val setlistId: String = intent.getStringExtra("setlistId")!!
        viewModel.loadSetlist(setlistId)

        viewModel.currentBlankTextAndColor.observe(this) {
            val (blankText: Int, blankColor: Int) = it
            binding.btnSetlistBlank.setBackgroundColor(getColor(blankColor))
            binding.btnSetlistBlank.text = getString(blankText)
        }

        viewModel.currentSlideText.observe(this) { binding.tvSetlistSlidePreview.text = it }
        viewModel.currentSongTitle.observe(this) { binding.tvCurrentSongTitle.text = it }
        viewModel.currentSongPosition.observe(this) {
            binding.rcvSongs.scrollToPosition(it)
            binding.rcvSongs.postInvalidate()
        }
        viewModel.changedSongPosition.observe(this) {
            songItemsAdapter.notifyItemChanged(it)
        }

        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()

        viewModel.sendConfiguration()
        viewModel.sendSlide()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_controls, menu)

        val castActionProvider =
            MenuItemCompat.getActionProvider(menu.findItem(R.id.menu_cast)) as CustomMediaRouteActionProvider
        castActionProvider.routeSelector = CastContext.getSharedInstance()!!.mergedSelector!!

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> goToSettings()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        binding.rcvSongs.setHasFixedSize(true)
        binding.rcvSongs.layoutManager = LinearLayoutManager(baseContext)

        val onLongClickListener =
            LongClickAdapterItemListener { _: ControlsSongItemsAdapter.ViewHolder, position, _ ->
                binding.rcvSongs.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                viewModel.selectSong(position)
                return@LongClickAdapterItemListener true
            }

        val onClickListener =
            ClickAdapterItemListener { _: ControlsSongItemsAdapter.ViewHolder, position, _ ->
                binding.rcvSongs.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                viewModel.selectSong(position)
            }

        songItemsAdapter = ControlsSongItemsAdapter(
            this,
            viewModel.songs,
            onItemLongClickListener = onLongClickListener,
            onItemClickListener = onClickListener
        )

        binding.rcvSongs.adapter = songItemsAdapter
    }

    private fun setupListeners() {
        binding.btnSetlistBlank.setOnClickListener { viewModel.sendBlank() }
        binding.btnSetlistPrev.setOnClickListener { viewModel.previousSlide() }
        binding.btnSetlistNext.setOnClickListener { viewModel.nextSlide() }
    }

    private fun goToSettings(): Boolean {
        val intent = Intent(baseContext, SettingsActivity::class.java)
        startActivity(intent)
        viewModel.sendConfiguration()
        return true
    }
}