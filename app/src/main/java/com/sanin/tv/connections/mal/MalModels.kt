package com.sanin.tv.connections.mal
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
@Serializable
data class MalPaging(    
val previous: String? = null,    
val next: String? = null,)
@Serializable
data class MalPicture(    
val medium: String? = null,    
val large: String? = null,)
@Serializable
data class MalAlternativeTitles(    
val synonyms: List<String>? = null,    
val en: String? = null,    
val ja: String? = null,)
@Serializable
data class MalGenre(    
val id: Int,    
val name: String,)
@Serializable
data class MalStudio(    
val id: Int,    
val name: String,)
@Serializable
data class MalSeason(    
val year: Int,    
val season: String,
data class MalRelatedEdge(    
val node: MalAnimeNode? = null,    
@SerialName("relation_type") 
val relationType: String? = null,    
@SerialName("relation_type_formatted") 
val relationTypeFormatted: String? = null,)
@Serializable
data class MalListStatus(    
val status: String? = null,    
val score: Int = 0,    
@SerialName("num_episodes_watched") 
val numEpisodesWatched: Int? = null,    
@SerialName("num_chapters_read") 
val numChaptersRead: Int? = null,    
@SerialName("num_volumes_read") 
val numVolumesRead: Int? = null,    
@SerialName("is_rewatching") 
val isRewatching: Boolean = false,    
@SerialName("is_rereading") 
val isRereading: Boolean = false,    
@SerialName("updated_at") 
val updatedAt: String? = null,    
@SerialName("start_date") 
val startDate: String? = null,    
@SerialName("finish_date") 
val finishDate: String? = null,)
@Serializable
data class MalListEntry(    
val node: MalAnimeNode,    
@SerialName("list_status") 
val listStatus: MalListStatus? = null,)
@Serializable
data class MalListResponse(    
val data: List<MalListEntry> = emptyList(),    
val paging: MalPaging? = null,)
@Serializable
data class MalRankingEntry(    
val node: MalAnimeNode,    
val ranking: MalRankingPosition? = null,)    
@SerialName("large_image_url") 
val largeImageUrl: String? = null,)
@Serializable
data class JikanAired(    
val from: String? = null,    
val to: String? = null,)
@Serializable
data class JikanGenre(    
@SerialName("mal_id") 
val malId: Int,    
val name: String,)
@Serializable
data class JikanStudio(    
@SerialName("mal_id") 
val malId: Int,    
val name: String,)
@Serializable
data class JikanMediaData(    
@SerialName("mal_id") 
val malId: Int,    
val title: String? = null,    
@SerialName("title_english") 
val titleEnglish: String? = null,    
@SerialName("title_japanese") 
val titleJapanese: String? = null,    
val images: JikanImages? = null,    
val synopsis: String? = null,    
val score: Float? = null,    
val rank: Int? = null,    
val popularity: Int? = null,    
val episodes: Int? = null,    
val chapters: Int? = null,    
val volumes: Int? = null,    
val type: String? = null,    
val status: String? = null,    
val url: String? = null,    
val aired: JikanAired? = null,    
val published: JikanAired? = null,    
val duration: String? = null,    
val rating: String? = null,    
val season: String? = null,    
val year: Int? = null,    
@SerialName("title_synonyms") 
val titleSynonyms: List<String>? = null,    
val genres: List<JikanGenre>? = null,    
@SerialName("explicit_genres") 
val explicitGenres: List<JikanGenre>? = null,    
val themes: List<JikanGenre>? = null,    
val demographics: List<JikanGenre>? = null,    
val studios: List<JikanStudio>? = null,    
val producers: List<JikanStudio>? = null,    
val licensors: List<JikanStudio>? = null,    
val authors: List<JikanAuthor>? = null,    
val theme: JikanTheme? = null,    
val trailer: JikanTrailer? = null,    
val relations: List<JikanRelation>? = null,    
val recommendations: List<JikanRecommendation>? = null,    
val external: List<JikanExternal>? = null,    
val streaming: List<JikanExternal>? = null,    
val source: String? = null,    
val broadcast: JikanBroadcast? = null,    
val favorites: Int? = null,    
val members: Int? = null,    
@SerialName("scored_by") 
val scoredBy: Int? = null,)
@Serializable
data class JikanBroadcast(    
val day: String? = null,    
val time: String? = null,    
val timezone: String? = null,    
val string: String? = null,)
@Serializable
data class JikanSearchResponse(    
val data: List<JikanMediaData> = emptyList(),    
val pagination: JikanPagination? = null,)
@Serializable
data class JikanSingleResponse(    
val data: JikanMediaData? = null,)
@Serializable
data class JikanTrailer(    
@SerialName("youtube_id") 
val youtubeId: String? = null,    
@SerialName("embed_url") 
val embedUrl: String? = null,) {
    fun effectiveYoutubeId(): String? {
if (!youtubeId.isNullOrBlank()) return youtubeId
val url = embedUrl ?: return null
val regex = Regex("""youtube(?:-nocookie)?\.com/embed/([\w-]+)""")        return regex.find(url)?.groupValues?.getOrNull(1)    }}

