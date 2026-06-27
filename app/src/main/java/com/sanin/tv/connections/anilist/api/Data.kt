package com.sanin.tv.connections.anilist.api
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
class Query {    
@Serializable    
data class Viewer(        
@SerialName("data")        
val data: Data?    ) {        
@Serializable        
data class Data(            
@SerialName("Viewer")            
val user: com.sanin.tv.connections.anilist.api.User?        )    }

@Serializable    
data class Media(        
@SerialName("data")        
val data: Data?    ) {        
@Serializable        
data class Data(            
@SerialName("Media")            
val media: com.sanin.tv.connections.anilist.api.Media?,            
@SerialName("Page")            
val page: com.sanin.tv.connections.anilist.api.Page?        )    }

@Serializable    
data class MediaList(        
@SerialName("data")        
val data: Data?    ) {        
@Serializable        
data class Data(            
@SerialName("Page")            
val page: com.sanin.tv.connections.anilist.api.Page?        )    }

@Serializable    
data class Page(        
@SerialName("data")        
val data: Data?    ) {        
@Serializable        
data class Data(            
@SerialName("Page")            
val page: com.sanin.tv.connections.anilist.api.Page?        )    }//    
data class AiringSchedule(//        
val data : Data?//    ){//        
data class Data(//            
val AiringSchedule: com.sanin.tv.connections.anilist.api.AiringSchedule?//        )//    }

@Serializable    
data class Character(        
@SerialName("data")        
val data: Data?    ) {        
@Serializable        
data class Data(            
@SerialName("Character")            
val character: com.sanin.tv.connections.anilist.api.Character?        )    }

@Serializable    
data class Studio(        
@SerialName("data")        
val data: Data?    ) {        
@Serializable        
data class Data(            
@SerialName("Studio")            
val studio: com.sanin.tv.connections.anilist.api.Studio?        )    }

@Serializable    
data class Author(        
@SerialName("data")        
val data: Data?    ) {        
@Serializable        
data class Data(            
@SerialName("Staff")            
val author: Staff?        )    }    //    
data class MediaList(//        
val data: Data?//    ){//        
data class Data(//            
val MediaList: com.sanin.tv.connections.anilist.api.MediaList?//        )//    }

@Serializable    
data class MediaListCollection(        
@SerialName("data")        
val data: Data?    ) {        
@Serializable        
data class Data(            
@SerialName("MediaListCollection")            
val mediaListCollection: com.sanin.tv.connections.anilist.api.MediaListCollection?        )    }

@Serializable    
data class ToggleFollow(        
@SerialName("data")        
val data: Data?    ) : java.io.Serializable {        
@Serializable        
data class Data(            
@SerialName("ToggleFollow")            
val toggleFollow: FollowData        ) : java.io.Serializable    }

@Serializable    
data class GenreCollection(        
@SerialName("data")        
val data: Data    ) : java.io.Serializable {        
@Serializable        
data class Data(            
@SerialName("GenreCollection")            
val genreCollection: List<String>?        ) : java.io.Serializable    }

@Serializable    
data class MediaTagCollection(        
@SerialName("data")        
val data: Data    ) : java.io.Serializable {        
@Serializable        
data class Data(            
@SerialName("MediaTagCollection")            
val mediaTagCollection: List<MediaTag>?        ) : java.io.Serializable    }

@Serializable    
data class User(        
@SerialName("data")        
val data: Data    ) : java.io.Serializable {        
@Serializable        
data class Data(            
@SerialName("User")            
val user: com.sanin.tv.connections.anilist.api.User?        ) : java.io.Serializable    }

@Serializable    
data class UserProfileResponse(        
@SerialName("data")        
val data: Data    ) : java.io.Serializable {        
@Serializable        
data class Data(            
@SerialName("followerPage")            
val followerPage: UserProfilePage?,            
@SerialName("followingPage")            
val followingPage: UserProfilePage?,            
@SerialName("user")            
val user: UserProfile?        ) : java.io.Serializable    }

@Serializable    
data class UserProfilePage(        
@SerialName("pageInfo")        
val pageInfo: PageInfo,    ) : java.io.Serializable    
@Serializable    
data class Following(        
@SerialName("data")        
val data: Data    ) : java.io.Serializable {        
@Serializable        
data class Data(            
@SerialName("Page")            
val page: FollowingPage?        ) : java.io.Serializable    }

@Serializable    
data class Follower(        
@SerialName("data")        
val data: Data    ) : java.io.Serializable {        
@Serializable        
data class Data(            
@SerialName("Page")            
val page: FollowerPage?        ) : java.io.Serializable    }

@Serializable    
data class FollowerPage(        
@SerialName("followers")        
val followers: List<com.sanin.tv.connections.anilist.api.User>?    ) : java.io.Serializable    
@Serializable    
data class FollowingPage(        
@SerialName("following")        
val following: List<com.sanin.tv.connections.anilist.api.User>?    ) : java.io.Serializable    
@Serializable    
data class ReviewsResponse(        
@SerialName("data")        
val data: Data    ) : java.io.Serializable {        
@Serializable        
data class Data(            
@SerialName("Page")            
val page: ReviewPage?        ) : java.io.Serializable    }

@Serializable    
data class ReviewPage(        
@SerialName("pageInfo")        
val pageInfo: PageInfo,        
@SerialName("reviews")        
val reviews: List<Review>?    ) : java.io.Serializable    
@Serializable    
data class RateReviewResponse(        
@SerialName("data")        
val data: Data    ) : java.io.Serializable {        
@Serializable        
data class Data(            
@SerialName("RateReview")            
val rateReview: Review        ) : java.io.Serializable    }

@Serializable    
data class Review(        
@SerialName("id")        
val id: Int,        
@SerialName("mediaId")        
val mediaId: Int,        
@SerialName("mediaType")        
val mediaType: String,        
@SerialName("summary")        
val summary: String,        
@SerialName("body")        
val body: String,        
@SerialName("rating")        
var rating: Int,        
@SerialName("ratingAmount")        
var ratingAmount: Int,        
@SerialName("userRating")        
var userRating: String,        
@SerialName("score")        
val score: Int,        
@SerialName("private")        
val private: Boolean,    
@Serializable    
data class UserCharacterFavouritesCollection(        
@SerialName("nodes")        
val nodes: List<UserCharacterImageFavorite>,    ) : java.io.Serializable    
@Serializable    
data class UserCharacterImageFavorite(        
@SerialName("id")        
val id: Int,        
@SerialName("name")        
val name: CharacterName,        
@SerialName("image")        
val image: CharacterImage,        
@SerialName("isFavourite")        
val isFavourite: Boolean    ) : java.io.Serializable    
@Serializable    
data class UserStaffFavouritesCollection(        
@SerialName("nodes")        
val nodes: List<UserCharacterImageFavorite>, //downstream it's the same as character    ) : java.io.Serializable    
@Serializable    
data class UserStudioFavouritesCollection(        
@SerialName("nodes")        
val nodes: List<UserStudioFavorite>,    ) : java.io.Serializable    
@Serializable    
data class UserStudioFavorite(        
@SerialName("id")        
val id: Int,        
@SerialName("name")        
val name: String,    ) : java.io.Serializable    //----------------------------------------        
@SerialName("standardDeviation")        
val standardDeviation: Float,        
@SerialName("minutesWatched")        
val minutesWatched: Int,        
@SerialName("episodesWatched")        
val episodesWatched: Int,        
@SerialName("chaptersRead")        
val chaptersRead: Int,        
@SerialName("volumesRead")        
val volumesRead: Int,        
@SerialName("formats")        
val formats: List<StatisticsFormat>,        
@SerialName("statuses")        
val statuses: List<StatisticsStatus>,        
@SerialName("scores")        
val scores: List<StatisticsScore>,        
@SerialName("lengths")        
val lengths: List<StatisticsLength>,        
@SerialName("releaseYears")        
val releaseYears: List<StatisticsReleaseYear>,        
@SerialName("startYears")        
val startYears: List<StatisticsStartYear>,        
@SerialName("genres")        
val genres: List<StatisticsGenre>,        
@SerialName("tags")        
val tags: List<StatisticsTag>,        
@SerialName("countries")        
val countries: List<StatisticsCountry>,        
@SerialName("voiceActors")        
val voiceActors: List<StatisticsVoiceActor>,        
@SerialName("staff")        
val staff: List<StatisticsStaff>,        
@SerialName("studios")        
val studios: List<StatisticsStudio>    ) : java.io.Serializable    
@Serializable    
data class StatisticsFormat(        
@SerialName("count")        
val count: Int,        
@SerialName("meanScore")        
val meanScore: Float,        
@SerialName("minutesWatched")        
val minutesWatched: Int,        
@SerialName("chaptersRead")        
val chaptersRead: Int,        
@SerialName("mediaIds")        
val mediaIds: List<Int>,        
@SerialName("format")        
val format: String    ) : java.io.Serializable    
@Serializable    
data class StatisticsStatus(        
@SerialName("count")        
val count: Int,        
@SerialName("meanScore")        
val meanScore: Float,        
@SerialName("minutesWatched")        
val minutesWatched: Int,        
@SerialName("chaptersRead")        
val chaptersRead: Int,        
@SerialName("mediaIds")        
val mediaIds: List<Int>,        
@SerialName("status")        
val status: String    ) : java.io.Serializable    
@Serializable    
data class StatisticsScore(        
@SerialName("count")        
val count: Int,        
@SerialName("meanScore")        
val meanScore: Float,        
@SerialName("minutesWatched")        
val minutesWatched: Int,        
@SerialName("chaptersRead")        
val chaptersRead: Int,        
@SerialName("mediaIds")        
val mediaIds: List<Int>,        
@SerialName("meanScore")        
val meanScore: Float,        
@SerialName("minutesWatched")        
val minutesWatched: Int,        
@SerialName("chaptersRead")        
val chaptersRead: Int,        
@SerialName("mediaIds")        
val mediaIds: List<Int>,        
@SerialName("startYear")        
val startYear: Int    ) : java.io.Serializable    
@Serializable    
data class StatisticsGenre(        
@SerialName("count")        
val count: Int,        
@SerialName("meanScore")        
val meanScore: Float,        
@SerialName("minutesWatched")        
val minutesWatched: Int,        
@SerialName("chaptersRead")        
val chaptersRead: Int,        
@SerialName("mediaIds")        
val mediaIds: List<Int>,        
@SerialName("genre")        
val genre: String    ) : java.io.Serializable    
@Serializable    
data class StatisticsTag(        
@SerialName("count")        
val count: Int,        
@SerialName("meanScore")        
val meanScore: Float,        
@SerialName("minutesWatched")        
val minutesWatched: Int,        
@SerialName("chaptersRead")        
val chaptersRead: Int,        
@SerialName("mediaIds")        
val mediaIds: List<Int>,        
@SerialName("tag")        
val tag: Tag    ) : java.io.Serializable    
@Serializable    
data class Tag(        
@SerialName("id")        
val id: Int,        
@SerialName("name")        
val name: String    ) : java.io.Serializable    
@Serializable    
data class StatisticsCountry(        
@SerialName("count")        
val count: Int,        
@SerialName("meanScore")        
val meanScore: Float,        
@SerialName("minutesWatched")        
val minutesWatched: Int,        
@SerialName("chaptersRead")        
val chaptersRead: Int,        
@SerialName("mediaIds")        
val mediaIds: List<Int>,        
@SerialName("country")        
val country: String    ) : java.io.Serializable    
@Serializable    
data class StatisticsVoiceActor(        
@SerialName("count")        
val count: Int,        
@SerialName("meanScore")        
val meanScore: Float,        
@SerialName("minutesWatched")        
val minutesWatched: Int,        
@SerialName("chaptersRead")        
val chaptersRead: Int,        
@SerialName("mediaIds")        
val mediaIds: List<Int>,        
@SerialName("voiceActor")        
val voiceActor: VoiceActor,        
@SerialName("characterIds")        
val characterIds: List<Int>    ) : java.io.Serializable    
@Serializable    
data class VoiceActor(        
@SerialName("id")        
val id: Int,        
@SerialName("name")        
val name: StaffName    ) : java.io.Serializable    
@Serializable    
data class StaffName(        
@SerialName("first")        
val first: String?,        
@SerialName("middle")        
val middle: String?,        
@SerialName("last")        
val last: String?,        
@SerialName("full")        
val full: String?,        
@SerialName("native")        
val native: String?,        
@SerialName("alternative")        
val alternative: List<String>?,        
@SerialName("userPreferred")        
val userPreferred: String?    ) : java.io.Serializable    
@Serializable    
data class StatisticsStaff(        
@SerialName("count")        
val count: Int,        
@SerialName("meanScore")        
val meanScore: Float,        
@SerialName("minutesWatched")        
val minutesWatched: Int,        
@SerialName("chaptersRead")        
val chaptersRead: Int,        
@SerialName("mediaIds")        
val mediaIds: List<Int>,        
@SerialName("staff")        
val staff: VoiceActor    ) : java.io.Serializable    
@Serializable    
data class StatisticsStudio(        
@SerialName("count")        
val count: Int,        
@SerialName("meanScore")        
val meanScore: Float,        
@SerialName("minutesWatched")        
val minutesWatched: Int,        
@SerialName("chaptersRead")        
val chaptersRead: Int,        
@SerialName("mediaIds")        
val mediaIds: List<Int>,        
@SerialName("studio")        
val studio: StatStudio    ) : java.io.Serializable    
@Serializable    
data class StatStudio(        
@SerialName("id")        
val id: Int,        
@SerialName("name")        
val name: String,        
@SerialName("isAnimationStudio")        
val isAnimationStudio: Boolean    ) : java.io.Serializable}//
data class WhaData(//    
val Studio: Studio?,////    // Follow query//    
val Following: User?,////    // Follow query//    
val Follower: User?,////    // Thread query//    
val Thread: Thread?,////    // Recommendation query//    
val Recommendation: Recommendation?,////    // Like query//    
val Like: User?,//    // Review query//    
val Review: Review?,////    // Activity query//    
val Activity: ActivityUnion?,////    // Activity reply query//    
val ActivityReply: ActivityReply?,//    // CommentNotificationWorker query//    
val ThreadComment: List<ThreadComment>?,//    // Notification query//    
val Notification: NotificationUnion?,//    // Media Trend query//    
val MediaTrend: MediaTrend?,//    // Provide AniList markdown to be converted to html (Requires auth)//    
val Markdown: ParsedMarkdown?,//    // SiteStatistics: SiteStatistics//    
val AniChartUser: AniChartUser?,//)))))))))))))
))))))
