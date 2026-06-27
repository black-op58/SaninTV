package com.sanin.tv.media

val nameRomaji: String,    
val userPreferredName: String,    
var cover: String? = null,    
var banner: String? = null,    
var logo: String? = null,    
var relation: String? = null,    
var favourites: Int? = null,    
var isAdult: Boolean,    
var isFav: Boolean = false,    
var notify: Boolean = false,    
var userListId: Int? = null,    
var isListPrivate: Boolean = false,    
var notes: String? = null,    
var userProgress: Int? = null,    
var userProgressVolumes: Int? = null,    
var userStatus: String? = null,    
var userScore: Int = 0,    
var userRepeat: Int = 0,    
var userUpdatedAt: Long? = null,    
var userStartedAt: FuzzyDate = FuzzyDate(),    
var userCompletedAt: FuzzyDate = FuzzyDate(),    
var inCustomListsOf: MutableMap<String, Boolean>? = null,    
var userFavOrder: Int? = null,    
var status: String? = null,    
var format: String? = null,    
var source: String? = null,    
var countryOfOrigin: String? = null,    
var meanScore: Int? = null,    
var genres: ArrayList<String> = arrayListOf(),    
var tags: ArrayList<String> = arrayListOf(),    
var description: String? = null,    
var synonyms: ArrayList<String> = arrayListOf(),    
var trailer: String? = null,    
var startDate: FuzzyDate? = null,    
var endDate: FuzzyDate? = null,    
var popularity: Int? = null,    
var timeUntilAiring: Long? = null,    
var characters: ArrayList<Character>? = null,    
var review: ArrayList<Query.Review>? = null,    
var staff: ArrayList<Author>? = null,    
var prequel: Media? = null,    
var sequel: Media? = null,    
var relations: ArrayList<Media>? = null,    
var recommendations: ArrayList<Media>? = null,    
var users: ArrayList<User>? = null,    
var vrvId: String? = null,    
var crunchySlug: String? = null,    
var nameMAL: String? = null,    
var folderName: String? = null,    
var shareLink: String? = null,    
var selected: Selected? = null,    
var streamingEpisodes: List<MediaStreamingEpisode>? = null,    
var idKitsu: String? = null,    
var externalLinks: ArrayList<MediaExternalLink>? = null,    
var idIMDB: String? = null,    
var cameFromContinue: Boolean = false) : Serializable {    constructor(apiMedia: ApiMedia) : this(        id = apiMedia.id,        idMAL = apiMedia.idMal,        popularity = apiMedia.popularity,        name = apiMedia.title!!.english,        nameRomaji = apiMedia.title!!.romaji,        userPreferredName = apiMedia.title!!.userPreferred,        cover = apiMedia.coverImage?.large ?: apiMedia.coverImage?.medium,        banner = apiMedia.bannerImage,        status = apiMedia.status.toString(),        isFav = apiMedia.isFavourite!!,        isAdult = apiMedia.isAdult ?: false,        isListPrivate = apiMedia.mediaListEntry?.
private ?: false,        userProgress = apiMedia.mediaListEntry?.progress,        userProgressVolumes = apiMedia.mediaListEntry?.progressVolumes,        userPreferredName = node.alternativeTitles?.en ?: node.title,        cover = node.mainPicture?.large ?: node.mainPicture?.medium,        banner = node.mainPicture?.large,        status = when (node.status?.lowercase()) {            "currently_airing", "currently_publishing" -> "RELEASING"            "finished_airing", "finished" -> "FINISHED"            "not_yet_aired", "not_yet_published" -> "NOT_YET_RELEASED"            "on_hiatus" -> "HIATUS"            "discontinued" -> "CANCELLED"
else -> node.status?.replace("_", " ")?.uppercase()        },        isAdult = node.rating == "rx",        meanScore = node.mean?.times(10)?.toInt(),        popularity = node.popularity,        format = node.mediaType?.uppercase(),        source = node.source?.replace("_", " "),        genres = ArrayList(node.genres?.map { it.name } ?: emptyList()),        description = node.synopsis,        startDate = parseIsoDate(node.startDate),        endDate = parseIsoDate(node.endDate),        countryOfOrigin = when (node.mediaType?.lowercase()) {            "manhwa" -> "KR"            "manhua" -> "CN"
else -> "JP"        },        userStatus = if (isAnime && node.myListStatus?.isRewatching == true ||                         !isAnime && node.myListStatus?.isRereading == true)            "REPEATING"
else            convertMalStatusToAnilist(node.myListStatus?.status, isAnime),        userProgress = if (isAnime) node.myListStatus?.numEpisodesWatched else node.myListStatus?.numChaptersRead,        userScore = node.myListStatus?.score?.times(10) ?: 0,        anime = if (isAnime) Anime(            totalEpisodes = if (node.numEpisodes == 0) null else node.numEpisodes,            season = node.startSeason?.season,            seasonYear = node.startSeason?.year,            episodeDuration = node.averageEpisodeDuration?.div(60),            nextAiringEpisode = if (node.status?.lowercase() == "currently_airing" && node.startDate != null) {
try {
    val datePart = node.startDate.substringBefore('T')            convertMalStatusToAnilist(ls?.status, isAnime)        this.cameFromContinue = true        ls?.startDate?.let { dateStr ->            parseIsoDate(dateStr)?.let { this.userStartedAt = it }        }        ls?.finishDate?.let { dateStr ->            parseIsoDate(dateStr)?.let { this.userCompletedAt = it }        }    }    constructor(jikan: JikanMediaData, isAnime: Boolean) : this(        id = jikan.malId,        idMAL = jikan.malId,        name = jikan.titleEnglish ?: jikan.title,        nameRomaji = jikan.title ?: "",        userPreferredName = jikan.titleEnglish ?: jikan.title ?: "",        cover = jikan.images?.jpg?.largeImageUrl ?: jikan.images?.jpg?.imageUrl,        banner = jikan.images?.jpg?.largeImageUrl,        status = when (jikan.status?.lowercase()) {            "currently airing", "publishing" -> "RELEASING"            "finished airing", "finished" -> "FINISHED"            "not yet aired", "not yet published" -> "NOT_YET_RELEASED"            "on hiatus" -> "HIATUS"            "discontinued" -> "CANCELLED"
else -> jikan.status?.replace("_", " ")?.uppercase()        },        isAdult = jikan.rating?.contains("rx", true) == true,        meanScore = jikan.score?.times(10)?.toInt(),        popularity = jikan.popularity,        favourites = jikan.favorites,        format = jikan.type?.uppercase(),        source = jikan.source?.replace("_", " "),        genres = ArrayList(jikan.genres?.map { it.name } ?: emptyList()),        description = jikan.synopsis,        startDate = parseIsoDate(if (isAnime) jikan.aired?.from else jikan.published?.from),        endDate = parseIsoDate(if (isAnime) jikan.aired?.to else jikan.published?.to),        countryOfOrigin = when (jikan.type?.lowercase()) {            "manhwa" -> "KR"            "manhua" -> "CN"
else -> "JP"        
val mappedStreaming = jikan.streaming            ?.map {                MediaExternalLink(                    id = null,                    url = it.url,                    site = it.name ?: "Streaming",                    siteId = null,                    type = ExternalLinkType.STREAMING,                    language = null,                    color = null,                    icon = null,                    notes = null                )            } ?: emptyList()        
val allLinks = (mappedExternal + mappedStreaming).distinctBy { it.url ?: it.site }
if (allLinks.isNotEmpty()) {            this.externalLinks = ArrayList(allLinks)        }

val mappedRecommendations = jikan.recommendations            ?.mapNotNull { it.entry }            ?.map {                Media(                    id = it.malId,                    idMAL = it.malId,                    name = it.title,                    nameRomaji = it.title ?: "",                    userPreferredName = it.title ?: "",                    cover = it.images?.jpg?.largeImageUrl ?: it.images?.jpg?.imageUrl,                    banner = it.images?.jpg?.largeImageUrl,                    isAdult = false,                    status = null,                    meanScore = null,                    popularity = null,                    format = null,                )            }            ?.distinctBy { it.id }            ?.let { ArrayList(it) }
if (!mappedRecommendations.isNullOrEmpty()) {            this.anime?.season = jikan.season?.uppercase(Locale.US)            this.anime?.seasonYear = jikan.year            this.anime?.op = ArrayList(jikan.theme?.openings ?: emptyList())            this.anime?.ed = ArrayList(jikan.theme?.endings ?: emptyList())            this.anime?.mainStudio = jikan.studios?.firstOrNull()?.let {                Studio(                    id = it.malId.toString(),                    name = it.name,                    isFavourite = false,                    favourites = null,                    imageUrl = null                )            }

val producerStudios = buildList {                jikan.producers?.forEach {                    add(                        Studio(                            id = it.malId.toString(),                            name = it.name,                            isFavourite = false,                            favourites = null,                            imageUrl = null                        )                    )                }                jikan.licensors?.forEach {                    add(                        Studio(                            id = it.malId.toString(),                            name = it.name,                            isFavourite = false,                            favourites = null,                            imageUrl = null                        )                    )                }            }.distinctBy { it.id }
if (producerStudios.isNotEmpty()) {                this.anime?.producers = ArrayList(producerStudios)            }            this.trailer = jikan.trailer?.effectiveYoutubeId()
if (jikan.status?.equals("Currently Airing", true) == true) {
    var nextAiringTime: Long? = null
if (jikan.broadcast != null) {                    computeNextAiringFromBroadcast(jikan.broadcast)?.let { (episodeTime) ->                        this.anime?.nextAiringEpisodeTime = episodeTime                        nextAiringTime = episodeTime                    }                }
try {
    val fromDateStr = jikan.aired?.from
if (fromDateStr != null) {
    val datePart = fromDateStr.substringBefore('T')                        
val parts = datePart.split("-")                        
val year = parts.getOrNull(0)?.toIntOrNull()                        
val month = parts.getOrNull(1)?.toIntOrNull()                        
val day = parts.getOrNull(2)?.toIntOrNull()
if (year != null && month != null && day != null) {
    val parsedStart = java.time.LocalDate.of(year, month, day)                            
val targetDate = if (nextAiringTime != null) {                                java.time.Instant.ofEpochSecond(nextAiringTime).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
} else {                                java.time.LocalDate.now()                            }
if (targetDate.isAfter(parsedStart)) {
    val weeks = java.time.temporal.ChronoUnit.WEEKS.between(parsedStart, targetDate)                                
var estimatedEp = (weeks + 1).toInt()                                
val totalEpisodes = jikan.episodes ?: 0
if (totalEpisodes > 0 && estimatedEp > totalEpisodes) {                                    estimatedEp = totalEpisodes                                }                                this.anime?.nextAiringEpisode = estimatedEp - 1
} else {                                this.anime?.nextAiringEpisode = 0                            }
} else {                            this.anime?.nextAiringEpisode = 0                        }
} else {        year = year,        month = parts.getOrNull(1)?.toIntOrNull(),        day = parts.getOrNull(2)?.toIntOrNull(),    )}

private fun parseJikanDuration(duration: String?): Int? {
if (duration.isNullOrBlank() || duration == "Unknown") return null
val lower = duration.lowercase()    
var totalMinutes = 0
val hrMatch = Regex("(\\d+)\\s*hr").find(lower)    
val minMatch = Regex("(\\d+)\\s*min").find(lower)
if (hrMatch != null) totalMinutes += (hrMatch.groupValues[1].toIntOrNull() ?: 0) * 60
if (minMatch != null) totalMinutes += minMatch.groupValues[1].toIntOrNull() ?: 0
val secMatch = Regex("(\\d+)\\s*sec").find(lower)
if (totalMinutes == 0 && secMatch != null) totalMinutes = 1
return if (totalMinutes > 0) totalMinutes else null}

private fun convertMalStatusToAnilist(malStatus: String?, isAnime: Boolean): String? {
return when (malStatus?.lowercase()) {        "watching", "reading" -> "CURRENT"        "completed" -> "COMPLETED"        "on_hold" -> "PAUSED"        "dropped" -> "DROPPED"        "plan_to_watch", "plan_to_read" -> "PLANNING"        "rewatching", "rereading" -> "REPEATING"
else -> null     }}

private fun computeNextAiringFromBroadcast(broadcast: JikanBroadcast): Pair<Long, Nothing?>? {
    val dayStr = broadcast.day?.removeSuffix("s")?.lowercase() ?: return null
val timeStr = broadcast.time ?: return null
val tzStr = broadcast.timezone ?: "Asia/Tokyo"    
val dayOfWeek = when (dayStr) {        "monday" -> java.time.DayOfWeek.MONDAY        "tuesday" -> java.time.DayOfWeek.TUESDAY        "wednesday" -> java.time.DayOfWeek.WEDNESDAY        "thursday" -> java.time.DayOfWeek.THURSDAY        "friday" -> java.time.DayOfWeek.FRIDAY        "saturday" -> java.time.DayOfWeek.SATURDAY        "sunday" -> java.time.DayOfWeek.SUNDAY
else -> return null    }
return try {
    val zone = java.time.ZoneId.of(tzStr)        
val timeParts = timeStr.split(":")        
val hour = timeParts[0].toInt()        
val minute = timeParts.getOrNull(1)?.toInt() ?: 0
val now = java.time.ZonedDateTime.now(zone)        
var nextAiring = now.with(java.time.temporal.TemporalAdjusters.nextOrSame(dayOfWeek))            .withHour(hour).withMinute(minute).withSecond(0).withNano(0)
if (!nextAiring.isAfter(now)) {            nextAiring = now.with(java.time.temporal.TemporalAdjusters.next(dayOfWeek))                .withHour(hour).withMinute(minute).withSecond(0).withNano(0)        }        Pair(nextAiring.toEpochSecond(), null)    } catch (_: Exception) {        null    }}

fun Media?.deleteFromList(    scope: CoroutineScope,    onSuccess: suspend () -> Unit,    onError: suspend (e: Exception) -> Unit,    onNotFound: suspend () -> Unit) {
    val id = this?.userListId
val rescueMode: Boolean = PrefManager.getVal(PrefName.RescueMode)    scope.launch {        withContext(Dispatchers.IO) {            this@deleteFromList?.let { media ->
if (rescueMode) {
    val pending = com.sanin.tv.connections.PendingDeletion(                        mediaId = media.id,                        idMAL = media.idMAL,                        isAnime = media.anime != null,                    )                    
val existing: List<com.sanin.tv.connections.PendingDeletion> =                        PrefManager.getVal(PrefName.PendingDeletions, listOf())                    
val updated = existing.filterNot { it.mediaId == media.id } + pending                    PrefManager.setVal(PrefName.PendingDeletions, updated)                    
val removeList = PrefManager.getCustomVal("removeList", setOf<Int>())                    PrefManager.setCustomVal("removeList", removeList.minus(media.id))
try {                        MAL.query.deleteList(media.anime != null, media.idMAL)                    } catch (_: Exception) { /* MAL delete failed
AniList sync still queued */ }                    onSuccess()
} else {
    val _id = id ?: Anilist.query.userMediaDetails(media).userListId                    _id?.let { listId ->
try {                            Anilist.mutation.deleteList(listId)                            MAL.query.deleteList(media.anime != null, media.idMAL)                            
val removeList = PrefManager.getCustomVal("removeList", setOf<Int>())                            PrefManager.setCustomVal(                                "removeList", removeList.minus(media.id)                            )                            onSuccess()                        } catch (e: Exception) {                            onError(e)                        }                    } ?: onNotFound()                }            }        }    }}

fun emptyMedia() = Media(    id = 0,    name = "No media found",    nameRomaji = "No media found",    userPreferredName = "",    isAdult = false,    isFav = false,    isListPrivate = false,    userScore = 0,    userStatus = "",    format = "",)
object MediaSingleton {
    var media: Media? = null
var bitmap: Bitmap? = null}
}}}}}})
