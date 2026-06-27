package com.sanin.tv.connections.anilist

isFav = false,                        isAdult = false,                        status = node.status,                        anime = Anime(                            totalEpisodes = node.numEpisodes,                            season = null,                            seasonYear = null,                        ),                    )                }                missingSequels.postValue(ArrayList(sequelMedia))
} else {                missingSequels.postValue(arrayListOf())            }        } ?: missingSequels.postValue(arrayListOf())    }    suspend 
fun loadMain(context: FragmentActivity) {        Anilist.getSavedToken()        MAL.getSavedToken()
if (!BuildConfig.FLAVOR.contains("fdroid")) {
if (PrefManager.getVal(PrefName.CheckUpdate))                context.lifecycleScope.launch(Dispatchers.IO) {                    AppUpdater.check(context, false)                }        }

val rescueMode: Boolean = PrefManager.getVal(PrefName.RescueMode)
if (rescueMode) {
if (MAL.token != null) tryWithSuspend { MAL.query.getUserData() }
if (Anilist.genres == null) {                Anilist.genres = arrayListOf(                    "Action", "Adventure", "Comedy", "Drama", "Ecchi", "Fantasy",                    "Horror", "Mecha", "Music", "Mystery", "Psychological", "Romance",                    "Sci-Fi", "Slice of Life", "Sports", "Supernatural", "Thriller",                    "Shounen", "Shoujo", "Seinen", "Josei", "Isekai", "Harem",                    "Gourmet", "Historical", "Martial Arts", "Military", "Parody",                    "School", "Space", "Super Power", "Vampire",                    "Boys Love", "Girls Love", "Kids", "Samurai"                )    
private val popularMovies: MutableLiveData<MutableList<Media>> =        MutableLiveData<MutableList<Media>>(null)    
fun getMovies(): LiveData<MutableList<Media>> = popularMovies    
private val topRatedAnime: MutableLiveData<MutableList<Media>> =        MutableLiveData<MutableList<Media>>(null)    
fun getTopRated(): LiveData<MutableList<Media>> = topRatedAnime    
private val mostFavAnime: MutableLiveData<MutableList<Media>> =        MutableLiveData<MutableList<Media>>(null)    
fun getMostFav(): LiveData<MutableList<Media>> = mostFavAnime    suspend 
fun loadAll() {
    val rescueMode: Boolean = PrefManager.getVal(PrefName.RescueMode)
if (rescueMode) {            loadAllFromMAL()
return        }

val list = Anilist.query.loadAnimeList()        updated.postValue(list["recentUpdates"])        popularMovies.postValue(list["trendingMovies"])        topRatedAnime.postValue(list["topRated"])        mostFavAnime.postValue(list["mostFav"])    }

private suspend 
fun loadAllFromMAL() {        tryWithSuspend {            MAL.query.getAnimeRanking("airing", 15)?.data?.let { entries ->                updated.postValue(entries.map { Media(it.node, true) }.toMutableList())            }        }        tryWithSuspend {            MAL.query.getAnimeRanking("bypopularity", 15)?.data?.let { entries ->                popularMovies.postValue(entries.map { Media(it.node, true) }.toMutableList())            }        }        tryWithSuspend {                r.source,                r.format,                r.countryOfOrigin,                r.isAdult,                r.onList,                r.excludedGenres,                r.excludedTags,                r.startYear,                r.seasonYear,                r.season,            )        )    }

private suspend 
fun loadCharacterSearch(r: CharacterSearchResults) {
if (PrefManager.getVal<Boolean>(PrefName.RescueMode)) {
    val search = r.search
if (!search.isNullOrBlank()) {

    val res = MAL.jikan.searchCharacters(search, r.page)                characterResult.postValue(CharacterSearchResults(                    search = search, page = r.page,                    results = res?.data?.map { Character(it.malId, it.name, it.images?.jpg?.imageUrl, null, "", false) }?.toMutableList() ?: mutableListOf(),                    hasNextPage = res?.pagination?.hasNextPage ?: false                ))
} else {                characterResult.postValue(CharacterSearchResults(                    search = search, page = r.page,                    results = mutableListOf(),                    hasNextPage = false                ))            }
return        }        characterResult.postValue(Anilist.query.searchCharacters(r.page, r.search))    }

private suspend 
fun loadStudiosSearch(r: StudioSearchResults) {
if (PrefManager.getVal<Boolean>(PrefName.RescueMode)) {
    val search = r.search
if (!search.isNullOrBlank()) {

    val res = MAL.jikan.searchStudios(search, r.page)                studioResult.postValue(StudioSearchResults(                    search = search, page = r.page,                    results = res?.data?.map { Studio(it.malId.toString(), it.name ?: "", false, it.favorites ?: 0, it.images?.jpg?.imageUrl) }?.toMutableList() ?: mutableListOf(),                    hasNextPage = res?.pagination?.hasNextPage ?: false                ))
} else {                studioResult.postValue(StudioSearchResults(                    search = search, page = r.page,                    results = mutableListOf(),                    hasNextPage = false                ))            }
return        }        studioResult.postValue(Anilist.query.searchStudios(r.page, r.search))    }

private suspend 
fun loadStaffSearch(r: StaffSearchResults) {
if (PrefManager.getVal<Boolean>(PrefName.RescueMode)) {
    val search = r.search
if (!search.isNullOrBlank()) {

    val res = MAL.jikan.searchStaff(search, r.page)                staffResult.postValue(StaffSearchResults(                    search = search, page = r.page,                    results = res?.data?.map { Author(it.malId, it.name ?: "", it.images?.jpg?.imageUrl, "STAFF") }?.toMutableList() ?: mutableListOf(),                    hasNextPage = res?.pagination?.hasNextPage ?: false                ))
} else {                staffResult.postValue(StaffSearchResults(                    search = search, page = r.page,                    results = mutableListOf(),                    hasNextPage = false                ))            }
return        }        staffResult.postValue(Anilist.query.searchStaff(r.page, r.search))    }

private suspend 
fun loadUserSearch(r: UserSearchResults) {
if (PrefManager.getVal<Boolean>(PrefName.RescueMode)) {
    val search = r.search
if (!search.isNullOrBlank()) {
    val res = MAL.jikan.searchUsers(search, r.page)                
val resultsList = res?.data?.map {                    User(                        id = it.username.hashCode().and(0x7FFFFFFF),                        name = it.username ?: "",                        pfp = it.images?.jpg?.imageUrl,                        banner = "https://myanimelist.net/profile/${it.username ?: ""}"                    )                }?.toMutableList() ?: mutableListOf()
if (resultsList.isEmpty()) {
    val exactProfile = MAL.jikan.getUserProfile(search)
if (exactProfile != null) {                        resultsList.add(                            User(                                id = exactProfile.username.hashCode().and(0x7FFFFFFF),                                name = exactProfile.username ?: "",                                pfp = exactProfile.images?.jpg?.imageUrl,                                banner = "https://myanimelist.net/profile/${exactProfile.username ?: ""}"                            )                        )                    }                }                userResult.postValue(UserSearchResults(                    search = search, page = r.page,                    results = resultsList,                    hasNextPage = res?.pagination?.hasNextPage ?: false                ))
} else {                userResult.postValue(UserSearchResults(                    search = search, page = r.page,                    results = mutableListOf(),                    hasNextPage = false                ))            }                r.type,                r.page + 1,                r.perPage,                r.search,                r.sort,                r.genres,                r.tags,                r.status,                r.source,                r.format,                r.countryOfOrigin,                r.isAdult,                r.onList,                r.excludedGenres,                r.excludedTags,                r.startYear,                r.seasonYear,                r.season            )        )    }

private suspend 
fun loadNextCharacterPage(r: CharacterSearchResults) {
if (PrefManager.getVal<Boolean>(PrefName.RescueMode)) {
    val search = r.search
if (!search.isNullOrBlank()) {

    val res = MAL.jikan.searchCharacters(search, r.page + 1)                characterResult.postValue(CharacterSearchResults(                    search = search, page = r.page + 1,                    results = res?.data?.map { Character(it.malId, it.name, it.images?.jpg?.imageUrl, null, "", false) }?.toMutableList() ?: mutableListOf(),                    hasNextPage = res?.pagination?.hasNextPage ?: false                ))
} else {                characterResult.postValue(CharacterSearchResults(                    search = search, page = r.page + 1,                    results = mutableListOf(),                    hasNextPage = false                ))            }
return        }        characterResult.postValue(Anilist.query.searchCharacters(r.page + 1, r.search))    }

private suspend 
fun loadNextStudiosPage(r: StudioSearchResults) {
if (PrefManager.getVal<Boolean>(PrefName.RescueMode)) {
    val search = r.search
if (!search.isNullOrBlank()) {

    val res = MAL.jikan.searchStudios(search, r.page + 1)                studioResult.postValue(StudioSearchResults(                    search = search, page = r.page + 1,                    results = res?.data?.map { Studio(it.malId.toString(), it.name ?: "", false, it.favorites ?: 0, it.images?.jpg?.imageUrl) }?.toMutableList() ?: mutableListOf(),                    hasNextPage = res?.pagination?.hasNextPage ?: false                ))
} else {                studioResult.postValue(StudioSearchResults(                    search = search, page = r.page + 1,                    results = mutableListOf(),                    hasNextPage = false                ))            }
return        }        studioResult.postValue(Anilist.query.searchStudios(r.page + 1, r.search))    }

private suspend 
fun loadNextStaffPage(r: StaffSearchResults) {
if (PrefManager.getVal<Boolean>(PrefName.RescueMode)) {
    val search = r.search
if (!search.isNullOrBlank()) {

    val res = MAL.jikan.searchStaff(search, r.page + 1)                staffResult.postValue(StaffSearchResults(                    search = search, page = r.page + 1,                    results = res?.data?.map { Author(it.malId, it.name ?: "", it.images?.jpg?.imageUrl, "STAFF") }?.toMutableList() ?: mutableListOf(),                    hasNextPage = res?.pagination?.hasNextPage ?: false                ))
} else {                staffResult.postValue(StaffSearchResults(                    search = search, page = r.page + 1,                    results = mutableListOf(),                    hasNextPage = false                ))            }
return        }        staffResult.postValue(Anilist.query.searchStaff(r.page + 1, r.search))    }

private suspend 
fun loadNextUserPage(r: UserSearchResults) {
if (PrefManager.getVal<Boolean>(PrefName.RescueMode)) {
    val search = r.search
if (!search.isNullOrBlank()) {

    val res = MAL.jikan.searchUsers(search, r.page + 1)                userResult.postValue(UserSearchResults(                    search = search, page = r.page + 1,                    results = res?.data?.map { User(it.username.hashCode().and(0x7FFFFFFF), it.username ?: "", it.images?.jpg?.imageUrl, "https://myanimelist.net/profile/${it.username ?: ""}") }?.toMutableList() ?: mutableListOf(),                    hasNextPage = res?.pagination?.hasNextPage ?: false                ))
} else {                userResult.postValue(UserSearchResults(                    search = search, page = r.page + 1,                    results = mutableListOf(),                    hasNextPage = false                ))            }
return        }        userResult.postValue(Anilist.query.searchUsers(r.page + 1, r.search))    }}

