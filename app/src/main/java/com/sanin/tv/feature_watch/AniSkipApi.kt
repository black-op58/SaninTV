package com.sanin.tv.feature_watch

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

/**
 * Feature 6: Skip Intro / Skip Outro
 *
 * Fetches crowd-sourced opening (OP) and ending (ED) timestamps from the
 * AniSkip API (https://aniskip.com).
 *
 * The API maps MAL IDs + episode numbers to skip intervals.
 * SaninTV uses Anilist IDs, so you need the MAL ID from the media object
 * (media.malId). If the media object doesn't carry malId, fall back to null
 * and no skip button will appear — that's safe.
 */
object AniSkipApi {

    private const val BASE_URL = "https://api.aniskip.com/v2/skip-times"

    enum class SkipType(val param: String) {
        OPENING("op"),
        ENDING("ed"),
        MIXED_OPENING("mixed-op"),
        MIXED_ENDING("mixed-ed")
    }

    data class SkipInterval(
        val type: SkipType,
        val startTime: Double,
        val endTime: Double
    ) {
        fun containsTime(seconds: Double): Boolean =
            seconds >= startTime && seconds <= endTime

        val durationSeconds: Double get() = endTime - startTime
    }

    data class AniSkipResult(
        val intervals: List<SkipInterval>,
        val found: Boolean
    ) {
        val opening: SkipInterval? get() =
            intervals.firstOrNull { it.type == SkipType.OPENING || it.type == SkipType.MIXED_OPENING }

        val ending: SkipInterval? get() =
            intervals.firstOrNull { it.type == SkipType.ENDING || it.type == SkipType.MIXED_ENDING }
    }

    /**
     * Fetch skip intervals for [malId] + [episodeNumber].
     * Returns AniSkipResult with an empty list (found=false) on any failure
     * so callers never need to null-check.
     */
    suspend fun getSkipTimes(malId: Int, episodeNumber: Int): AniSkipResult =
        withContext(Dispatchers.IO) {
            try {
                val types = SkipType.entries.joinToString("&") { "types=${it.param}" }
                val url = "$BASE_URL/$malId/$episodeNumber?$types&episodeLength=0"
                val json = JSONObject(URL(url).readText())

                if (!json.optBoolean("found", false)) {
                    return@withContext AniSkipResult(emptyList(), false)
                }

                val results = json.getJSONArray("results")
                val intervals = mutableListOf<SkipInterval>()

                for (i in 0 until results.length()) {
                    val item = results.getJSONObject(i)
                    val typeStr = item.getString("skipType")
                    val type = SkipType.entries.firstOrNull { it.param == typeStr } ?: continue
                    val interval = item.getJSONObject("interval")

                    intervals += SkipInterval(
                        type = type,
                        startTime = interval.getDouble("startTime"),
                        endTime = interval.getDouble("endTime")
                    )
                }

                AniSkipResult(intervals, intervals.isNotEmpty())
            } catch (e: Exception) {
                AniSkipResult(emptyList(), false)
            }
        }
}
