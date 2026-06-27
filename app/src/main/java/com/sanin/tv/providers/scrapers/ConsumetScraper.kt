package com.sanin.tv.providers.scrapers

import com.sanin.tv.Mapper
import com.sanin.tv.client
import com.sanin.tv.providers.StreamFetcher
import com.sanin.tv.util.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URLEncoder

/**
 * Scraper that uses the Consumet REST API to fetch streams from
 * Gogoanime and Zoro (Aniwatch).
 *
 * Consumet exposes:
 *  GET /{provider}/{title}                   — search
 *  GET /{provider}/info/{id}                 — episode list
 *  GET /{provider}/watch/{episodeId}         — stream URLs
 *
 * The base URL is user-configurable so self-hosted Consumet instances work.
 */
object ConsumetScraper {

    // ── Gogoanime ─────────────────────────────────────────────────────────────

    suspend fun fetchGogoanime(
        baseUrl: String,
        title: String,
        episode: Int,
        isDub: Boolean
    ): StreamFetcher.StreamResult? {
        val searchTitle = if (isDub) "$title (Dub)" else title
        val id = searchId("$baseUrl/anime/gogoanime", searchTitle) ?: return null
        val episodeId = gogoanimeEpisodeId("$baseUrl/anime/gogoanime/info/$id", episode) ?: return null
        return streamFromWatch("$baseUrl/anime/gogoanime/watch/$episodeId", "Gogoanime")
    }

    // ── Zoro / Aniwatch ───────────────────────────────────────────────────────

    suspend fun fetchZoro(
        baseUrl: String,
        title: String,
        episode: Int,
        isDub: Boolean
    ): StreamFetcher.StreamResult? {
        val id = searchId("$baseUrl/anime/zoro", title) ?: return null
        val episodeId = zoroEpisodeId("$baseUrl/anime/zoro/info/$id", episode, isDub) ?: return null
        return streamFromWatch("$baseUrl/anime/zoro/watch/$episodeId", "Zoro")
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    private suspend fun searchId(searchBase: String, title: String): String? {
        return try {
            val encoded = URLEncoder.encode(title, "UTF-8")
            val resp = client.get("$searchBase/$encoded")
            if (resp.statusCode != 200) return null
            Mapper.json.decodeFromString<ConsumetSearchResp>(resp.text).results.firstOrNull()?.id
        } catch (e: CancellationException) { throw e }
          catch (e: Exception) {
              Logger.log("ConsumetScraper.searchId: ${e.message}")
              null
          }
    }

    private suspend fun gogoanimeEpisodeId(infoUrl: String, episodeNumber: Int): String? {
        return try {
            val resp = client.get(infoUrl)
            if (resp.statusCode != 200) return null
            val episodes = Mapper.json.decodeFromString<ConsumetInfoResp>(resp.text).episodes
            episodes.find { it.number == episodeNumber }?.id
                ?: episodes.getOrNull(episodeNumber - 1)?.id
        } catch (e: CancellationException) { throw e }
          catch (e: Exception) {
              Logger.log("ConsumetScraper.gogoanimeEpisodeId: ${e.message}")
              null
          }
    }

    private suspend fun zoroEpisodeId(infoUrl: String, episodeNumber: Int, isDub: Boolean): String? {
        return try {
            val resp = client.get(infoUrl)
            if (resp.statusCode != 200) return null
            val episodes = Mapper.json.decodeFromString<ConsumetInfoResp>(resp.text).episodes
            val pool = if (isDub) episodes.filter { it.isDub == true }.takeIf { it.isNotEmpty() } ?: episodes else episodes
            pool.find { it.number == episodeNumber }?.id
                ?: episodes.getOrNull(episodeNumber - 1)?.id
        } catch (e: CancellationException) { throw e }
          catch (e: Exception) {
              Logger.log("ConsumetScraper.zoroEpisodeId: ${e.message}")
              null
          }
    }

    private suspend fun streamFromWatch(watchUrl: String, providerName: String): StreamFetcher.StreamResult? {
        return try {
            val resp = client.get(watchUrl)
            if (resp.statusCode != 200) return null
            val body = Mapper.json.decodeFromString<ConsumetWatchResp>(resp.text)
            val best = body.sources.maxByOrNull { qualityScore(it.quality) } ?: return null
            StreamFetcher.StreamResult(
                url          = best.url,
                quality      = best.quality ?: "auto",
                headers      = body.headers ?: emptyMap(),
                providerName = providerName
            )
        } catch (e: CancellationException) { throw e }
          catch (e: Exception) {
              Logger.log("ConsumetScraper.streamFromWatch[$providerName]: ${e.message}")
              null
          }
    }

    private fun qualityScore(quality: String?): Int = when {
        quality == null          -> 0
        "1080" in quality        -> 1080
        "720"  in quality        -> 720
        "480"  in quality        -> 480
        "360"  in quality        -> 360
        quality == "default"     -> 900
        quality == "auto"        -> 800
        quality == "backup"      -> 100
        else                     -> 50
    }
}

// ─── Response models ──────────────────────────────────────────────────────────

@Serializable
data class ConsumetSearchResp(val results: List<ConsumetSearchItem> = emptyList())

@Serializable
data class ConsumetSearchItem(val id: String = "", val title: String? = null)

@Serializable
data class ConsumetInfoResp(val episodes: List<ConsumetEpisode> = emptyList())

@Serializable
data class ConsumetEpisode(
    val id: String = "",
    val number: Int = 0,
    @SerialName("isDubbed") val isDub: Boolean? = null
)

@Serializable
data class ConsumetWatchResp(
    val sources: List<ConsumetSource> = emptyList(),
    val headers: Map<String, String>? = null
)

@Serializable
data class ConsumetSource(
    val url: String = "",
    val quality: String? = null,
    val isM3U8: Boolean = false
)
