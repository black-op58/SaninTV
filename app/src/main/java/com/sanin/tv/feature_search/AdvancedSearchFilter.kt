package com.sanin.tv.feature_search

import java.io.Serializable

/**
 * Feature 6: Advanced Search Filters
 * Data class representing all filter options for the enhanced search screen.
 */
data class AdvancedSearchFilter(
    val seasonYear: Int? = null,
    val season: String? = null,              // WINTER, SPRING, SUMMER, FALL
    val minScore: Int? = null,               // 0–100
    val maxScore: Int? = null,
    val studioId: Int? = null,
    val studioName: String? = null,
    val source: String? = null,              // ORIGINAL, MANGA, LIGHT_NOVEL, etc.
    val status: String? = null,              // RELEASING, FINISHED, NOT_YET_RELEASED, CANCELLED, HIATUS
    val format: String? = null,             // TV, MOVIE, OVA, ONA, SPECIAL, MUSIC
    val genre: String? = null,
    val minEpisodes: Int? = null,
    val maxEpisodes: Int? = null,
    val onMyList: Boolean = false,
    val isAdult: Boolean = false
) : Serializable {

    companion object {
    private const val serialVersionUID = 1L

        val SEASONS = listOf("WINTER", "SPRING", "SUMMER", "FALL")
        val SOURCES = listOf("ORIGINAL", "MANGA", "LIGHT_NOVEL", "VISUAL_NOVEL", "VIDEO_GAME", "OTHER", "NOVEL", "DOUJINSHI", "ANIME", "WEB_NOVEL", "LIVE_ACTION", "GAME", "COMIC", "MULTIMEDIA_PROJECT", "PICTURE_BOOK")
        val STATUSES = listOf("RELEASING", "FINISHED", "NOT_YET_RELEASED", "CANCELLED", "HIATUS")
        val FORMATS = listOf("TV", "TV_SHORT", "MOVIE", "SPECIAL", "OVA", "ONA", "MUSIC")
        val CURRENT_YEAR = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val YEARS = (1990..CURRENT_YEAR + 1).toList().reversed()

        val EMPTY = AdvancedSearchFilter()
    }

    val isEmpty: Boolean
        get() = seasonYear == null && season == null && minScore == null && maxScore == null &&
                studioId == null && source == null && status == null && format == null &&
                genre == null && minEpisodes == null && maxEpisodes == null && !onMyList

    /** Build AniList GraphQL variable string fragment from the active filters. */
    fun toGraphQLArgs(): String {
    val parts = mutableListOf<String>()
        seasonYear?.let { parts.add("seasonYear: $it") }
        season?.let { parts.add("season: $it") }
        minScore?.let { parts.add("averageScore_greater: ${it - 1}") }
        maxScore?.let { parts.add("averageScore_lesser: ${it + 1}") }
        studioId?.let { parts.add("studios: [$it]") }
        source?.let { parts.add("source: $it") }
        status?.let { parts.add("status: $it") }
        format?.let { parts.add("format: $it") }
        genre?.let { parts.add("genre: \"$it\"") }
        minEpisodes?.let { parts.add("episodes_greater: ${it - 1}") }
        maxEpisodes?.let { parts.add("episodes_lesser: ${it + 1}") }
        if (onMyList) parts.add("onList: true")
        if (!isAdult) parts.add("isAdult: false")
        return parts.joinToString(", ")
    }

    fun activeFilterCount(): Int {
    var count = 0
        if (seasonYear != null) count++
        if (season != null) count++
        if (minScore != null || maxScore != null) count++
        if (studioId != null) count++
        if (source != null) count++
        if (status != null) count++
        if (format != null) count++
        if (genre != null) count++
        if (minEpisodes != null || maxEpisodes != null) count++
        if (onMyList) count++
        return count
    }
}
