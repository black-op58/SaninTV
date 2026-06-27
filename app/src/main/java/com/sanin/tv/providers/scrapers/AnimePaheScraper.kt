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
 * Scraper for AnimePahe using its public JSON API.
 *
 * Flow:
 *  1. Search anime by title        → find session + id
 *  2. Fetch episode release list   → find episode session
 *  3. Fetch kwik links             → resolve m3u8 URL
 */
object AnimePaheScraper {

    suspend fun fetch(
        baseUrl: String,
        title: String,
        episode: Int
    ): StreamFetcher.StreamResult? {
        val base = baseUrl.trimEnd('/')
        val show = searchShow(base, title) ?: return null
        val epSession = findEpisodeSession(base, show.session, episode) ?: return null
        val streamUrl = resolveStream(base, show.id, epSession) ?: return null
        return StreamFetcher.StreamResult(
            url          = streamUrl,
            quality      = "720",
            providerName = "AnimePahe"
        )
    }

    // ── Step 1: search ────────────────────────────────────────────────────────

    private suspend fun searchShow(base: String, title: String): AnimePaheShow? {
        return try {
            val q = URLEncoder.encode(title, "UTF-8")
            val resp = client.get(
                "$base/api?m=search&q=$q",
                headers = mapOf("Referer" to "$base/")
            )
            if (resp.statusCode != 200) return null
            Mapper.json.decodeFromString<AnimePaheSearchResp>(resp.text).data.firstOrNull()
        } catch (e: CancellationException) { throw e }
          catch (e: Exception) {
              Logger.log("AnimePaheScraper.searchShow: ${e.message}")
              null
          }
    }

    // ── Step 2: episode list ──────────────────────────────────────────────────

    private suspend fun findEpisodeSession(base: String, showSession: String, episode: Int): String? {
        val page = ((episode - 1) / 30) + 1
        return try {
            val resp = client.get(
                "$base/api?m=release&id=$showSession&sort=episode_asc&page=$page",
                headers = mapOf("Referer" to "$base/")
            )
            if (resp.statusCode != 200) return null
            Mapper.json.decodeFromString<AnimePaheReleaseResp>(resp.text)
                .data.find { it.episode == episode }?.session
        } catch (e: CancellationException) { throw e }
          catch (e: Exception) {
              Logger.log("AnimePaheScraper.findEpisodeSession: ${e.message}")
              null
          }
    }

    // ── Step 3: stream URL ────────────────────────────────────────────────────

    private suspend fun resolveStream(base: String, showId: Int, epSession: String): String? {
        return try {
            val resp = client.get(
                "$base/api?m=links&id=$showId&session=$epSession&p=kwik",
                headers = mapOf("Referer" to "$base/")
            )
            if (resp.statusCode != 200) return null
            val links = Mapper.json.decodeFromString<AnimePaheLinksResp>(resp.text)
            val best = links.data.values.flatten().maxByOrNull { qualityScore(it.quality) }
                ?: return null
            extractKwikStream(base, best)
        } catch (e: CancellationException) { throw e }
          catch (e: Exception) {
              Logger.log("AnimePaheScraper.resolveStream: ${e.message}")
              null
          }
    }

    /**
     * Kwik uses a JS-obfuscated page to hide the direct m3u8.
     * We follow the link and extract the stream URL via regex.
     */
    private suspend fun extractKwikStream(base: String, link: AnimePaheLink): String? {
        val hls = link.hls
        if (!hls.isNullOrBlank() && hls.startsWith("http") && "m3u8" in hls) return hls
        val kwikUrl = link.kwik ?: link.kwikPahewin ?: return null
        return try {
            val resp = client.get(kwikUrl, headers = mapOf("Referer" to "$base/"))
            val m3u8Regex = Regex("""source\s*=\s*['"]([^'"]+\.m3u8[^'"]*)['"]""")
            m3u8Regex.find(resp.text)?.groupValues?.getOrNull(1)
        } catch (e: CancellationException) { throw e }
          catch (e: Exception) { null }
    }

    private fun qualityScore(q: String?): Int = when {
        q == null     -> 0
        "1080" in q   -> 1080
        "720"  in q   -> 720
        "480"  in q   -> 480
        "360"  in q   -> 360
        else          -> 0
    }
}

// ─── Response models ──────────────────────────────────────────────────────────

@Serializable data class AnimePaheSearchResp(val data: List<AnimePaheShow> = emptyList())
@Serializable data class AnimePaheShow(
    val id: Int = 0,
    val title: String = "",
    val session: String = ""
)

@Serializable data class AnimePaheReleaseResp(val data: List<AnimePaheEpisode> = emptyList())
@Serializable data class AnimePaheEpisode(
    @SerialName("episode") val episode: Int = 0,
    val session: String = ""
)

@Serializable data class AnimePaheLinksResp(val data: Map<String, List<AnimePaheLink>> = emptyMap())
@Serializable data class AnimePaheLink(
    val quality: String? = null,
    val hls: String? = null,
    val kwik: String? = null,
    @SerialName("kwik_pahewin") val kwikPahewin: String? = null
)
