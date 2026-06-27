package com.sanin.tv.connections.anilist.api
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
@Serializable
data class User(    // The id of the user    
@SerialName("id") 
var id: Int,    // The name of the user    
@SerialName("name") 
var name: String?,    // The bio written by user (Markdown)    //    
@SerialName("about") 
var about: String?,    // The user's avatar images    
@SerialName("avatar") 
var avatar: UserAvatar?,    // The user's banner images    
@SerialName("bannerImage") 
var bannerImage: String?,    // If the authenticated user if following this user    
@SerialName("isFollowing") 
var isFollowing: Boolean?,    // If this user if following the authenticated user    
@SerialName("isFollower") 
var isFollower: Boolean?,    // If the user is blocked by the authenticated user    //    
@SerialName("isBlocked") 
var isBlocked: Boolean?,    // FIXME: No documentation is provided for "Json"    // 
@SerialName("bans") 
var bans: Json?,    // The user's general options    
@SerialName("options") 
var options: UserOptions?,    // The user's media list options    
@SerialName("mediaListOptions") 
var mediaListOptions: MediaListOptions?,
@Serializable
enum class UserTitleLanguage {    
@SerialName("ENGLISH")    ENGLISH,    
@SerialName("ROMAJI")    ROMAJI,    
@SerialName("NATIVE")    NATIVE}

@Serializable
enum class UserStaffNameLanguage {    
@SerialName("ROMAJI_WESTERN")    ROMAJI_WESTERN,    
@SerialName("ROMAJI")    ROMAJI,    
@SerialName("NATIVE")    NATIVE}

@Serializable
enum class ScoreFormat {    
@SerialName("POINT_100")    POINT_100,    
@SerialName("POINT_10_DECIMAL")    POINT_10_DECIMAL,    
@SerialName("POINT_10")    POINT_10,    
@SerialName("POINT_5")    POINT_5,    
@SerialName("POINT_3")
})
