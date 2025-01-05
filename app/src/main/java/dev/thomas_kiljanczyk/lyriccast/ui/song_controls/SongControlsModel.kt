/*
 * Created by Tomasz Kiljanczyk on 05/01/2025, 19:35
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 05/01/2025, 19:35
 */

package dev.thomas_kiljanczyk.lyriccast.ui.song_controls

import android.util.Log
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
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.ReceivedPayload
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.ShowLyricsContent
import dev.thomas_kiljanczyk.lyriccast.shared.misc.LyricCastMessagingContext
import dev.thomas_kiljanczyk.lyriccast.shared.misc.SessionServerCommand
import dev.thomas_kiljanczyk.lyriccast.shared.misc.SessionServerMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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
        dataStore.data.onEach { settings ->
            castConfiguration = settings.getCastConfiguration()
            sendConfiguration()
        }.flowOn(Dispatchers.Default).launchIn(viewModelScope)

        lyricCastMessagingContext.receivedPayload
            .onEach {
                Log.i("SongControlsModel", "Received payload: $it")
            }
            .onEach(::handlePayload)
            .flowOn(Dispatchers.Default).launchIn(viewModelScope)
    }

    fun initialize(sessionManager: SessionManager) {
        sessionManager.addSessionManagerListener(castSessionListener)
    }

    override fun onCleared() {
        CastContext.getSharedInstance()!!.sessionManager.removeSessionManagerListener(
            castSessionListener
        )

        super.onCleared()
    }

    private fun handlePayload(receivedPayload: ReceivedPayload) {
        val content = SessionServerMessage.fromJson(receivedPayload.payload) ?: return

        when (content.command) {
            SessionServerCommand.SEND_LATEST_SLIDE -> {
                lyricCastMessagingContext.sendContentMessage(
                    receivedPayload.endpointId, getCurrentShowLyricsContent()
                )
            }
        }

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
        lyricCastMessagingContext.broadcastContentMessage(
            getCurrentShowLyricsContent()
        )
        postSlide()
    }

    private fun postSlide() = viewModelScope.launch(Dispatchers.Default) {
        _currentSlideNumber.emit("${currentSlide + 1}/${lyrics.size}")
        _currentSlideText.emit(lyrics[currentSlide])
    }

    private fun getCurrentShowLyricsContent(): ShowLyricsContent {
        return ShowLyricsContent(
            songTitle, lyrics[currentSlide], currentSlide + 1, lyrics.size
        )
    }
}