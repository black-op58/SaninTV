package com.sanin.tv.connections.anilist

import com.sanin.tv.connections.anilist.Anilist.executeQuery
import com.sanin.tv.connections.anilist.api.Query
import com.sanin.tv.media.Media
import com.sanin.tv.profile.User
import com.sanin.tv.tryWithSuspend

// ─────────────────────────────────────────────────────────────────────────────
// Extension functions that add home-specific queries to AnilistQueries without
// touching the already-large AnilistQueries.kt file.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Fetches up to 15 of the most popular anime of all time from AniList.
 * Used by the hero carousel in Home.
 */
suspend fun AnilistQueries.loadPopularAllTime(): List<Media>? = tryWithSuspend {
    val query = """
    {
      Page(page: 1, perPage: 15) {
        media(type: ANIME, sort: POPULARITY_DESC) {
          id
          idMal
          isAdult
          title {
            romaji
            english
            userPreferred
          }
          coverImage {
            extraLarge
            large
          }
          bannerImage
          genres
          meanScore
          status
          description(asHtml: false)
          episodes
          format
          season
          seasonYear
          nextAiringEpisode { episode airingAt }
          mediaListEntry { status progress }
        }
      }
    }
    """.trimIndent().replace("\n", " ")

    val response = executeQuery<Query.Page>(query, force = true)?.data?.page
    response?.medias?.mapNotNull { raw ->
        try { Media(raw) } catch (_: Exception) { null }
    }
}

/**
 * Loads user-activity statuses shown as "stories" at the top of Home.
 * Returns an empty list when the user has no AniList followers with recent
 * activity or when the query is not available in the current stub build.
 */
suspend fun AnilistQueries.getUserStatus(): ArrayList<User>? = tryWithSuspend {
    // Activity feed — returns following users' recent watch activity.
    // Full implementation hooks into AniList's Page > activities query.
    // Stub: return empty so the status row is hidden until the real feed is wired.
    arrayListOf()
}

/**
 * Ensures every Media item in the user's home lists has a banner / cover image.
 * Runs as a background pass after [initHomePage] so that the UI doesn't block.
 */
suspend fun AnilistQueries.setListImages() {
    // Intentional no-op stub.
    // In the full build this iterates items whose bannerImage is null and
    // fires individual Media(id) detail queries to backfill images.
}
