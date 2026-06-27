package com.sanin.tv.media
class MediaDetailsViewModel : androidx.lifecycle.ViewModel() {

private fun loadExtras() {
viewModelScope.launch {

val fullMapped = Media(fullData, isAnime)
if (media.description.isNullOrBlank() && !fullMapped.description.isNullOrBlank()) {                    media.description = fullMapped.description                }
if (fullMapped.synonyms.isNotEmpty()) media.synonyms = fullMapped.synonyms
if (fullMapped.genres.isNotEmpty()) media.genres = fullMapped.genres
if (!fullMapped.externalLinks.isNullOrEmpty()) media.externalLinks = fullMapped.externalLinks
if ((media.meanScore == null || media.meanScore == 0) && fullMapped.meanScore != null) {                    media.meanScore = fullMapped.meanScore                }
if (media.source.isNullOrBlank() && !fullMapped.source.isNullOrBlank()) {                    media.source = fullMapped.source                }
if (!fullMapped.relations.isNullOrEmpty()) {
if (media.relations.isNullOrEmpty() || (fullMapped.relations?.size ?: 0) > (media.relations?.size ?: 0)) {                        media.relations = fullMapped.relations                    }
if (media.prequel == null) media.prequel = fullMapped.prequel
if (media.sequel == null) media.sequel = fullMapped.sequel                }
if (!fullMapped.staff.isNullOrEmpty()) {                    media.staff = ArrayList(                        ((media.staff ?: arrayListOf()) + fullMapped.staff!!).distinctBy { it.id }                    )                }
if (!fullMapped.recommendations.isNullOrEmpty() &&                    (fullMapped.recommendations?.size ?: 0) > (media.recommendations?.size ?: 0)) {                    media.recommendations = fullMapped.recommendations                }
if (!fullMapped.trailer.isNullOrBlank()) media.trailer = fullMapped.trailer
if (isAnime) {                    fullMapped.anime?.let { anime ->
if (anime.op.isNotEmpty()) media.anime?.op = anime.op
if (anime.ed.isNotEmpty()) media.anime?.ed = anime.ed                        anime.mainStudio?.let { media.anime?.mainStudio = it }
if (!anime.producers.isNullOrEmpty()) media.anime?.producers = anime.producers                        anime.season?.let { media.anime?.season = it }                        anime.seasonYear?.let { media.anime?.seasonYear = it }
if (media.anime?.nextAiringEpisodeTime == null && anime.nextAiringEpisodeTime != null) {                            media.anime?.nextAiringEpisodeTime = anime.nextAiringEpisodeTime                }            }

val mappedCharacters = charactersDeferred.await()                .mapNotNull { jChar ->                    
val character = jChar.character ?: return@mapNotNull null                    Character(                        id = character.malId,                        name = character.name,                        image = character.images?.jpg?.largeImageUrl ?: character.images?.jpg?.imageUrl,                        banner = media.banner ?: media.cover,                        role = jChar.role ?: "",                        isFav = false,                        voiceActor = jChar.voiceActors                            ?.mapNotNull { va ->                                va.person?.let { person ->                                    Author(                                        id = person.malId,                                        name = person.name,                                        image = person.images?.jpg?.largeImageUrl ?: person.images?.jpg?.imageUrl,                                        role = va.language                                    )                                }                            }                            ?.let { ArrayList(it) }                    )                }
if (mappedCharacters.isNotEmpty()) {                media.characters = ArrayList(mappedCharacters.distinctBy { it.id })            }

val mappedStaff = staffDeferred.await()                .mapNotNull { staff ->                    
val person = staff.person ?: return@mapNotNull null                    Author(                        id = person.malId,                        name = person.name,                        image = person.images?.jpg?.largeImageUrl ?: person.images?.jpg?.imageUrl,                        role = staff.positions?.joinToString(", ")                    )                                }                            }
if (resolvedFmt != null) {                                rel.format = resolvedFmt
val rawRelation = rel.relation?.substringBefore("\n") ?: ""
if (rawRelation.isNotEmpty()) {                                    rel.relation = "$rawRelation\n$resolvedFmt"                                }                            }                        }                    } catch (_: Exception) {}                }            }            deferreds.forEach { it.await() }        }    }

