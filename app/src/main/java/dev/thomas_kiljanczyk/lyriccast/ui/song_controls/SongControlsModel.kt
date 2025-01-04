/*
 * Created by Tomasz Kiljanczyk on 04/01/2025, 16:41
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 04/01/2025, 16:41
 */

package dev.thomas_kiljanczyk.lyriccast.ui.song_controls

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.application.AppSettings
import dev.thomas_kiljanczyk.lyriccast.application.CastConfiguration
import dev.thomas_kiljanczyk.lyriccast.application.getCastConfiguration
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Song
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SongsRepository
import dev.thomas_kiljanczyk.lyriccast.shared.cast.CastMessagingContext
import dev.thomas_kiljanczyk.lyriccast.shared.cast.CastSessionListener
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.ShowLyricsContent
import dev.thomas_kiljanczyk.lyriccast.shared.misc.LyricCastMessagingContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SongControlsModel @Inject constructor(
    dataStore: DataStore<AppSettings>,
    val songsRepository: SongsRepository,
    private val castMessagingContext: CastMessagingContext,
    private val lyricCastMessagingContext: LyricCastMessagingContext
) : ViewModel() {
    var songTitle: String = ""

    private var castConfiguration: CastConfiguration? = null

    val currentSlideText: Flow<String> get() = _currentSlideText
    private val _currentSlideText = MutableStateFlow("")

    val currentSlideNumber: Flow<String> get() = _currentSlideNumber
    private val _currentSlideNumber = MutableStateFlow("")

    private var currentSlide = 0
    private lateinit var lyrics: List<String>

    private val castSessionListener: CastSessionListener = CastSessionListener(onStarted = {
        if (castConfiguration != null) sendConfiguration()
        sendSlide()
    })

    init {
        dataStore.data
            .onEach { settings ->
                castConfiguration = settings.getCastConfiguration()
                sendConfiguration()
            }.flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    fun initialize(sessionManager: SessionManager) {
        sessionManager.addSessionManagerListener(castSessionListener)
    }

    override fun onCleared() {
        CastContext.getSharedInstance()!!.sessionManager
            .removeSessionManagerListener(castSessionListener)

        super.onCleared()
    }

    fun loadSong(songId: String) {
        val song: Song = songsRepository.getSong(songId)!!

        lyrics = song.lyricsList
        songTitle = song.title
        postSlide()
    }

    fun previousSlide() {
        if (currentSlide <= 0) {
            return
        }
        currentSlide--

        sendSlide()
    }

    fun nextSlide() {
        if (currentSlide >= lyrics.size - 1) {
            return
        }
        currentSlide++

        sendSlide()
    }

    fun sendBlank() {
        castMessagingContext.sendBlank(!castMessagingContext.isBlanked.value)
    }

    private fun sendConfiguration() {
        castMessagingContext.sendConfiguration(castConfiguration!!)
    }

    fun sendSlide() {
        lyricCastMessagingContext.sendContentMessage(
            ShowLyricsContent(
                songTitle,
                lyrics[currentSlide],
                currentSlide + 1,
                lyrics.size
            )
        )
        postSlide()
    }

    private fun postSlide() {
        _currentSlideNumber.tryEmit("${currentSlide + 1}/${lyrics.size}")
        _currentSlideText.tryEmit(lyrics[currentSlide])
    }
}