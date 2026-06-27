package com.sanin.tv.parsers

import com.sanin.tv.media.Media
import eu.kanade.tachiyomi.animesource.model.SAnime

abstract class WatchSources : BaseSources() {

    val size: Int get() = list.size

    open suspend fun loadEpisodesFromMedia(i: Int, media: Media): MutableMap<String, Episode> {
        val parser = get(i) as? AnimeParser ?: return mutableMapOf()
        val saved = parser.loadSavedShowResponse(media.id)
        val response = saved ?: parser.autoSearch(media) ?: return mutableMapOf()
        if (saved == null) parser.saveShowResponse(media.id, response, true)
        return loadEpisodes(i, response.link, response.extra, response.sAnime)
    }

    open suspend fun loadEpisodes(
        i: Int,
        link: String,
        extra: Map<String, String>?,
        sAnime: SAnime?
    ): MutableMap<String, Episode> {
        val parser = get(i) as? AnimeParser ?: return mutableMapOf()
        return parser.loadEpisodes(link, extra, sAnime ?: SAnime.create())
            .associateBy { it.number }
            .toMutableMap()
    }
}
