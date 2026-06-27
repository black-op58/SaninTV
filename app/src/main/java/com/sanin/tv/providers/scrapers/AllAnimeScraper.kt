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
 * Scraper for AllAnime (api.allanime.day) using its public GraphQL API.
 *
 * Flow:
 *  1. Search for the show by title  → get show _id
 *  2. Request episode source URLs   → get raw/encoded source list
 *  3. Resolve the best source URL   → return direct m3u8 / mp4
 */
object AllAnimeScraper {

    private const val API = "https://api.allanime.day/api"

    suspend fun fetch(
        baseUrl: String,
        title: String,
        episode: Int,
        isDub: Boolean
    ): StreamFetcher.StreamResult? {
        val apiUrl = if (baseUrl.isNotBlank()) "$baseUrl/api" else API
        val showId = searchShow(apiUrl, title) ?: return null
        val lang   = if (isDub) "dub" else "sub"
        return episodeStream(apiUrl, showId, episode.toString(), lang)
    }

    // ── Step 1: search ────────────────────────────────────────────────────────

    private suspend fun searchShow(apiUrl: String, title: String): String? {
        return try {
            val gql = """{ shows(search:{query:"${esc(title)}"}, limit:5, page:1) { edges { _id name } } }"""
            val encoded = URLEncoder.encode(gql, "UTF-8")
            val resp = client.get("$apiUrl?query=$encoded")
            if (resp.statusCode != 200) return null
            Mapper.json.decodeFromString<AllAnimeSearchResp>(resp.text).data?.shows?.edges?.firstOrNull()?._id
        } catch (e: CancellationException) { throw e }
          catch (e: Exception) {
              Logger.log("AllAnimeScraper.searchShow: ${e.message}")
              null
          }
    }

    // ── Step 2: episode sources ───────────────────────────────────────────────

    private suspend fun episodeStream(
        apiUrl: String,
        showId: String,
        episodeStr: String,
        lang: String
    ): StreamFetcher.StreamResult? {
        return try {
            val gql = """{ episode(showId:"${esc(showId)}", translationType:"$lang", episodeString:"$episodeStr") { sourceUrls } }"""
            val encoded = URLEncoder.encode(gql, "UTF-8")
            val resp = client.get("$apiUrl?query=$encoded")
            if (resp.statusCode != 200) return null
            val sources = Mapper.json.decodeFromString<AllAnimeEpisodeResp>(resp.text).data?.episode?.sourceUrls
                ?: return null
            val url = resolveSourceUrl(sources) ?: return null
            StreamFetcher.StreamResult(url = url, quality = "auto", providerName = "AllAnime")
        } catch (e: CancellationException) { throw e }
          catch (e: Exception) {
              Logger.log("AllAnimeScraper.episodeStream: ${e.message}")
              null
          }
    }

    // ── Source resolution ─────────────────────────────────────────────────────

    /**
     * AllAnime returns a list of sourceUrls with symbolic names.
     * We prefer direct HLS/MP4 sources; encoded ones are decoded below.
     */
    private fun resolveSourceUrl(sources: List<AllAnimeSourceUrl>): String? {
        val preferred = listOf("Aw-", "S-mp4", "Luf-mp4", "Kir", "Ok", "Fla")
        for (pref in preferred) {
            val src = sources.find { it.sourceName?.startsWith(pref) == true } ?: continue
            val url = decodeUrl(src.sourceUrl ?: continue) ?: continue
            return url
        }
        return sources.mapNotNull { decodeUrl(it.sourceUrl ?: return@mapNotNull null) }.firstOrNull()
    }

    /**
     * AllAnime encodes some URLs with a simple unicode-escape pattern.
     * Encoded entries start with "--"; decode \\uXXXX sequences then validate.
     */
    private fun decodeUrl(raw: String): String? {
        if (raw.isBlank()) return null
        val text = if (raw.startsWith("--")) {
            buildString {
                var i = 2
                while (i < raw.length) {
                    if (i + 5 < raw.length && raw[i] == '\\' && raw[i + 1] == 'u') {
                        val hex = raw.substring(i + 2, i + 6)
                        append(hex.toIntOrNull(16)?.toChar() ?: raw[i])
                        i += 6
                    } else {
                        append(raw[i])
                        i++
                    }
                }
            }
        } else raw
        return text.takeIf { it.startsWith("http") && (it.contains(".m3u8") || it.contains(".mp4")) }
    }

    private fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"")
}

// ─── Response models ──────────────────────────────────────────────────────────

@Serializable data class AllAnimeSearchResp(val data: AllAnimeSearchData? = null)
@Serializable data class AllAnimeSearchData(val shows: AllAnimeShowList? = null)
@Serializable data class AllAnimeShowList(val edges: List<AllAnimeShow> = emptyList())
@Serializable data class AllAnimeShow(@SerialName("_id") val _id: String = "", val name: String? = null)

@Serializable data class AllAnimeEpisodeResp(val data: AllAnimeEpisodeData? = null)
@Serializable data class AllAnimeEpisodeData(val episode: AllAnimeEpisode? = null)
@Serializable data class AllAnimeEpisode(val sourceUrls: List<AllAnimeSourceUrl> = emptyList())
@Serializable data class AllAnimeSourceUrl(val sourceUrl: String? = null, val sourceName: String? = null)
