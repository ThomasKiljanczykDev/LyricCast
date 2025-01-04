/*
 * Created by Tomasz Kiljanczyk on 04/01/2025, 16:41
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 04/01/2025, 15:35
 */

package dev.thomas_kiljanczyk.lyriccast.shared.cast

import android.util.Log
import com.google.android.gms.cast.framework.CastContext
import dev.thomas_kiljanczyk.lyriccast.application.CastConfiguration
import dev.thomas_kiljanczyk.lyriccast.shared.enums.ControlAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CastMessagingContext {
    companion object {
        private const val TAG = "CastMessagingContext"
        private const val CONTENT_NAMESPACE: String = "urn:x-cast:lyric.cast.content"
        private const val CONTROL_NAMESPACE: String = "urn:x-cast:lyric.cast.control"
    }

    private val _isBlanked: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isBlanked: StateFlow<Boolean> get() = _isBlanked

    fun sendContentMessage(message: String) {
        val context: CastContext = CastContext.getSharedInstance()!!
        val castSession = context.sessionManager.currentCastSession

        val formattedMessage = message.replace("\n", "<br>")
            .replace("\r", "")

        val messageContent = TextCastMessage(formattedMessage)
        val messageContentJson = Json.encodeToString(messageContent)

        Log.d(TAG, "Sending content message")
        Log.d(TAG, "Namespace: $CONTENT_NAMESPACE")
        Log.d(TAG, "Content: $messageContentJson")
        if (castSession == null) {
            Log.d(TAG, "Message not sent (no session)")
            return
        }

        castSession.sendMessage(CONTENT_NAMESPACE, messageContentJson)
    }

    fun sendBlank(blanked: Boolean) {
        if (isNotInSession()) {
            return
        }

        _isBlanked.value = blanked
        sendControlMessage(ControlAction.BLANK, blanked)
    }

    fun sendConfiguration(configuration: CastConfiguration) {
        sendControlMessage(
            ControlAction.CONFIGURE,
            Json.encodeToString(configuration)
        )
    }

    fun onSessionEnded() {
        _isBlanked.value = true
    }

    private fun isNotInSession(): Boolean {
        val castSession = runBlocking(Dispatchers.Main) {
            val context = CastContext.getSharedInstance()
            context?.sessionManager?.currentCastSession
        }

        return castSession == null
    }

    private inline fun <reified T> sendControlMessage(action: ControlAction, value: T) {
        val message = ControlCastMessage(action.toString(), value)

        val messageJson = Json.encodeToString(message)

        Log.d(TAG, "Sending control message")
        Log.d(TAG, "Namespace: $CONTROL_NAMESPACE")
        Log.d(TAG, "Content: $messageJson")
        if (isNotInSession()) {
            Log.d(TAG, "Message not sent (no session)")
            return
        }

        val context: CastContext = CastContext.getSharedInstance()!!
        val castSession = context.sessionManager.currentCastSession!!
        castSession.sendMessage(CONTROL_NAMESPACE, messageJson)
    }

}