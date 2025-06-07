/*
 * Created by Tomasz Kiljanczyk on 6/7/25, 5:53 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/7/25, 5:53 PM
 */

package dev.thomas_kiljanczyk.lyriccast.shared.cast

import android.util.Log
import com.google.android.gms.cast.framework.CastContext
import dev.thomas_kiljanczyk.lyriccast.application.CastConfiguration
import dev.thomas_kiljanczyk.lyriccast.shared.enums.ControlAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class CastMessagingContext(
    private val castContext: CastContext
) {
    companion object {
        private const val TAG = "CastMessagingContext"
        private const val CONTENT_NAMESPACE: String = "urn:x-cast:lyric.cast.content"
        private const val CONTROL_NAMESPACE: String = "urn:x-cast:lyric.cast.control"
    }

    private val _isBlanked: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isBlanked get() = _isBlanked.asStateFlow()

    suspend fun sendContentMessage(message: String) {
        val formattedMessage = message.replace("\n", "<br>").replace("\r", "")

        val messageContentJson = TextCastMessage(formattedMessage).toJson()

        Log.d(TAG, "Sending content message")
        Log.d(TAG, "Namespace: $CONTENT_NAMESPACE")
        Log.d(TAG, "Content: $messageContentJson")

        return withContext(Dispatchers.Main) {
            val castSession =
                castContext.sessionManager.currentCastSession
            if (castSession == null) {
                Log.d(TAG, "Message not sent (no session)")
                return@withContext
            }

            castSession.sendMessage(CONTENT_NAMESPACE, messageContentJson)
        }
    }

    suspend fun sendBlank(blanked: Boolean) {
        if (isNotInSession()) {
            return
        }

        _isBlanked.value = blanked
        sendControlMessage(ControlAction.BLANK, blanked)
    }

    suspend fun sendConfiguration(configuration: CastConfiguration) {
        sendControlMessage(
            ControlAction.CONFIGURE, configuration
        )
    }

    fun onSessionEnded() {
        _isBlanked.value = true
    }

    private suspend fun isNotInSession(): Boolean {
        val castSession =
            withContext(Dispatchers.Main) { castContext.sessionManager.currentCastSession }
        return castSession == null
    }

    private suspend inline fun <reified T> sendControlMessage(action: ControlAction, value: T) {
        val messageJson = Json.encodeToString(ControlCastMessage(action.toString(), value))

        Log.d(TAG, "Sending control message")
        Log.d(TAG, "Namespace: $CONTROL_NAMESPACE")
        Log.d(TAG, "Content: $messageJson")

        withContext(Dispatchers.Main) {
            if (isNotInSession()) {
                Log.d(TAG, "Message not sent (no session)")
                return@withContext
            }

            val castSession =
                withContext(Dispatchers.Main) { castContext.sessionManager.currentCastSession!! }
            castSession.sendMessage(CONTROL_NAMESPACE, messageJson)
        }
    }

}