package com.sanin.tv.parsers
class OfflineAnimeParser(
private val context: android.content.Context) {

private fun getSubtitleType(path: String): Any {
return when {

//put the title and episdode number in the extra data
val extraData = mutableMapOf<String, String>()                extraData["title"] = animeLink                extraData["episode"] = it.name!!
if (it.isDirectory) {
    val episode = Episode(                        it.name!!,                        getTaskName(animeLink, it.name!!),                        it.name,                        null,                        null,                        extra = extraData,                        sEpisode = SEpisodeImpl()                    )                    episodes.add(episode)                }            }        }        episodes.addAll(loadEpisodesCompat(animeLink, extra, sAnime))        //filter those with the same name
return episodes.distinctBy { it.number }            .sortedBy { MediaNameAdapter.findEpisodeNumber(it.number) }    }

override suspend 
fun loadVideoServers(        episodeLink: String,        extra: Map<String, String>?,        sEpisode: SEpisode    ): List<VideoServer> {
return listOf(            VideoServer(                episodeLink,                offline = true,                extraData = extra            )        )    }

override suspend 
fun search(query: String): List<ShowResponse> {
    private fun determineSubtitleType(url: String): SubtitleType {
return when {            url.lowercase(Locale.ROOT).endsWith("ass") -> SubtitleType.ASS            url.lowercase(Locale.ROOT).endsWith("vtt") -> SubtitleType.VTT
else -> SubtitleType.SRT        }    }}