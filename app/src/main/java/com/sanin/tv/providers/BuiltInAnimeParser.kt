package com.sanin.tv.providers

import com.sanin.tv.FileUrl
import com.sanin.tv.media.Media
import com.sanin.tv.parsers.AnimeParser
import com.sanin.tv.parsers.Episode
import com.sanin.tv.parsers.VideoContainer
import com.sanin.tv.parsers.VideoExtractor
import com.sanin.tv.parsers.VideoServer
import com.sanin.tv.parsers.VideoType
import com.sanin.tv.parsers.Video
import com.sanin.tv.util.Logger
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.ShowResponse

/**
 * A built-in [AnimeParser] that delegates episode stream resolution to
 * [StreamFetcher]. It appears as the "Built-In" source in the watch
 * screen and works without any installed extension.
 *
 * Episode stubs are generated from the known episode count, and the
 * actual stream URL is resolved lazily when the user taps an episode.
 */
class BuiltInAnimeParser : AnimeParser() {

    override val name = "Built-In"
    override val saveName = "BuiltIn"
    override val hostUrl = "https://consumet.org"
    override val isNSFW = false
    override val malSyncBackupName = ""

    // ──────────────────────────────────────────────────────────────────────
    // Auto-search — produce a ShowResponse whose link/extra encodes the
    // title and MAL-ID so they are available during loadEpisodes().
    // ──────────────────────────────────────────────────────────────────────

    override suspend fun autoSearch(mediaObj: Media): ShowResponse? {
        val title = mediaObj.mainName() ?: mediaObj.name ?: return null
        val malId = mediaObj.idMAL

        // Best-effort episode count: use nextAiringEpisode - 1 if available,
        // otherwise fall back to a safe default of 24 (user will see all stubs).
        val epCount = (mediaObj.anime?.nextAiringEpisode?.minus(1))
            ?.takeIf { it > 0 }
            ?: 24

        val extra = mutableMapOf(
            "malId" to (malId?.toString() ?: ""),
            "lastEp" to epCount.toString()
        )

        return ShowResponse(
            name = title,
            link = title,
            coverUrl = FileUrl(mediaObj.cover ?: ""),
            extra = extra,
            sAnime = SAnime.create().apply { this.title = title }
        )
    }

    // ──────────────────────────────────────────────────────────────────────
    // Episode list — return numbered stubs 1..lastEp.  The heavy lifting
    // (stream resolution) happens in loadVideoServers().
    // ──────────────────────────────────────────────────────────────────────

    override suspend fun loadEpisodes(
        animeLink: String,
        extra: Map<String, String>?,
        sAnime: SAnime
    ): List<Episode> {
        val lastEp = extra?.get("lastEp")?.toIntOrNull() ?: 24
        val malId = extra?.get("malId") ?: ""

        Logger.log("BuiltInAnimeParser: generating $lastEp episode stubs for \"$animeLink\"")

        return (1..lastEp).map { epNum ->
            Episode(
                number = epNum.toString(),
                link = epNum.toString(),
                title = "Episode $epNum",
                thumbnail = null as FileUrl?,
                extra = mapOf(
                    "title" to animeLink,
                    "malId" to malId
                )
            )
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Video servers — call StreamFetcher and wrap the result.
    // episodeLink is the episode number string written in loadEpisodes().
    // ──────────────────────────────────────────────────────────────────────

    override suspend fun loadVideoServers(
        episodeLink: String,
        extra: Map<String, String>?,
        sEpisode: SEpisode
    ): List<VideoServer> {
        val epNum = episodeLink.toIntOrNull() ?: 1
        val title = extra?.get("title") ?: ""
        val malId = extra?.get("malId")?.toIntOrNull()

        Logger.log("BuiltInAnimeParser: fetching stream for \"$title\" ep$epNum (mal=$malId)")

        val result = StreamFetcher.fetchStreamUrl(title, malId, epNum, selectDub)
            ?: return emptyList()

        return listOf(
            VideoServer(
                name = result.providerName,
                embed = FileUrl(url = result.url, headers = result.headers),
                extraData = mapOf("providerName" to result.providerName)
            )
        )
    }

    // ──────────────────────────────────────────────────────────────────────
    // Video extractor — since we already have the direct stream URL from
    // StreamFetcher, we use a thin extractor that just returns it.
    // ──────────────────────────────────────────────────────────────────────

    override suspend fun getVideoExtractor(server: VideoServer): VideoExtractor =
        BuiltInVideoExtractor(server)

    override val allowsPreloading = false
}

// ────────────────────────────────────────────────────────────────────────────
// Simple extractor: the embed URL is already the playable stream URL.
// ────────────────────────────────────────────────────────────────────────────

class BuiltInVideoExtractor(private val videoServer: VideoServer) : VideoExtractor() {

    override val server: VideoServer get() = videoServer

    override suspend fun extract(): VideoContainer {
        val url = videoServer.embed.url
        val headers = videoServer.embed.headers

        val videoType = when {
            url.contains(".m3u8", ignoreCase = true) -> VideoType.M3U8
            url.contains(".mp4", ignoreCase = true) -> VideoType.CONTAINER
            url.contains(".mkv", ignoreCase = true) -> VideoType.CONTAINER
            url.contains(".dash", ignoreCase = true) -> VideoType.DASH
            else -> VideoType.M3U8
        }

        val video = Video(
            quality = null,
            format = videoType,
            file = FileUrl(url = url, headers = headers)
        )

        return VideoContainer(listOf(video))
    }
}
