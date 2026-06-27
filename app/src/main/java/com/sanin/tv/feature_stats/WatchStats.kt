package com.sanin.tv.feature_stats

import java.io.Serializable

/**
 * Feature 10: Statistics Dashboard
 * Aggregated watch statistics computed from the user's AniList media list.
 */
data class WatchStats(
    val totalEpisodes: Int = 0,
    val totalMinutesWatched: Long = 0L,
    val uniqueTitles: Int = 0,
    val completedTitles: Int = 0,
    val droppedTitles: Int = 0,
    val planningTitles: Int = 0,
    val currentlyWatching: Int = 0,
    val averageScore: Float = 0f,
    val genreBreakdown: Map<String, Int> = emptyMap(),    // genre → count
    val yearBreakdown: Map<Int, Int> = emptyMap(),         // year → episode count
    val monthlyEpisodes: Map<String, Int> = emptyMap(),    // "YYYY-MM" → count
    val topGenres: List<String> = emptyList(),
    val longestStreak: Int = 0,                            // days
    val currentStreak: Int = 0,                            // days
    val lastWatchedAt: Long? = null,
    val computedAt: Long = System.currentTimeMillis()
) : Serializable {
    val totalHoursWatched: Float get() = totalMinutesWatched / 60f
    val totalDaysWatched: Float get() = totalHoursWatched / 24f

    companion object {
    private const val serialVersionUID = 1L
        val EMPTY = WatchStats()
    }
}
