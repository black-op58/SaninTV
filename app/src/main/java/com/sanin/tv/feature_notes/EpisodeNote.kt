package com.sanin.tv.feature_notes

import java.io.Serializable

data class EpisodeNote(
    val mediaId: Int,
    val episodeNumber: Float,
    val timestampMs: Long,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable {
    companion object {
    private const val serialVersionUID = 1L
    }
}
