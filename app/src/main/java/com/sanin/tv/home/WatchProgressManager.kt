package com.sanin.tv.home

import com.sanin.tv.settings.saving.PrefManager
import java.io.Serializable

/**
 * Represents a single "recently watched" entry persisted locally on device.
 */
data class WatchEntry(
    val mediaId: Int,
    val mediaTitle: String,
    val mediaCover: String?,
    val episodeNumber: String,
    val episodeTitle: String?,
    val progressMs: Long,
    val durationMs: Long,
    val timestamp: Long
) : Serializable {
    companion object {
    private const val serialVersionUID = 1L
    }

    /** 0.0–1.0 progress fraction; 0 when duration unknown. */
    val progressFraction: Float
        get() = if (durationMs > 0L)
            (progressMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        else 0f
}

/**
 * Stores and retrieves local "Continue Watching" history for the last 5 anime
 * and remembers the globally last-used extension (source) index.
 */
object WatchProgressManager {
    private const val KEY_HISTORY = "local_watch_history"
    private const val KEY_LAST_EXT = "last_used_extension_index"
    private const val MAX_ENTRIES = 5

    /** Record (or update) a watch entry.  Always kept newest-first, capped at 5. */
    fun record(entry: WatchEntry) {
    val list = getHistory().toMutableList()
        list.removeAll { it.mediaId == entry.mediaId }
        list.add(0, entry)
        while (list.size > MAX_ENTRIES) list.removeAt(list.size - 1)
        PrefManager.setCustomVal(KEY_HISTORY, ArrayList(list))
    }

    /** Returns up to 5 most-recently watched entries, newest first. */
    @Suppress("UNCHECKED_CAST")
    fun getHistory(): List<WatchEntry> = try {
        (PrefManager.getNullableCustomVal(KEY_HISTORY, null, ArrayList::class.java)
                as? ArrayList<WatchEntry>) ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }

    /** Persist the last-used extension (source) index globally. */
    fun saveLastExtensionIndex(index: Int) {
        PrefManager.setCustomVal(KEY_LAST_EXT, index)
    }

    /** Returns the last-used extension index, or 0 if never set. */
    fun getLastExtensionIndex(): Int =
        PrefManager.getCustomVal(KEY_LAST_EXT, 0)
}
