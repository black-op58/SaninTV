package com.sanin.tv.feature_recommendations

import com.sanin.tv.connections.anilist.AnilistQueries
import com.sanin.tv.media.Media
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Feature 9: Recommendation Engine
 * Produces "Because you watched X" recommendations from AniList data.
 * Groups suggestions by the source title that triggered each recommendation.
 */
object RecommendationEngine {
    private const val TAG = "RecommendationEngine"
    private const val CACHE_KEY = "rec_engine_cache"
    private const val CACHE_TTL_MS = 12 * 60 * 60 * 1000L  // 12 hours

    data class RecommendationGroup(
        val becauseOf: Media,       // The title the user watched that drove this recommendation
        val recommendations: List<Media>
    )

    data class RecommendationCache(
        val groups: List<RecommendationGroup>,
        val cachedAt: Long = System.currentTimeMillis()
    )

    /** Build recommendation groups from the user's completed/watching list. */
    suspend fun buildRecommendations(
        completedList: List<Media>,
        forceRefresh: Boolean = false
    ): List<RecommendationGroup> = withContext(Dispatchers.IO) {
    if (!forceRefresh) {
            loadCache()?.let { cache ->
                if (System.currentTimeMillis() - cache.cachedAt < CACHE_TTL_MS) {
                    Logger.log("$TAG: Serving cached recommendations (${cache.groups.size} groups)")
                    return@withContext cache.groups
                }
            }
        }

        Logger.log("$TAG: Building fresh recommendations for ${completedList.size} titles")

        val groups = mutableListOf<RecommendationGroup>()

        // Sample up to 10 most recently updated titles to avoid rate-limiting
        val sampledList = completedList
            .sortedByDescending { it.userUpdatedAt ?: 0L }
            .take(10)

        for (media in sampledList) {
    try {
    val recs = fetchRecommendationsForMedia(media)
                if (recs.isNotEmpty()) {
                    groups.add(RecommendationGroup(becauseOf = media, recommendations = recs))
                    Logger.log("$TAG: ${recs.size} recs for '${media.userPreferredName}'")
                }
            } catch (e: Exception) {
                Logger.log("$TAG: Failed recs for ${media.id}: ${e.message}")
            }
        }

        // Deduplicate across groups — remove titles already on user's list
        val onListIds = completedList.mapNotNull { it.id }.toSet()
        val deduped = groups.map { group ->
            group.copy(recommendations = group.recommendations
                .filter { it.id !in onListIds }
                .distinctBy { it.id }
                .take(6)
            )
        }.filter { it.recommendations.isNotEmpty() }

        saveCache(RecommendationCache(deduped))
        Logger.log("$TAG: Built ${deduped.size} recommendation groups")
        deduped
    }

    /** Score-based smart ranking: prefer titles with high meanScore + popularity match. */
    fun rankRecommendations(recs: List<Media>, seedGenres: List<String>): List<Media> {
    return recs.sortedByDescending { media ->
            val baseScore = (media.meanScore ?: 0).toFloat()
            val popularityBoost = minOf((media.popularity ?: 0) / 10000f, 5f)
            val genreMatchBoost = media.genres
                ?.count { it in seedGenres }
                ?.times(3f) ?: 0f
            baseScore + popularityBoost + genreMatchBoost
        }
    }

    /** Get all unique genres from a list of media for genre-match boosting. */
    fun extractGenres(mediaList: List<Media>): List<String> {
    return mediaList.flatMap { it.genres ?: emptyList() }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(10)
            .map { it.key }
    }

    private suspend fun fetchRecommendationsForMedia(media: Media): List<Media> {
    return try {
            AnilistQueries.getRecommendations(media.id)
        } catch (e: Exception) {
            Logger.log("$TAG: fetchRecommendationsForMedia failed: ${e.message}")
            emptyList()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadCache(): RecommendationCache? =
        PrefManager.getNullableCustomVal(CACHE_KEY, null, RecommendationCache::class.java)

    private fun saveCache(cache: RecommendationCache) {
        PrefManager.setCustomVal(CACHE_KEY, cache)
    }

    fun clearCache() {
        PrefManager.removeVal(CACHE_KEY)
    }
}
