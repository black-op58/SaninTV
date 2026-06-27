package com.sanin.tv.connections.anilist

import com.sanin.tv.connections.anilist.api.Query
import com.sanin.tv.media.Media
import com.sanin.tv.tryWithSuspend

/**
 * Discover queries — anime-only genre and season browsing.
 * Called by [DiscoverFragment] / [DiscoverViewModel].
 */
suspend fun AnilistQueries.searchAnime(
    query: String,
    filter: com.sanin.tv.feature_search.AdvancedSearchFilter = com.sanin.tv.feature_search.AdvancedSearchFilter.EMPTY,
    page: Int = 1,
    perPage: Int = 30,
): List<com.sanin.tv.media.Media>? = tryWithSuspend {
    val filterArgs = filter.toGraphQLArgs()
    val filterPart = if (filterArgs.isNotBlank()) ", $filterArgs" else ", isAdult: false"

    val gql = """{
        Page(page: $page, perPage: $perPage) {
            media(
                type: ANIME
                search: "$query"
                sort: [SEARCH_MATCH, POPULARITY_DESC]
                $filterPart
            ) {
                id idMal type isAdult status(version: 2) chapters episodes
                nextAiringEpisode { episode }
                meanScore isFavourite format popularity
                bannerImage
                coverImage { large }
                title { english romaji userPreferred }
                mediaListEntry { progress private score(format: POINT_100) status }
                startDate { year }
            }
        }
    }"""

    val response = Anilist.executeQuery<com.sanin.tv.connections.anilist.api.Query.Page>(gql, force = true)
    response?.data?.page?.media?.map { com.sanin.tv.media.Media(it) }
}

suspend fun AnilistQueries.discoverAnime(
    genre: String? = null,
    season: String? = null,
    seasonYear: Int? = null,
    page: Int = 1,
    perPage: Int = 30,
): List<Media>? = tryWithSuspend {
    val genrePart   = if (genre != null) ", genre: \"$genre\"" else ""
    val seasonPart  = if (season != null) ", season: $season" else ""
    val yearPart    = if (seasonYear != null) ", seasonYear: $seasonYear" else ""

    val query = """{
        Page(page: $page, perPage: $perPage) {
            media(
                type: ANIME
                isAdult: false
                sort: [POPULARITY_DESC]
                $genrePart
                $seasonPart
                $yearPart
            ) {
                id idMal type isAdult status(version: 2) chapters episodes
                nextAiringEpisode { episode }
                meanScore isFavourite format popularity
                bannerImage
                coverImage { large }
                title { english romaji userPreferred }
                mediaListEntry { progress private score(format: POINT_100) status }
                startDate { year }
            }
        }
    }"""

    val response = Anilist.executeQuery<Query.Page>(query, force = true)
    response?.data?.page?.media?.map { Media(it) }
}
