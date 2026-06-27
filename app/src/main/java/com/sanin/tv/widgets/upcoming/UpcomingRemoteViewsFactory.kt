package com.sanin.tv.widgets.upcoming

val hours = (timeUntil % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)        
val minutes = ((timeUntil % (1000 * 60 * 60 * 24)) % (1000 * 60 * 60)) / (1000 * 60)
return if (timeUntil >= 0) {            buildString {
if (days > 0) append("$days day${if (days > 1) "s" else ""} ")
if (hours > 0 || days > 0) append("$hours hour${if (hours > 1) "s" else ""} ")                append("$minutes minute${if (minutes > 1) "s" else ""}")            }.trim()
} else {
    val elapsedDays = -days
val elapsedHours = -hours
val elapsedMinutes = -minutes            buildString {                append("Aired ")
if (elapsedDays > 0) append("$elapsedDays day${if (elapsedDays > 1) "s" else ""} ")
if (elapsedHours > 0 || elapsedDays > 0) append("$elapsedHours hour${if (elapsedHours > 1) "s" else ""} ")                append("$elapsedMinutes minute${if (elapsedMinutes > 1) "s" else ""} ago")            }.trim()        }    }

override fun onDataSetChanged() {
if (refreshing) return        Logger.log("UpcomingRemoteViewsFactory onDataSetChanged")        widgetItems.clear()        fillWidgetItems()    }

private fun fillWidgetItems() {        refreshing = true
val userId = PrefManager.getVal<String>(PrefName.AnilistUserId)        
val prefs = context.getSharedPreferences(UpcomingWidget.PREFS_NAME, Context.MODE_PRIVATE)        
val lastUpdated = prefs.getLong(UpcomingWidget.LAST_UPDATE, 0)        
val serializedMedia = prefs.getString(UpcomingWidget.PREF_SERIALIZED_MEDIA, "")        
val timeSinceLastUpdate = System.currentTimeMillis() - lastUpdated
val media = if (!serializedMedia.isNullOrEmpty()) deserializeMedia(serializedMedia) else null
var forceRefresh = false
if (media != null) {
for (mediaItem in media) {
    val timeUntilAiring = (mediaItem.timeUntilAiring?.minus(timeSinceLastUpdate) ?: 0)
if (timeUntilAiring <= 0 || mediaItem.anime?.nextAiringEpisode == null) {                    forceRefresh = true                    break                }            }        }
if (timeSinceLastUpdate > 1000 * 60 * 60 * 4 || serializedMedia.isNullOrEmpty() || forceRefresh) {            runBlocking(Dispatchers.IO) {                Anilist.getSavedToken()                
val upcoming = Anilist.query.getUpcomingAnime(userId)                
val seen = mutableSetOf<Int>()                upcoming.forEach { mediaItem ->
if (seen.add(mediaItem.id)) {
    val timeUntilAiring = mediaItem.timeUntilAiring ?: 0
if (timeUntilAiring > 0) {
    val episodeNumber =  mediaItem.anime?.nextAiringEpisode?.let { it + 1 }                            widgetItems.add(                                WidgetItem(                                    title = mediaItem.userPreferredName,                                    countdown = formatTime(timeUntilAiring),                                    image = mediaItem.cover ?: "",                                    id = mediaItem.id,                                    episode = episodeNumber                                )                            )                        }                    }                }                prefs.edit().putLong(UpcomingWidget.LAST_UPDATE, System.currentTimeMillis()).apply()                
val serialized = serializeMedia(upcoming)
if (serialized != null) {                    prefs.edit().putString(UpcomingWidget.PREF_SERIALIZED_MEDIA, serialized).apply()
} else {                    prefs.edit().putString(UpcomingWidget.PREF_SERIALIZED_MEDIA, "").apply()                    Logger.log("Error serializing media")                }                refreshing = false            }
} else {            refreshing = false
if (media != null) {
    val seen = mutableSetOf<Int>()                media.forEach { mediaItem ->
if (seen.add(mediaItem.id)) {
    val timeUntilAiring = (mediaItem.timeUntilAiring?.minus(timeSinceLastUpdate) ?: 0)
if (timeUntilAiring > 0) {
    val episodeNumber = mediaItem.anime?.nextAiringEpisode?.let { it + 1 }                            widgetItems.add(                                WidgetItem(                                    title = mediaItem.userPreferredName,                                    countdown = formatTime(timeUntilAiring),                                    image = mediaItem.cover ?: "",                                    id = mediaItem.id,                                    episode = episodeNumber                                )                            )                        }                    }                }
} else {                prefs.edit().putString(UpcomingWidget.PREF_SERIALIZED_MEDIA, "").apply()                prefs.edit().putLong(UpcomingWidget.LAST_UPDATE, 0).apply()                Logger.log("Error deserializing media")                fillWidgetItems()            }        }    }

private fun serializeMedia(media: List<Media>): String? {
return try {
    val gson = GsonBuilder()                .registerTypeAdapter(SAnime::class.java, InstanceCreator<SAnime> {                    SAnimeImpl()                })                .registerTypeAdapter(SEpisode::class.java, InstanceCreator<SEpisode> {                    SEpisodeImpl()                })                .create()            gson.toJson(media)        } catch (e: Exception) {            Logger.log("Error serializing media: $e")            Logger.log(e)            null        }    }

private fun deserializeMedia(json: String): List<Media>? {
return try {
    val gson = GsonBuilder()                .registerTypeAdapter(SAnime::class.java, InstanceCreator<SAnime> {                    SAnimeImpl()                })                .registerTypeAdapter(SEpisode::class.java, InstanceCreator<SEpisode> {                    SEpisodeImpl()                })                .create()            gson.fromJson(json, Array<Media>::class.java).toList()        } catch (e: Exception) {            Logger.log("Error deserializing media: $e")            Logger.log(e)            null        }    }

override fun onDestroy() {        widgetItems.clear()    }

override fun getCount(): Int = widgetItems.size    
override fun getViewAt(position: Int): RemoteViews {
    val image: String,    
val id: Int,    
val episode: Int?
