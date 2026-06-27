package com.sanin.tv.providers

import com.sanin.tv.providers.scrapers.AllAnimeScraper
import com.sanin.tv.providers.scrapers.AnimePaheScraper
import com.sanin.tv.providers.scrapers.ConsumetScraper
import com.sanin.tv.providers.scrapers.CustomScraper
import com.sanin.tv.util.Logger
import kotlinx.coroutines.CancellationException

/**
 * Orchestrates stream fetching across all enabled [ProviderConfig] entries.
 *
 * Providers are sorted by [ProviderConfig.priority] (ascending) and tried
 * in that order. The first non-null result is returned; on any exception
 * or null result the next provider is tried.
 *
 * Usage:
 * ```
 * val result = StreamFetcher.fetchStreamUrl("Frieren", 52991, 1)
 * if (result != null) exoplayer.load(result.url, result.headers)
 * ```
 */
object StreamFetcher {

    data class StreamResult(
        val url: String,
        val quality: String,
        val headers: Map<String, String> = emptyMap(),
        val providerName: String
    )

    /**
     * Fetch a playable stream URL, trying each enabled provider in priority order.
     * Returns null only when all providers are exhausted.
     */
    suspend fun fetchStreamUrl(
        animeTitle: String,
        malId: Int?,
        episodeNumber: Int,
        isDub: Boolean = false
    ): StreamResult? {
        val providers = ProviderRepository.load()
            .filter { it.enabled }
            .sortedBy { it.priority }

        if (providers.isEmpty()) {
            Logger.log("StreamFetcher: no providers enabled")
            return null
        }

        for (provider in providers) {
            try {
                Logger.log("StreamFetcher: trying ${provider.name} for \"$animeTitle\" ep$episodeNumber")
                val result = fetchFromProvider(provider, animeTitle, malId, episodeNumber, isDub)
                if (result != null) {
                    Logger.log("StreamFetcher: ✓ ${provider.name} → ${result.url.take(80)}")
                    return result
                }
                Logger.log("StreamFetcher: ${provider.name} returned null — trying next")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.log("StreamFetcher: ${provider.name} threw ${e.javaClass.simpleName}: ${e.message}")
            }
        }

        Logger.log("StreamFetcher: all providers exhausted for \"$animeTitle\" ep$episodeNumber")
        return null
    }

    /**
     * Fetch from a single provider. Exposed as [internal] so
     * [ProviderSourcesActivity] can run single-provider tests.
     */
    internal suspend fun fetchFromProvider(
        provider: ProviderConfig,
        title: String,
        malId: Int?,
        episode: Int,
        isDub: Boolean
    ): StreamResult? = when (provider.type) {
        ProviderType.CONSUMET_GOGOANIME ->
            ConsumetScraper.fetchGogoanime(provider.baseUrl, title, episode, isDub)
        ProviderType.CONSUMET_ZORO ->
            ConsumetScraper.fetchZoro(provider.baseUrl, title, episode, isDub)
        ProviderType.ALL_ANIME ->
            AllAnimeScraper.fetch(provider.baseUrl, title, episode, isDub)
        ProviderType.ANIME_PAHE ->
            AnimePaheScraper.fetch(provider.baseUrl, title, episode)
        ProviderType.CUSTOM ->
            CustomScraper.fetch(provider.baseUrl, title, episode)
    }
}
