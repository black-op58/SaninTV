package com.sanin.tv.connections.anilist
import android.util.Base64
import com.sanin.tv.R
import com.sanin.tv.checkGenreTime
import com.sanin.tv.checkId
import com.sanin.tv.connections.anilist.Anilist.authorRoles
import com.sanin.tv.connections.anilist.Anilist.executeQuery
import com.sanin.tv.connections.anilist.api.FeedResponse
import com.sanin.tv.connections.anilist.api.FuzzyDate
import com.sanin.tv.connections.anilist.api.MediaEdge
import com.sanin.tv.connections.anilist.api.MediaList
import com.sanin.tv.connections.anilist.api.MediaListStatus
import com.sanin.tv.connections.anilist.api.NotificationResponse
import com.sanin.tv.connections.anilist.api.Page
import com.sanin.tv.connections.anilist.api.Query
import com.sanin.tv.connections.anilist.api.ReplyResponse
import com.sanin.tv.currContext
import com.sanin.tv.isOnline
import com.sanin.tv.logError
import com.sanin.tv.media.Author
import com.sanin.tv.media.Character
import com.sanin.tv.media.Media
import com.sanin.tv.media.Studio
import com.sanin.tv.others.MalScraper
import com.sanin.tv.connections.mal.MAL
import com.sanin.tv.profile.User
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.snackString
import com.sanin.tv.tryWithSuspend
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
if (ids.isEmpty()) return null
val idsString = ids.joinToString(",")        
val response = executeQuery<Query.MediaList>(            """{Page(page:1,perPage:50){media(id_in:[${idsString}],isAdult:false){id mediaListEntry{progress 
private score(format:POINT_100) status} idMal type isAdult popularity status(version:2) chapters episodes nextAiringEpisode{episode} meanScore isFavourite format bannerImage coverImage{large} title{english romaji userPreferred} startDate{year}}}}""",            force = true        )        
val fetchedMediaList = response?.data?.page?.media ?: return null
return fetchedMediaList.map {            Media(it)        }    }

