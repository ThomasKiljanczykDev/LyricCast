/*
 * Created by Tomasz Kiljanczyk on 19/12/2021, 20:07
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 19/12/2021, 20:01
 */

package pl.gunock.lyriccast.ui.song_controls

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.application.Settings
import pl.gunock.lyriccast.application.getCastConfigurationJson
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.shared.cast.CastMessageHelper
import pl.gunock.lyriccast.shared.cast.SessionStartedListener
import javax.inject.Inject

@HiltViewModel
class SongControlsModel @Inject constructor(
        val songsRepository: SongsRepository
) : ViewModel() {
    private companion object {
        private const val blankOnColor: Int = R.color.green
        private const val blankOffColor: Int = R.color.red
        private val currentBlankColor: Int
            get() = if (CastMessageHelper.isBlanked) {
                blankOffColor
            } else {
                blankOnColor
            }

        private const val blankOffText: Int = R.string.controls_off
        private const val blankOnText: Int = R.string.controls_on
        private val currentBlankText: Int
            get() = if (CastMessageHelper.isBlanked) {
                blankOffText
            } else {
                blankOnText
            }
    }

    var songTitle: String = ""

    var settings: Settings? = null

    val currentSlideText: LiveData<String> get() = _currentSlideText
    private val _currentSlideText: MutableLiveData<String> = MutableLiveData("")

    val currentSlideNumber: LiveData<String> get() = _currentSlideNumber
    private val _currentSlideNumber: MutableLiveData<String> = MutableLiveData("")

    val currentBlankTextAndColor: LiveData<Pair<Int, Int>> get() = _currentBlankTextAndColor
    private val _currentBlankTextAndColor: MutableLiveData<Pair<Int, Int>> =
            MutableLiveData(Pair(currentBlankText, currentBlankColor))

    private var currentSlide = 0
    private lateinit var lyrics: List<String>

    private val sessionStartedListener: SessionStartedListener = SessionStartedListener {
        postBlankColor()
        if (settings != null) {
            sendConfiguration()
        }
        sendSlide()
    }

    init {
        val sessionsManager: SessionManager = CastContext.getSharedInstance()!!.sessionManager
        sessionsManager.addSessionManagerListener(sessionStartedListener)
    }

    override fun onCleared() {
        CastContext.getSharedInstance()!!.sessionManager
                .removeSessionManagerListener(sessionStartedListener)

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
        CastMessageHelper.sendBlank(!CastMessageHelper.isBlanked)
        postBlankColor()
    }

    fun sendConfiguration() {
        CastMessageHelper.sendConfiguration(settings!!.getCastConfigurationJson())
    }

    fun sendSlide() {
        CastMessageHelper.sendContentMessage(lyrics[currentSlide])
        postSlide()
    }

    private fun postSlide() {
        _currentSlideNumber.postValue("${currentSlide + 1}/${lyrics.size}")
        _currentSlideText.postValue(lyrics[currentSlide])
    }

    private fun postBlankColor() {
        val textAndColor = Pair(currentBlankText, currentBlankColor)
        _currentBlankTextAndColor.postValue(textAndColor)
    }
}