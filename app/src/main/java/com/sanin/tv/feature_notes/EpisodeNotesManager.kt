package com.sanin.tv.feature_notes

import com.sanin.tv.settings.saving.PrefManager

/**
 * Feature 5: Episode Notes
 * Manages timestamped personal notes attached to specific episodes.
 * Notes are persisted via PrefManager keyed by mediaId + episodeNumber.
 */
object EpisodeNotesManager {
    private fun keyFor(mediaId: Int, episodeNumber: Float): String =
        "episode_notes_${mediaId}_$episodeNumber"

    private fun allNotesKey(mediaId: Int): String = "episode_notes_index_$mediaId"

    /** Save or update a note for a given episode. If [text] is blank, the note is deleted. */
    fun saveNote(mediaId: Int, episodeNumber: Float, timestampMs: Long, text: String) {
    val key = keyFor(mediaId, episodeNumber)
        if (text.isBlank()) {
            deleteNote(mediaId, episodeNumber)
            return
        }
        val existing = getNote(mediaId, episodeNumber)
        val note = EpisodeNote(
            mediaId = mediaId,
            episodeNumber = episodeNumber,
            timestampMs = timestampMs,
            text = text,
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        PrefManager.setCustomVal(key, note)
        updateIndex(mediaId, episodeNumber, add = true)
    }

    fun getNote(mediaId: Int, episodeNumber: Float): EpisodeNote? =
        PrefManager.getNullableCustomVal(keyFor(mediaId, episodeNumber), null, EpisodeNote::class.java)

    fun deleteNote(mediaId: Int, episodeNumber: Float) {
        PrefManager.removeVal(keyFor(mediaId, episodeNumber))
        updateIndex(mediaId, episodeNumber, add = false)
    }

    /** Returns all notes for a given media item sorted by episode number. */
    @Suppress("UNCHECKED_CAST")
    fun getAllNotes(mediaId: Int): List<EpisodeNote> {
    val index = getIndex(mediaId)
        return index.mapNotNull { ep -> getNote(mediaId, ep) }
            .sortedBy { it.episodeNumber }
    }

    fun hasNote(mediaId: Int, episodeNumber: Float): Boolean =
        getNote(mediaId, episodeNumber) != null

    @Suppress("UNCHECKED_CAST")
    private fun getIndex(mediaId: Int): Set<Float> {
    return (PrefManager.getNullableCustomVal(
            allNotesKey(mediaId), null, Set::class.java
        ) as? Set<Float>) ?: emptySet()
    }

    private fun updateIndex(mediaId: Int, episodeNumber: Float, add: Boolean) {
    val current = getIndex(mediaId).toMutableSet()
        if (add) current.add(episodeNumber) else current.remove(episodeNumber)
        PrefManager.setCustomVal(allNotesKey(mediaId), current)
    }

    /** Format a millisecond timestamp as HH:MM:SS for display. */
    fun formatTimestamp(ms: Long): String {
    val totalSecs = ms / 1000
        val h = totalSecs / 3600
        val m = (totalSecs % 3600) / 60
        val s = totalSecs % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
    }
}
