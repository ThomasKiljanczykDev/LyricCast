/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 10/01/2025, 01:46
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
