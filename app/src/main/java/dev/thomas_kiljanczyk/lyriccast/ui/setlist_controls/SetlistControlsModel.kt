/*
 * Created by Tomasz Kiljanczyk on 06/01/2025, 19:30
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 06/01/2025, 19:18
 */

package dev.thomas_kiljanczyk.lyriccast.ui.setlist_controls

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.cast.framework.CastContext
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.application.AppSettings
import dev.thomas_kiljanczyk.lyriccast.application.CastConfiguration
import dev.thomas_kiljanczyk.lyriccast.application.getCastConfiguration
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Setlist
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Song
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SetlistsRepository
import dev.thomas_kiljanczyk.lyriccast.domain.models.SongItem
import dev.thomas_kiljanczyk.lyriccast.shared.cast.CastMessagingContext
import dev.thomas_kiljanczyk.lyriccast.shared.cast.CastSessionListener
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.ReceivedPayload
import dev.thomas_kiljanczyk.lyriccast.shared.gms_nearby.ShowLyricsContent
import dev.thomas_kiljanczyk.lyriccast.shared.misc.LyricCastMessagingContext
import dev.thomas_kiljanczyk.lyriccast.shared.misc.SessionServerCommand
import dev.thomas_kiljanczyk.lyriccast.shared.misc.SessionServerMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetlistControlsModel @Inject constructor(
    dataStore: DataStore<AppSettings>,
    private val castContext: CastContext,
    private val setlistsRepository: SetlistsRepository,
    private val castMessagingContext: CastMessagingContext,
    private val lyricCastMessagingContext: LyricCastMessagingContext
) : ViewModel() {
    companion object {
        private const val TAG = "SetlistControlsModel"
    }


    private var castConfiguration: CastConfiguration? = null

    val songs: List<SongItem> get() = _songs
    private val _songs: MutableList<SongItem> = mutableListOf()

    private val _currentSlideText = MutableSharedFlow<String>(1)
    val currentSlideText: Flow<String> get() = _currentSlideText

    private val _currentSlideNumber = MutableStateFlow("")
    val currentSlideNumber: Flow<String> get() = _currentSlideNumber

    private val _currentSongTitle = MutableSharedFlow<String>(1)
    val currentSongTitle: Flow<String> get() = _currentSongTitle

    private val _currentSongPosition = MutableStateFlow(0)
    val currentSongPosition: Flow<Int> get() = _currentSongPosition

    private val _changedSongItems: MutableStateFlow<List<Int>> = MutableStateFlow(listOf())
    val changedSongPositions: Flow<List<Int>> get() = _changedSongItems


    private var currentLyricsPosition: Int = 0
    private lateinit var currentSongItem: SongItem
    private lateinit var previousSongItem: SongItem
    private val currentSong: Song get() = currentSongItem.song

    private val castSessionListener: CastSessionListener = CastSessionListener(onStarted = {
        viewModelScope.launch {
            if (castConfiguration != null) {
                sendConfiguration()
            }
            sendSlide()
        }
    })

    init {
        dataStore.data.onEach { settings ->
            castConfiguration = settings.getCastConfiguration()
            sendConfiguration()
        }.flowOn(Dispatchers.Default).launchIn(viewModelScope)

        lyricCastMessagingContext.receivedPayload.onEach {
            Log.d(TAG, "Received payload: $it")
        }.onEach(::handlePayload).flowOn(Dispatchers.Default).launchIn(viewModelScope)

        viewModelScope.launch(Dispatchers.Main) {
            castContext.sessionManager.addSessionManagerListener(castSessionListener)
        }
    }

    override fun onCleared() {
        // Must happen outside of the ViewModel scope
        CoroutineScope(Dispatchers.Main).launch {
            castContext.sessionManager.removeSessionManagerListener(castSessionListener)
        }

        super.onCleared()
    }

    fun loadSetlist(setlistId: String) {
        val setlist: Setlist = setlistsRepository.getSetlist(setlistId)!!

        _songs.clear()

        val songItems = setlist.presentation.map { SongItem(it) }
        _songs.addAll(songItems)

        currentSongItem = songItems.first()
        currentSongItem.isHighlighted = true
        selectSong(0)
    }

    fun previousSlide() {
        val isFirstLyricsPage = currentLyricsPosition <= 0
        if (isFirstLyricsPage && _currentSongPosition.value > 0) {
            selectSong(_currentSongPosition.value - 1)
        } else if (!isFirstLyricsPage) {
            currentLyricsPosition--
            sendSlide()
        }
    }

    fun nextSlide() {
        val isLastLyricsPage = currentLyricsPosition >= currentSong.lyricsList.size - 1
        if (isLastLyricsPage && _currentSongPosition.value < songs.size - 1) {
            selectSong(_currentSongPosition.value + 1)
        } else if (!isLastLyricsPage) {
            currentLyricsPosition++
            sendSlide()
        }
    }

    suspend fun sendBlank() {
        castMessagingContext.sendBlank(!castMessagingContext.isBlanked.value)
    }

    private suspend fun sendConfiguration() {
        castMessagingContext.sendConfiguration(castConfiguration!!)
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

    fun selectSong(position: Int, fromStart: Boolean = false) =
        viewModelScope.launch(Dispatchers.Default) {
            previousSongItem = currentSongItem
            currentSongItem = _songs[position]
            currentLyricsPosition = if (fromStart) 0
            else if (position >= _songs.indexOf(previousSongItem)) 0
            else currentSongItem.song.lyricsList.size - 1
            _currentSongPosition.emit(position)

            sendSlide()
        }

    fun sendSlide() = viewModelScope.launch(Dispatchers.Default) {
        val showLyricsContent = getCurrentShowLyricsContent()
        lyricCastMessagingContext.broadcastContentMessage(
            showLyricsContent
        )
        _currentSlideText.emit(showLyricsContent.slideText)
        _currentSlideNumber.emit("${currentLyricsPosition + 1}/${currentSong.lyricsList.size}")

        // Uses reference equality to make it work for songs with same title
        val isNewSong = previousSongItem !== currentSongItem
        if (isNewSong) {
            previousSongItem.isHighlighted = false
            currentSongItem.isHighlighted = true

            // Uses reference equality to make it work for songs with same title
            val previousSongPosition = _songs.indexOfFirst { it === previousSongItem }
            val currentSongPosition = _songs.indexOfFirst { it === currentSongItem }

            _changedSongItems.value = listOf(previousSongPosition, currentSongPosition)
            _currentSongTitle.emit(currentSong.title)
        }
    }

    private fun getCurrentShowLyricsContent(): ShowLyricsContent {
        return ShowLyricsContent(
            currentSong.title,
            currentSong.lyricsList[currentLyricsPosition],
            currentLyricsPosition + 1,
            currentSong.lyricsList.size
        )
    }

}