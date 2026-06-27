package com.sanin.tv.feature_stats

import com.sanin.tv.media.Media
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.util.Logger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Feature 10: Statistics Dashboard
 * Computes WatchStats from a list of Media objects.
 */
object StatsCalculator {
    private const val TAG = "StatsCalculator"
    private const val CACHE_KEY = "watch_stats_cache"
    private const val CACHE_TTL_MS = 6 * 60 * 60 * 1000L  // 6 hours
    private const val AVG_EPISODE_DURATION_MINS = 24

    fun compute(mediaList: List<Media>): WatchStats {
        Logger.log("$TAG: Computing stats for ${mediaList.size} titles")

        val completed = mediaList.filter { it.userStatus == "COMPLETED" }
        val dropped = mediaList.filter { it.userStatus == "DROPPED" }
        val planning = mediaList.filter { it.userStatus == "PLANNING" }
        val watching = mediaList.filter { it.userStatus == "CURRENT" }

        val totalEpisodes = mediaList.sumOf { it.userProgress ?: 0 }

        val totalMinutes = mediaList.sumOf { media ->
            val eps = media.userProgress ?: 0
            val duration = media.anime?.episodeDuration ?: AVG_EPISODE_DURATION_MINS
            (eps * duration).toLong()
        }

        val scores = mediaList.mapNotNull { it.userScore?.takeIf { s -> s > 0 }?.toFloat() }
        val avgScore = if (scores.isEmpty()) 0f else scores.average().toFloat()

        // Genre breakdown
        val genreMap = mutableMapOf<String, Int>()
        mediaList.forEach { media ->
            media.genres?.forEach { genre ->
                genreMap[genre] = (genreMap[genre] ?: 0) + 1
            }
        }
        val topGenres = genreMap.entries.sortedByDescending { it.value }.take(8).map { it.key }

        // Year breakdown based on when episodes were updated
        val yearMap = mutableMapOf<Int, Int>()
        mediaList.forEach { media ->
            media.userUpdatedAt?.let { updatedAt ->
                val year = java.util.Calendar.getInstance().also {
                    it.timeInMillis = updatedAt
                }.get(java.util.Calendar.YEAR)
                yearMap[year] = (yearMap[year] ?: 0) + (media.userProgress ?: 0)
            }
        }

        // Streak calculation using updatedAt timestamps
        val (longestStreak, currentStreak) = calculateStreaks(mediaList)

        val lastWatched = mediaList.mapNotNull { it.userUpdatedAt }.maxOrNull()

        val stats = WatchStats(
            totalEpisodes = totalEpisodes,
            totalMinutesWatched = totalMinutes,
            uniqueTitles = mediaList.size,
            completedTitles = completed.size,
            droppedTitles = dropped.size,
            planningTitles = planning.size,
            currentlyWatching = watching.size,
            averageScore = avgScore,
            genreBreakdown = genreMap,
            yearBreakdown = yearMap,
            topGenres = topGenres,
            longestStreak = longestStreak,
            currentStreak = currentStreak,
            lastWatchedAt = lastWatched
        )

        saveCache(stats)
        Logger.log("$TAG: Stats computed — ${totalEpisodes} eps, ${totalMinutes / 60}h watched")
        return stats
    }

    fun loadCached(): WatchStats? {
    val cached = PrefManager.getNullableCustomVal(CACHE_KEY, null, WatchStats::class.java)
            ?: return null
        if (System.currentTimeMillis() - cached.computedAt > CACHE_TTL_MS) return null
        return cached
    }

    private fun saveCache(stats: WatchStats) {
        PrefManager.setCustomVal(CACHE_KEY, stats)
    }

    fun clearCache() {
        PrefManager.removeVal(CACHE_KEY)
    }

    private fun calculateStreaks(mediaList: List<Media>): Pair<Int, Int> {
    val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val watchDays = mediaList
            .mapNotNull { it.userUpdatedAt }
            .map { dayFormat.format(Date(it)) }
            .toSortedSet()
            .toList()

        if (watchDays.isEmpty()) return Pair(0, 0)

        val today = dayFormat.format(Date())
        var longest = 1
        var current = 1
        var temp = 1

        val cal = java.util.Calendar.getInstance()
        for (i in 1 until watchDays.size) {
    val prev = dayFormat.parse(watchDays[i - 1]) ?: continue
            val curr = dayFormat.parse(watchDays[i]) ?: continue
            val diffDays = ((curr.time - prev.time) / (1000 * 60 * 60 * 24)).toInt()
            if (diffDays == 1) {
                temp++
                if (temp > longest) longest = temp
            } else {
                temp = 1
            }
        }

        // Current streak: count backwards from today
        val lastDay = watchDays.last()
        val todayCal = dayFormat.parse(today) ?: return Pair(longest, 0)
        val lastCal = dayFormat.parse(lastDay) ?: return Pair(longest, 0)
        val daysSinceLast = ((todayCal.time - lastCal.time) / (1000 * 60 * 60 * 24)).toInt()
        current = if (daysSinceLast > 1) 0 else {
    var streak = 1
            for (i in watchDays.size - 1 downTo 1) {
    val prev = dayFormat.parse(watchDays[i - 1]) ?: break
                val curr = dayFormat.parse(watchDays[i]) ?: break
                if (((curr.time - prev.time) / (1000 * 60 * 60 * 24)).toInt() == 1) streak++ else break
            }
            streak
        }

        return Pair(longest, current)
    }
}
