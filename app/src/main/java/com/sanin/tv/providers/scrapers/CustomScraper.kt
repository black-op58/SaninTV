package com.sanin.tv.providers.scrapers

import com.sanin.tv.Mapper
import com.sanin.tv.client
import com.sanin.tv.providers.StreamFetcher
import com.sanin.tv.util.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import java.net.URLEncoder

/**
 * Generic scraper for user-supplied API endpoints.
 *
 * The [urlTemplate] may contain:
 *   {title}   — replaced with URL-encoded anime title
 *   {episode} — replaced with the episode number
 *
 * The endpoint must return JSON containing any of these top-level keys:
 *   url | stream | src | link | hls | file
 * with a value that is a direct playable URL (http/https).
 */
object CustomScraper {

    suspend fun fetch(urlTemplate: String, title: String, episode: Int): StreamFetcher.StreamResult? {
        return try {
            val encodedTitle = URLEncoder.encode(title, "UTF-8")
            val url = urlTemplate
                .replace("{title}", encodedTitle)
                .replace("{episode}", episode.toString())
            val resp = client.get(url)
            if (resp.statusCode != 200) return null
            val obj = runCatching {
                Mapper.json.parseToJsonElement(resp.text).jsonObject
            }.getOrNull() ?: return null
            val streamUrl = pickUrl(obj) ?: return null
            StreamFetcher.StreamResult(url = streamUrl, quality = "custom", providerName = "Custom")
        } catch (e: CancellationException) { throw e }
          catch (e: Exception) {
              Logger.log("CustomScraper: ${e.message}")
              null
          }
    }

    private val URL_KEYS = listOf("url", "stream", "src", "link", "hls", "file", "videoUrl", "streamUrl")

    private fun pickUrl(obj: JsonObject): String? {
        for (key in URL_KEYS) {
            val el = obj[key] as? JsonPrimitive ?: continue
            val s  = el.content
            if (s.startsWith("http")) return s
        }
        return null
    }
}