class GenresViewModel : ViewModel() {
    var genres: MutableMap<String, String>? = null
var done = false
var doneListener: (() -> Unit)? = null    suspend 
fun loadGenres(genre: ArrayList<String>, listener: (Pair<String, String>) -> Unit) {
if (genres == null) {            genres = mutableMapOf(
if (PrefManager.getVal<Boolean>(PrefName.RescueMode)) {                loadGenresFromJikan(genre, listener
} else {                Anilist.query.getGenres(genre) {                    genres!![it.first] = it.second                    listener.invoke(it
if (genres!!.size == genre.size) {                        done = true                        doneListener?.invoke()                    }                }            }        }    }

private suspend 
fun loadGenresFromJikan(genre: ArrayList<String>, listener: (Pair<String, String>) -> Unit) {

    val jikanGenreMap = mapOf(            "Action" to 1, "Adventure" to 2, "Comedy" to 4,            "Mystery" to 7, "Drama" to 8, "Ecchi" to 9, "Fantasy" to 10,            "Horror" to 14, "Mecha" to 18, "Music" to 19,            "Romance" to 22, "Sci-Fi" to 24, "Slice of Life" to 36,            "Sports" to 30, "Supernatural" to 37, "Thriller" to 41,            "Psychological" to 40, "Shounen" to 27, "Shoujo" to 25,            "Seinen" to 42, "Josei" to 43, "Isekai" to 62,            "Harem" to 35, "Gourmet" to 47, "Boys Love" to 28,            "Girls Love" to 26, "Martial Arts" to 17, "Parody" to 20,            "Super Power" to 31, "Military" to 38, "Historical" to 13,            "Space" to 29, "Vampire" to 32, "School" to 23,            "Kids" to 15, "Samurai" to 21,        
for (g in genre) {
    val genreId = jikanGenreMap[g]
if (genreId != null) {

    val res = tryWithSuspend {                    MAL.jikan.search(                        query = "",                        endpoint = "anime",                        page = 1,                        limit = 1,                        orderBy = "members",                        sort = "desc",                        genres = genreId.toString(),
}}}}}