@Serializable
data class JikanTheme(    
val openings: List<String>? = null,    
val endings: List<String>? = null,)
@Serializable
data class JikanExternal(    
val name: String? = null,    
val url: String? = null,)
@Serializable
data class JikanRecommendation(    
val entry: JikanRecommendationEntry? = null,)
@Serializable
data class JikanRecommendationsResponse(    
val data: List<JikanRecommendation> = emptyList())
@Serializable
data class JikanRecommendationEntry(    
@SerialName("mal_id") 
val malId: Int,    
val title: String? = null,    
val images: JikanImages? = null,)
@Serializable
data class JikanRelation(    
val relation: String? = null,    
val entry: List<JikanRelationEntry>? = null,)
@Serializable
data class JikanRelationEntry(    
@SerialName("mal_id") 
val malId: Int,    
val type: String? = null,    
val name: String? = null,    
val url: String? = null,)
@Serializable
data class JikanAuthor(    
val person: JikanPersonRef? = null,    
val position: String? = null,)
@Serializable
data class JikanCharacterRef(    
@SerialName("mal_id") 
val malId: Int,    
val url: String? = null,    
val images: JikanImages? = null,    
val name: String? = null,    
val favorites: Int? = null,)
@Serializable
data class JikanPersonRef(    
@SerialName("mal_id") 
val malId: Int,    
val url: String? = null,    
val images: JikanImages? = null,    
val name: String? = null,)
@Serializable
data class JikanAnimeCharacter(    
val character: JikanCharacterRef? = null,    
val role: String? = null,    
@SerialName("voice_actors") 
val voiceActors: List<JikanPersonVoiceActor>? = null,)
@Serializable
data class JikanPersonVoiceActor(    
val person: JikanPersonRef? = null,    
val language: String? = null,)
@Serializable
data class JikanStaffMember(    
val person: JikanPersonRef? = null,    
val positions: List<String>? = null,)
@Serializable
data class JikanAnimeCharactersResponse(    
val data: List<JikanAnimeCharacter> = emptyList(),    
val pagination: JikanPagination? = null,)
@Serializable
data class JikanStaffResponse(    
val data: List<JikanStaffMember> = emptyList(),    
val pagination: JikanPagination? = null,)
@Serializable
data class JikanReviewResponse(    
val data: List<JikanReview> = emptyList(),    
val pagination: JikanPagination? = null,)
@Serializable
data class JikanReview(    
@SerialName("mal_id") 
val malId: Int,    
val url: String? = null,    
val type: String? = null,    
val reactions: JikanReviewReactions? = null,    
val date: String? = null,    
val review: String? = null,    
val score: Int? = null,    
val tags: List<String>? = null,    
@SerialName("is_spoiler") 
val isSpoiler: Boolean = false,    
@SerialName("is_preliminary") 
val isPreliminary: Boolean = false,    
val user: JikanReviewUser? = null,)
@Serializable
data class JikanReviewReactions(    
val overall: Int? = null,    
val nice: Int? = null,    
@SerialName("love_it") 
val loveIt: Int? = null,    
val funny: Int? = null,    
val confusing: Int? = null,    
val informative: Int? = null,    
@SerialName("well_written") 
val wellWritten: Int? = null,    
val creative: Int? = null,)
@Serializable
data class JikanReviewUser(    
val username: String? = null,    
val url: String? = null,    
val images: JikanImages? = null,)
@Serializable
data class JikanCharacterSearchResponse(    
val data: List<JikanCharacterRef> = emptyList(),    
val pagination: JikanPagination? = null,)
@Serializable
data class JikanPersonSearchResponse(    
val data: List<JikanPersonRef> = emptyList(),    
val pagination: JikanPagination? = null,)
@Serializable
data class JikanProducerSearchResponse(    
val data: List<JikanProducer> = emptyList(),    
val pagination: JikanPagination? = null,)
@Serializable
data class JikanProducer(    
@SerialName("mal_id") 
val malId: Int,    
val name: String? = null,    
val url: String? = null,    
val images: JikanImages? = null,    
val favorites: Int? = null,)
@Serializable
data class JikanUserSearchResponse(    
val data: List<JikanUserRef> = emptyList(),    
val pagination: JikanPagination? = null,)
@Serializable
data class JikanUserProfileResponse(    
val data: JikanUserRef? = null)
@Serializable
data class JikanUserRef(    
val username: String? = null,    
val url: String? = null,    
val images: JikanImages? = null,)
@Serializable
data class JikanProducerTitle(    
val type: String? = null,    
val title: String? = null,)