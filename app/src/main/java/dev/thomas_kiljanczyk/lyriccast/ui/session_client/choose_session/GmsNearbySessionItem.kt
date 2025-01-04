/*
 * Created by Tomasz Kiljanczyk on 04/01/2025, 16:41
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 04/01/2025, 15:56
 */

package dev.thomas_kiljanczyk.lyriccast.ui.session_client.choose_session

data class GmsNearbySessionItem(
    val deviceName: String,
    val endpointId: String
) {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is GmsNearbySessionItem) {
            return false
        }

        return endpointId == other.endpointId
    }

    override fun hashCode(): Int {
        return endpointId.hashCode()
    }
}