fun mediaDetails(media: Media): Media {        media.cameFromContinue = false        runBlocking(Dispatchers.IO) {
    val anilist = async {
    var response =                    executeQuery<Query.Media>(fullMediaInformation(media.id), force = true)
if (response != null) {
    fun parse() {
    val fetchedMedia = response?.data?.media ?: return
val user = response?.data?.page                        media.isFav = fetchedMedia.isFavourite ?: false                        media.source = fetchedMedia.source?.toString()                        media.countryOfOrigin = fetchedMedia.countryOfOrigin                        media.format = fetchedMedia.format?.toString()                        media.cover = fetchedMedia.coverImage?.large ?: media.cover                        media.banner = fetchedMedia.bannerImage ?: media.banner                        media.favourites = fetchedMedia.favourites                        media.popularity = fetchedMedia.popularity                        media.startDate = fetchedMedia.startDate                        media.endDate = fetchedMedia.endDate                        media.streamingEpisodes = fetchedMedia.streamingEpisodes
if (fetchedMedia.genres != null) {                            media.genres = arrayListOf()                            fetchedMedia.genres?.forEach { i ->                                media.genres.add(i)                            }                        }                        media.trailer = fetchedMedia.trailer?.let { i ->
if (i.site != null && i.site.toString() == "youtube")                                i.id.toString().trim('"')
else null                        }                        fetchedMedia.synonyms?.apply {                            media.synonyms = arrayListOf()                            this.forEach { i ->                                media.synonyms.add(                                    i                                )                            }                        }                        fetchedMedia.tags?.apply {                            media.tags = arrayListOf()                            this.forEach { i ->
if (i.isMediaSpoiler == false)                                    media.tags.add("${i.name} : ${i.rank.toString()}%")                            }                        }                        media.description = fetchedMedia.description.toString()
if (fetchedMedia.characters != null) {                            media.characters = arrayListOf()                            fetchedMedia.characters?.edges?.forEach { i ->                                i.node?.apply {                                    media.characters?.add(                                        Character(                                            id = id,                                            name = i.node?.name?.userPreferred,                                            image = i.node?.image?.medium,                                            banner = media.banner ?: media.cover,                                            isFav = i.node?.isFavourite ?: false,                                            role = when (i.role.toString()) {                                                "MAIN" -> currContext()?.getString(R.string.main_role)                                                    ?: "MAIN"                                                "SUPPORTING" -> currContext()?.getString(R.string.supporting_role)                                                    ?: "SUPPORTING"
else -> i.role.toString()                                            },                                            voiceActor = i.voiceActors?.map {                                                Author(                                                    id = it.id,                                                    name = it.name?.userPreferred,                                                    image = it.image?.large,                                                    role = it.languageV2                                                )                                            }?.distinctBy { it.id }?.let { ArrayList(it) }                                        )                                    )                                }                            }                        }
if (fetchedMedia.staff != null) {                            media.staff = arrayListOf()                            fetchedMedia.staff?.edges?.forEach { i ->                                i.node?.apply {                                    media.staff?.add(                                        Author(                                            id = id,                                            name = i.node?.name?.userPreferred,                                            image = i.node?.image?.large,                                            role = when (i.role.toString()) {                                                "MAIN" -> currContext()?.getString(R.string.main_role)                                                    ?: "MAIN"                                                "SUPPORTING" -> currContext()?.getString(R.string.supporting_role)                                                    ?: "SUPPORTING"
else -> i.role.toString()                                            }                                        )                                    )                                }                            }                        }
if (fetchedMedia.relations != null) {                            media.relations = arrayListOf()                            fetchedMedia.relations?.edges?.forEach { mediaEdge ->                                
val m = Media(mediaEdge)                                media.relations?.add(m)
if (m.relation == "SEQUEL") {                                    media.sequel =
if ((media.sequel?.popularity ?: 0) < (m.popularity                                                ?: 0)                                        ) m else media.sequel
} else if (m.relation == "PREQUEL") {                                    media.prequel =
if ((media.prequel?.popularity ?: 0) < (m.popularity                                                ?: 0)                                        ) m else media.prequel                                }                            }                            media.relations?.sortByDescending { it.popularity }                            media.relations?.sortByDescending { it.startDate?.year }                            media.relations?.sortBy { it.relation }                        }
if (fetchedMedia.recommendations != null) {                            media.recommendations = arrayListOf()                            fetchedMedia.recommendations?.nodes?.forEach { i ->                                i.mediaRecommendation?.apply {                                    media.recommendations?.add(                                        Media(this)                                    )                                }                            }                        }
if (fetchedMedia.reviews?.nodes != null) {                            media.review = fetchedMedia.reviews!!.nodes as ArrayList<Query.Review>                        }
if (user?.mediaList?.isNotEmpty() == true) {                            media.users = user.mediaList?.mapNotNull {                                it.user?.let { user ->
if (user.id != Anilist.userid) {                                        User(                                            user.id,                                            user.name ?: "Unknown",                                            user.avatar?.large,                                            "",                                            it.status?.toString(),                                            it.score,                                            it.progress,                                            fetchedMedia.episodes ?: fetchedMedia.chapters,                                        )
} else null                                }                            }?.toCollection(arrayListOf()) ?: arrayListOf()                        }
if (fetchedMedia.mediaListEntry != null) {                            fetchedMedia.mediaListEntry?.apply {                                media.userProgress = progress                                media.userProgressVolumes = progressVolumes                                media.isListPrivate =private ?: false
                                media.notes = notes                                media.userListId = id                                media.userScore = score?.toInt() ?: 0                                media.userStatus = status?.toString()                                media.inCustomListsOf = customLists?.toMutableMap()                                media.userRepeat = repeat ?: 0                                media.userUpdatedAt = updatedAt?.toString()?.toLong()?.times(1000)                                media.userCompletedAt = completedAt ?: FuzzyDate()                                media.userStartedAt = startedAt ?: FuzzyDate()                            }
                                } else {                            media.isListPrivate = false                            media.userStatus = null                            media.userListId = null                            media.userProgress = null                            media.userProgressVolumes = null                            media.userScore = 0                            media.userRepeat = 0                            media.userUpdatedAt = null                            media.userCompletedAt = FuzzyDate()                            media.userStartedAt = FuzzyDate()                        }
                                if (media.anime != null) {                            media.anime.episodeDuration = fetchedMedia.duration                            media.anime.season = fetchedMedia.season?.toString()                            media.anime.seasonYear = fetchedMedia.seasonYear                            fetchedMedia.studios?.nodes?.apply {
    if (isNotEmpty()) {
    val animStudio = firstOrNull { it.isAnimationStudio == true }

val studioNode = animStudio ?: get(0)                                    media.anime.mainStudio = Studio(                                        studioNode.id.toString(),                                        studioNode.name ?: "N/A",                                        studioNode.isFavourite ?: false,                                        studioNode.favourites ?: 0,                                        null                                    )                                }                            }                            // Map non-main studios (isMain: false) as producers                            fetchedMedia.producers?.nodes?.apply {
if (isNotEmpty()) {                                    media.anime.producers = map {                                        Studio(                                            it.id.toString(),                                            it.name ?: "N/A",                                            it.isFavourite ?: false,                                            it.favourites ?: 0,                                            null                                        )                                    } as ArrayList<Studio>                                }                            }                            fetchedMedia.staff?.edges?.find { authorRoles.contains(it.role?.trim()) }?.node?.let {                                media.anime.author = Author(                                    it.id,                            fullMediaInformation(media.id),                            force = true,                            useToken = false                        )
if (response?.data?.media != null) parse()
else snackString(currContext()?.getString(R.string.what_did_you_open))                    }
} else {
if (currContext()?.let { isOnline(it) } == true) {                        snackString(currContext()?.getString(R.string.error_getting_data))
} else {                    }                }            }

val mal = async {
if (media.idMAL != null) {                    MalScraper.loadMedia(media)                }            }            awaitAll(anilist, mal)        }
return media    }

fun userMediaDetails(media: Media): Media {
    val query =            """{Media(id:${media.id}){id mediaListEntry{id status progress progressVolumes 
private repeat customLists updatedAt startedAt{year month day}completedAt{year month day}}isFavourite idMal}}"""        runBlocking(Dispatchers.IO) {
    val anilist = async {
    var response = executeQuery<Query.Media>(query, force = true, show = true)
if (response != null) {
    fun parse() {
    val fetchedMedia = response?.data?.media ?: return                        media.isFav = fetchedMedia.isFavourite ?: false
if (fetchedMedia.mediaListEntry != null) {                            fetchedMedia.mediaListEntry?.apply {                                media.userProgress = progress                                media.userProgressVolumes = progressVolumes                                media.isListPrivate =private ?: false
                                media.userListId = id                                media.userStatus = status?.toString()                                media.inCustomListsOf = customLists?.toMutableMap()                                media.userRepeat = repeat ?: 0                                media.userUpdatedAt = updatedAt?.toString()?.toLong()?.times(1000)                                media.userCompletedAt = completedAt ?: FuzzyDate()                                media.userStartedAt = startedAt ?: FuzzyDate()                            }
                                } else {                            media.isListPrivate = false                            media.userStatus = null                            media.userListId = null                            media.userProgress = null                            media.userProgressVolumes = null                            media.userRepeat = 0                            media.userUpdatedAt = null                            media.userCompletedAt = FuzzyDate()                            media.userStartedAt = FuzzyDate()                        }                    }
                                if (response.data?.media != null) parse()
                                else {                        response = executeQuery(query, force = true, useToken = false)
                                if (response?.data?.media != null) parse()                    }                }            }            awaitAll(anilist)        }
                                return media    }

private suspend 
fun favMedia(anime: Boolean, id: Int? = Anilist.userid): ArrayList<Media> {
    var hasNextPage = true
var page = 0        suspend 
fun getNextPage(page: Int): List<Media> {
    val response = executeQuery<Query.User>("""{${favMediaQuery(anime, page, id)}}""")            
val favourites = response?.data?.user?.favourites    }

private fun recommendationQuery(): String {
    return """ Page(page: 1, perPage:30) { $standardPageInformation recommendations(sort: RATING_DESC, onList: true) { rating userRating mediaRecommendation { id idMal isAdult mediaListEntry { progress progressVolumes 
private score(format:POINT_100) status } chapters volumes isFavourite format episodes nextAiringEpisode {episode} popularity meanScore isFavourite format title {english romaji userPreferred } type status(version: 2) bannerImage coverImage { large } } } } """    }

private fun missingSequelsCompletedSourceQuery(): String {
    return """ MediaListCollection( userId: ${Anilist.userid}, type: ANIME, status: COMPLETED, sort: UPDATED_TIME_DESC ) { lists { entries { media { id relations { edges { relationType(version: 2) node { id } } } } } } } """.trimIndent()    }

private fun missingSequelsAllListSourceQuery(): String {
    return """            MediaListCollection( userId: ${Anilist.userid}, type: ANIME ) { lists { entries { media { id } } } } """.trimIndent()    }

private val batchSize = 50    
private fun missingSequelsLookupQuery(ids: List<Int>): String {
    val idsString = ids.joinToString(",")        return """ { Page(page: 1, perPage: $batchSize) { media( id_in: [$idsString], type: ANIME, status_in: [RELEASING, FINISHED], onList: false ) { id mediaListEntry { progress progressVolumes 
private score(format: POINT_100) status } idMal type isAdult popularity status(version: 2) chapters volumes episodes nextAiringEpisode { episode } meanScore isFavourite format bannerImage coverImage { large } title { english romaji userPreferred } startDate { year } } } } """.trimIndent()    }

private fun extractMissingSequelIds(completedEntries: List<MediaList>?): Set<Int> {
    val sequelIds = mutableSetOf<Int>()        completedEntries?.forEach { entry ->            entry.media?.relations?.edges?.forEach { edge ->
if (edge.relationType?.name == "SEQUEL") {                    edge.node?.id?.let { sequelIds.add(it) }                }            }        }
return sequelIds    }

private fun extractAnimeIds(entries: List<MediaList>?): Set<Int> {
return entries            ?.mapNotNull { it.media?.id }            ?.toSet()            ?: emptySet()    }

private suspend 
fun fetchMissingSequelMedia(ids: Set<Int>): ArrayList<Media> {
if (ids.isEmpty()) return arrayListOf()        
val batches = ids.toList().chunked(batchSize)        
val batchResults: List<List<Media>> = coroutineScope {            batches.map { batch ->                async {                    executeQuery<Query.Page>(missingSequelsLookupQuery(batch))                        ?.data                        ?.page                        ?.media                        ?.mapNotNull { media ->
if (media.mediaListEntry == null) Media(media) else null                        }                        ?: emptyList()                }            }                .awaitAll()        }
return ArrayList(batchResults.flatten())    }

private fun loadMissingSequelCache(ids: Set<Int>): ArrayList<Media>? {
    val cached = PrefManager.getNullableCustomVal(            "missing_sequels_cache",            null,            MissingSequelsCache::class.java        ) ?: return null
val cacheExpired = System.currentTimeMillis() - cached.cachedAt > 6 * 60 * 60 * 1000L
if (cacheExpired || cached.sourceIds != ids) return null
return ArrayList(cached.media)    }

private fun saveMissingSequelCache(ids: Set<Int>, media: ArrayList<Media>) {        PrefManager.setCustomVal(            "missing_sequels_cache",            MissingSequelsCache(ids, media, System.currentTimeMillis())        )    }

private fun loadUserStatusCache(): ArrayList<User>? {
    val cached = PrefManager.getNullableCustomVal(            "user_status_cache",            null,            UserStatusCache::class.java        ) ?: return null
val cacheExpired = System.currentTimeMillis() - cached.cachedAt > 15 * 60 * 1000L
if (cacheExpired) return null
return ArrayList(cached.users)    }

private fun saveUserStatusCache(users: ArrayList<User>) {        PrefManager.setCustomVal(            "user_status_cache",            UserStatusCache(users, System.currentTimeMillis())        )    }

private suspend 
fun getMissingSequelMedia(ids: Set<Int>): ArrayList<Media> {        loadMissingSequelCache(ids)?.let { return it }

val fresh = fetchMissingSequelMedia(ids)        saveMissingSequelCache(ids, fresh)
return fresh    }

private fun continueMediaQuery(type: String, status: String): String {
    return """ MediaListCollection(userId: ${Anilist.userid}, type: $type, status: $status , sort: UPDATED_TIME ) { lists { entries { progress 
private score(format:POINT_100) status updatedAt media { id idMal type isAdult status chapters episodes nextAiringEpisode {episode} meanScore isFavourite format bannerImage coverImage{large} title { english romaji userPreferred } } } } } """    }    suspend 
fun initHomePage(): Map<String, ArrayList<Media>> {
    val removeList = PrefManager.getCustomVal("removeList", setOf<Int>())        
val hidePrivate = PrefManager.getVal<Boolean>(PrefName.HidePrivate)        
val removedMedia = ArrayList<Media>()        
val toShow = PrefManager.getVal<List<Boolean>>(PrefName.HomeLayout).toMutableList()        
val queries = mutableListOf<String>()        if (toShow.getOrNull(0) == true) {            queries.add("""currentAnime: ${continueMediaQuery("ANIME", "CURRENT")}""")        }

val response = if (queries.isEmpty()) {            null
} else {
    val query = "{${queries.joinToString(",")}}"            executeQuery<Query.HomePageMedia>(query, show = true)        }

val returnMap = mutableMapOf<String, ArrayList<Media>>()        
fun processMedia(            type: String,            currentMedia: List<MediaList>?,            repeatingMedia: List<MediaList>?        ) {
    val subMap = mutableMapOf<Int, Media>()            
val returnArray = arrayListOf<Media>()            (currentMedia ?: emptyList()).forEach { entry ->                
val media = Media(entry)
if (media.id !in removeList && (!hidePrivate || !media.isListPrivate)) {                    media.cameFromContinue = true                    subMap[media.id] = media
} else {                    removedMedia.add(media)                }            }            (repeatingMedia ?: emptyList()).forEach { entry ->                
val media = Media(entry)
if (media.id !in removeList && (!hidePrivate || !media.isListPrivate)) {                    media.cameFromContinue = true                    subMap[media.id] = media
} else {                    removedMedia.add(media)                }            }

@Suppress("UNCHECKED_CAST")            
val list = PrefManager.getNullableCustomVal(                "continue${type}List",    suspend 
fun getGenresAndTags(): Boolean {
    var genres: ArrayList<String>? = PrefManager.getVal<Set<String>>(PrefName.GenresList)            .toMutableList() as ArrayList<String>?        
val adultTags = PrefManager.getVal<Set<String>>(PrefName.TagsListIsAdult).toMutableList()        
val nonAdultTags =            PrefManager.getVal<Set<String>>(PrefName.TagsListNonAdult).toMutableList()        
var tags = if (adultTags.isEmpty() || nonAdultTags.isEmpty()) null else            mapOf(                true to adultTags.sortedBy { it },                false to nonAdultTags.sortedBy { it }            )        if (genres.isNullOrEmpty()) {            executeQuery<Query.GenreCollection>(                """{GenreCollection}""",                force = true,                useToken = false            )?.data?.genreCollection?.apply {                genres = arrayListOf()                forEach {                    genres?.add(it)                }                PrefManager.setVal(PrefName.GenresList, genres?.toSet())            }        }        if (tags == null) {            executeQuery<Query.MediaTagCollection>(                """{ MediaTagCollection { name isAdult } }""",                force = true            )?.data?.mediaTagCollection?.apply {
    val adult = mutableListOf<String>()                
val good = mutableListOf<String>()                forEach { node ->
if (node.isAdult == true) adult.add(node.name)
else good.add(node.name)                }                tags = mapOf(                    true to adult,                    false to good                )                PrefManager.setVal(PrefName.TagsListIsAdult, adult.toSet())                PrefManager.setVal(PrefName.TagsListNonAdult, good.toSet())            }        }
return if (!genres.isNullOrEmpty() && tags != null) {            Anilist.genres = genres?.sortedBy { it }?.toMutableList() as ArrayList<String>            Anilist.tags = tags            true
} else false    }    suspend 
fun getGenres(genres: ArrayList<String>, listener: ((Pair<String, String>) -> Unit)) {        genres.forEach {
    val thumbnail = getGenreThumbnail(it)
if (thumbnail != null) {                listener.invoke(it to thumbnail.thumbnail)
} else {                listener.invoke(it to "")            }        }    }

private fun <K, V : Serializable> saveSerializableMap(prefKey: String, map: Map<K, V>) {
    val byteStream = ByteArrayOutputStream()        ObjectOutputStream(byteStream).use { outputStream ->            outputStream.writeObject(map)        }

val serializedMap = Base64.encodeToString(byteStream.toByteArray(), Base64.DEFAULT)        PrefManager.setCustomVal(prefKey, serializedMap)    }

@Suppress("UNCHECKED_CAST")    
private fun <K, V : Serializable> loadSerializableMap(prefKey: String): Map<K, V>? {
try {
    val serializedMap = PrefManager.getCustomVal(prefKey, "")
if (serializedMap.isEmpty()) return null
val bytes = Base64.decode(serializedMap, Base64.DEFAULT)            
val byteArrayStream = ByteArrayInputStream(bytes)
return ObjectInputStream(byteArrayStream).use { inputStream ->                inputStream.readObject() as? Map<K, V>            }        } catch (e: Exception) {
return null        }    }

private suspend 
fun getGenreThumbnail(genre: String): Genre? {
    val genres: MutableMap<String, Genre> =            loadSerializableMap<String, Genre>("genre_thumb")?.toMutableMap()                ?: mutableMapOf()
if (genres.checkGenreTime(genre)) {
try {
    val genreQuery =                    """{ Page(perPage: 10){media(genre:"$genre", sort: TRENDING_DESC, type: ANIME, countryOfOrigin:"JP") {id bannerImage title{english romaji userPreferred} } } }"""                executeQuery<Query.Page>(genreQuery, force = true)?.data?.page?.media?.forEach {
    if (genres.checkId(it.id) && it.bannerImage != null) {                        genres[genre] = Genre(                            genre,                            it.id,                            it.bannerImage!!,                            System.currentTimeMillis()                        )                        saveSerializableMap("genre_thumb", genres)                        return genres[genre]                    }                }            } catch (e: Exception) {                logError(e)            }        } else {
    return genres[genre]        }        return null    }               $standardPageInformation               characters(search: "$search") {                  ${characterInformation(false)}               }             }           }        """.prepare()        
val response = executeQuery<Query.Page>(query, force = true)?.data?.page
if (response?.characters != null) {
    val responseArray = arrayListOf<Character>()            response.characters?.forEach { i ->                responseArray.add(                    Character(                        i.id,                        i.name?.full,                        i.image?.medium ?: i.image?.large,                        null,                        null.toString(),                        i.isFavourite ?: false,                        i.description,                        i.age,                        i.gender,                        i.dateOfBirth,                    )                )            }

val pageInfo = response.pageInfo ?: return null
return CharacterSearchResults(                search = search,                results = responseArray,                page = pageInfo.currentPage ?: 0,                hasNextPage = pageInfo.hasNextPage == true            )        }
return null    }    suspend 
fun searchStudios(page: Int, search: String?): StudioSearchResults? {
if (search.isNullOrBlank()) return null
val query = """           {             Page(page: $page, perPage: $ITEMS_PER_PAGE) {               $standardPageInformation               studios(search: "$search") {                  ${studioInformation(1, 1)}               }             }           }        """.prepare()        
val response = executeQuery<Query.Page>(query, force = true)?.data?.page
if (response?.studios != null) {
    val responseArray = arrayListOf<Studio>()            response.studios?.forEach { i ->                responseArray.add(                    Studio(                        i.id.toString(),                        i.name ?: return null,                        i.isFavourite ?: false,                        i.favourites,                        i.media?.edges?.firstOrNull()?.node?.let { it.coverImage?.large }                    )                )            }

val pageInfo = response.pageInfo ?: return null
return StudioSearchResults(                search = search,                results = responseArray,                page = pageInfo.currentPage ?: 0,                hasNextPage = pageInfo.hasNextPage == true            )        }
return null    }    suspend 
fun searchStaff(page: Int, search: String?): StaffSearchResults? {
if (search.isNullOrBlank()) return null
val query = """           {             Page(page: $page, perPage: $ITEMS_PER_PAGE) {               $standardPageInformation               staff(search: "$search") {                  ${staffInformation(1, 1)}               }             }           }        """.prepare()        
val response = executeQuery<Query.Page>(query, force = true)?.data?.page
if (response?.staff != null) {
    val responseArray = arrayListOf<Author>()            response.staff?.forEach { i ->                responseArray.add(                    Author(                        i.id,                        i.name?.userPreferred ?: return null,                        i.image?.large,                        null,                        null,                        null                    )                )            }

val pageInfo = response.pageInfo ?: return null
return StaffSearchResults(                search = search,                results = responseArray,                page = pageInfo.currentPage ?: 0,                hasNextPage = pageInfo.hasNextPage == true            )        }
return null    }    suspend 
fun searchUsers(page: Int, search: String?): UserSearchResults? {
    val query = """           {             Page(page: $page, perPage: $ITEMS_PER_PAGE) {               $standardPageInformation               users(search: "$search") {                  ${userInformation()}               }             }           }        """.prepare()        
val response = executeQuery<Query.Page>(query, force = true)?.data?.page
if (response?.users != null) {
    val users = response.users?.map { user ->                User(                    user.id,                    user.name ?: return null,                    user.avatar?.medium,                    user.bannerImage                )            } ?: return null
return UserSearchResults(                search = search,                results = users.toMutableList(),                page = response.pageInfo?.currentPage ?: 0,                hasNextPage = response.pageInfo?.hasNextPage == true            )        }
return null    }

private fun mediaList(media1: Page?): ArrayList<Media> {
    val combinedList = arrayListOf<Media>()        
val list = mutableListOf<Media>()        
var res: Page? = null        suspend 
fun next() {            res = execute(i)            list.addAll(res?.airingSchedules?.mapNotNull { j ->                j.media?.let {
if (it.countryOfOrigin == "JP" && (if (!Anilist.adult) it.isAdult == false else true)) {                        Media(it).apply { relation = "${j.episode},${j.airingAt}" }
} else null                }            } ?: listOf())        }        next()
while (res?.pageInfo?.hasNextPage == true) {            next()            i++        }
return list.reversed().toMutableList()    }    suspend 
fun getCharacterDetails(character: Character): Character {
    val query = """ {  Character(id: ${character.id}) {    ${characterInformation(true)}  }}""".prepare()        executeQuery<Query.Character>(query, force = true)?.data?.character?.let { i ->            return Character(                i.id,                i.name?.full,                i.image?.large ?: i.image?.medium,                null,                null.toString(),                i.isFavourite ?: false,                i.description,                i.age,                i.gender,                i.dateOfBirth,                i.media?.edges?.map {
    val m = Media(it)                    m.relation = it.characterRole.toString()                    m                }?.let { ArrayList(it) },                i.media?.edges?.flatMap { edge ->                    edge.voiceActors?.map { va ->                        Author(                            va.id,                            va.name?.userPreferred,                            va.image?.large ?: va.image?.medium,                            va.languageV2                        )                    } ?: emptyList()                }?.distinctBy { it.id }?.let { ArrayList(it) }            )        }
return character    }    suspend 
fun getStudioDetails(studio: Studio): Studio {
    fun query(page: Int = 0) = """ {  Studio(id: ${studio.id}) {    ${studioInformation(page, ITEMS_PER_PAGE)}  }}""".prepare()        
var hasNextPage = true
val yearMedia = mutableMapOf<String, ArrayList<Media>>()        
var page = 0
val seenMediaIds = hashSetOf<Int>()
while (hasNextPage) {            page++            hasNextPage =                executeQuery<Query.Studio>(query(page), force = true)?.data?.studio?.media?.let {                    it.edges?.forEach { i ->                        i.node?.apply {
if (id !in seenMediaIds) {                                seenMediaIds.add(id)                                
val status = status.toString()                                
val year = startDate?.year?.toString() ?: "TBA"                                
val title = if (status != "CANCELLED") year else status
if (!yearMedia.containsKey(title))                                    yearMedia[title] = arrayListOf()                                yearMedia[title]?.add(Media(this))                            }                        }                    }                    it.pageInfo?.hasNextPage == true                } ?: false        }
if (yearMedia.contains("CANCELLED")) {
    val a = yearMedia["CANCELLED"]!!            yearMedia.remove("CANCELLED")            yearMedia["CANCELLED"] = a        }        studio.yearMedia = yearMedia
return studio    }    suspend 
fun getAuthorDetails(author: Author): Author {
    fun query(page: Int = 0) = """ {  Staff(id: ${author.id}) {    ${staffInformation(page, ITEMS_PER_PAGE)}    characters(page: $page,sort:FAVOURITES_DESC) {      $standardPageInformation      nodes{        ${characterInformation(false)}      }    }  }}""".prepare()        
var hasNextPage = true
val yearMedia = mutableMapOf<String, ArrayList<Media>>()        
var page = 0
val characters = arrayListOf<Character>()
while (hasNextPage) {            page++            
val query = executeQuery<Query.Author>(                query(page), force = true            )?.data?.author            author.age = query?.age            author.yearsActive =
if (query?.yearsActive?.isEmpty() == true) null else query?.yearsActive            author.homeTown = if (query?.homeTown?.isBlank() == true) null else query?.homeTown            author.dateOfDeath = if (query?.dateOfDeath?.toStringOrEmpty()                    ?.isBlank() == true            ) null else query?.dateOfDeath?.toStringOrEmpty()            author.dateOfBirth = if (query?.dateOfBirth?.toStringOrEmpty()                    ?.isBlank() == true            ) null else query?.dateOfBirth?.toStringOrEmpty()            hasNextPage = query?.staffMedia?.let {                it.edges?.forEach { i ->                    i.node?.apply {
    val status = status.toString()                        
val year = startDate?.year?.toString() ?: "TBA"                        
val title = if (status != "CANCELLED") year else status
if (!yearMedia.containsKey(title))                            yearMedia[title] = arrayListOf()                        
val media = Media(this)                        media.relation = i.staffRole                        yearMedia[title]?.add(media)                    }                }                it.pageInfo?.hasNextPage == true            } ?: false            query?.characters?.let {                it.nodes?.forEach { i ->                    characters.add(                        Character(                            i.id,                            i.name?.userPreferred,                            i.image?.large,                            i.image?.medium,                            "",                            false                        )                    )                }            }
}}}
}}})