fun setMedia(m: Media) {        media.postValue(m)
if (!PrefManager.getVal<Boolean>(PrefName.RescueMode)) {            viewModelScope.launch(Dispatchers.IO) {
try {
if (m.idIMDB == null) {                        m.idIMDB = com.sanin.tv.others.IdMappers.getImdbId(m.id)                    }                } catch (e: Exception) {                    com.sanin.tv.util.Logger.log(e)                }            }        }    }

val responses = MutableLiveData<List<ShowResponse>?>(null)    //Anime    
private val kitsuEpisodes: MutableLiveData<Map<String, Episode>> =        MutableLiveData<Map<String, Episode>>(null)    
fun getKitsuEpisodes(): LiveData<Map<String, Episode>> = kitsuEpisodes    suspend 
fun loadKitsuEpisodes(s: Media) {        tryWithSuspend {
if (kitsuEpisodes.value == null) kitsuEpisodes.postValue(Kitsu.getKitsuEpisodesDetails(s))        }    }

private val anifyEpisodes: MutableLiveData<Map<String, Episode>> =        MutableLiveData<Map<String, Episode>>(null)    
fun getAnifyEpisodes(): LiveData<Map<String, Episode>> = anifyEpisodes    suspend 
fun loadAnifyEpisodes(s: Int) {        tryWithSuspend {
if (anifyEpisodes.value == null) anifyEpisodes.postValue(Anify.fetchAndParseMetadata(s))        }    }

private val fillerEpisodes: MutableLiveData<Map<String, Episode>> =        MutableLiveData<Map<String, Episode>>(null)    
fun getFillerEpisodes(): LiveData<Map<String, Episode>> = fillerEpisodes    suspend 
fun loadFillerEpisodes(s: Media) {        tryWithSuspend {
if (fillerEpisodes.value == null) fillerEpisodes.postValue(                Jikan.getEpisodes(                    s.idMAL ?: return@tryWithSuspend                )            )        }    }

var watchSources: WatchSources? = null    
private val episodes = MutableLiveData<MutableMap<Int, MutableMap<String, Episode>>>(null)    
private val epsLoaded = mutableMapOf<Int, MutableMap<String, Episode>>()    
fun getEpisodes(): LiveData<MutableMap<Int, MutableMap<String, Episode>>> = episodes    suspend 
fun loadEpisodes(media: Media, i: Int, invalidate: Boolean = false) {
if (!epsLoaded.containsKey(i) || invalidate) {            epsLoaded[i] = watchSources?.loadEpisodesFromMedia(i, media) ?: return        }        episodes.postValue(epsLoaded)    }    suspend 
fun forceLoadEpisode(media: Media, i: Int) {        epsLoaded[i] = watchSources?.loadEpisodesFromMedia(i, media) ?: return        episodes.postValue(epsLoaded)    }    suspend 
fun overrideEpisodes(i: Int, source: ShowResponse, id: Int) {        watchSources?.saveResponse(i, id, source)        epsLoaded[i] =            watchSources?.loadEpisodes(i, source.link, source.extra, source.sAnime) ?: return        episodes.postValue(epsLoaded)    }

private var episode = MutableLiveData<Episode?>(null)    
fun getEpisode(): LiveData<Episode?> = episode    suspend 
fun loadEpisodeVideos(ep: Episode, i: Int, post: Boolean = true) {
    val link = ep.link ?: return
if (!ep.allStreams || ep.extractors.isNullOrEmpty()) {
    val existingExtractors = ep.extractors?.toMutableList() ?: mutableListOf()            
val list = mutableListOf<VideoExtractor>()            ep.extractors = list            watchSources?.get(i)?.apply {
if (!post && !allowsPreloading) return@apply                ep.sEpisode?.let {                    loadByVideoServers(link, ep.extra, it) { extractor ->
if (extractor.videos.isNotEmpty()) {                            list.add(extractor)                            ep.extractorCallback?.invoke(extractor)                        }                    }                }                ep.extractorCallback = null
if (list.isNotEmpty())                    ep.allStreams = true
else if (existingExtractors.isNotEmpty())                    ep.extractors = existingExtractors            }        }
if (post) {            episode.postValue(ep)            MainScope().launch(Dispatchers.Main) {                episode.value = null            }        }    }

val timeStamps = MutableLiveData<List<AniSkip.Stamp>?>()    
private val timeStampsMap: MutableMap<Int, List<AniSkip.Stamp>?> = mutableMapOf()    suspend 
fun loadTimeStamps(        malId: Int?,        episodeNum: Int?,        duration: Long,        useProxyForTimeStamps: Boolean,        extensionTimestamps: List<eu.kanade.tachiyomi.animesource.model.TimeStamp> = emptyList()    ) {        episodeNum ?: return
if (timeStampsMap.containsKey(episodeNum))
return timeStamps.postValue(timeStampsMap[episodeNum])        // Extension timestamps take priority
fall back to AniSkip when the extension has none
val result: List<AniSkip.Stamp>? = if (extensionTimestamps.isNotEmpty()) {            extensionTimestamps.map { it.toAniSkipStamp() }
} else if (malId != null) {            AniSkip.getResult(malId, episodeNum, duration, useProxyForTimeStamps)
} else {            null        }        timeStampsMap[episodeNum] = result        timeStamps.postValue(result)    }

private fun eu.kanade.tachiyomi.animesource.model.TimeStamp.toAniSkipStamp(): AniSkip.Stamp {
    val skipType = when (type) {            eu.kanade.tachiyomi.animesource.model.ChapterType.Opening -> "op"            eu.kanade.tachiyomi.animesource.model.ChapterType.Ending -> "ed"            eu.kanade.tachiyomi.animesource.model.ChapterType.Recap -> "recap"    suspend 
fun searchNovels(query: String, i: Int) {
    val position = if (i >= novelSources.list.size) 0 else i
val source = novelSources[position]        tryWithSuspend(post = true) {
if (source != null) {                novelResponses.postValue(source.search(query))            }        }    }    suspend 
fun autoSearchNovels(media: Media) {
    val source = novelSources[media.selected?.sourceIndex ?: 0]        tryWithSuspend(post = true) {
if (source != null) {                novelResponses.postValue(source.sortedSearch(media))            }        }    }    suspend 
fun loadNovelChapters(media: Media, i: Int, invalidate: Boolean = false) {
if (!novelLoaded.containsKey(i) || invalidate) {            tryWithSuspend {
    val source = novelSources[i]
if (source == null) {                    novelLoaded[i] = emptyList()                    return@tryWithSuspend                }

val novelResponse = source.autoSearch(media)
if (novelResponse == null) {                    novelLoaded[i] = emptyList()                    return@tryWithSuspend                }

val book = source.loadBook(novelResponse.link, novelResponse.extra)
if (book == null || book.links.isEmpty()) {                    novelLoaded[i] = emptyList()                    return@tryWithSuspend                }

val chapterResponses = book.links.mapIndexed { index, fileUrl ->                    
val chapterName = fileUrl.headers?.get("X-Chapter-Name") ?: "Chapter ${index + 1}"                    
val releaseTime = fileUrl.headers?.get("X-Release-Time")                    
val chapterNumber = fileUrl.headers?.get("X-Chapter-Number")                    ShowResponse(                        name = chapterName,                        link = fileUrl.url,                        coverUrl = novelResponse.coverUrl,                        extra = mutableMapOf<String, String>().apply {                            releaseTime?.let { put("releaseTime", it) }                            chapterNumber?.let { put("chapterNumber", it) }                            put("sourceName", source.name)                        }                    )                }                novelLoaded[i] = chapterResponses            }        }        novelChapters.postValue(novelLoaded)    }    suspend 
fun overrideNovelChapters(i: Int, source: ShowResponse, mediaId: Int) {        novelSources.saveResponse(i, mediaId, source)        novelLoaded.remove(i)    }

val book: MutableLiveData<Book> = MutableLiveData(null)    suspend 
fun loadBook(novel: ShowResponse, i: Int) {        tryWithSuspend {            book.postValue(                novelSources[i]?.loadBook(novel.link, novel.extra) ?: return@tryWithSuspend            )        }    }

private val fetchedOnlineSubtitles = mutableMapOf<String, List<Any>>()    
fun saveFetchedSubtitles(id: String, subs: List<Any>) {        fetchedOnlineSubtitles[id] = subs    }

fun getFetchedSubtitles(id: String): List<Any>? {