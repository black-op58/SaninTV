package com.sanin.tv.connections.mal

suspend fun getAnimeSuggestions(limit: Int = 15): MalRankingResponse? {
    val headers = authHeader ?: return null
return tryWithSuspend {            executeRequest {                client.get(                    "$apiUrl/anime/suggestions?limit=$limit&fields=$rankingFields",                    headers                )            }.parsed<MalRankingResponse>()        }    }

private val sequelListFields = "list_status,num_episodes,main_picture,mean,media_type,status,related_anime${recRelFields}"    suspend 
fun getCompletedAnimeWithRelations(limit: Int = 100): MalListResponse? {
    val headers = authHeader ?: return null
return tryWithSuspend {            executeRequest {                client.get(                    "$apiUrl/users/@me/animelist?fields=$sequelListFields&status=completed&sort=list_updated_at&limit=$limit&nsfw=1",                    headers                )            }.parsed<MalListResponse>()        }    }    suspend 
fun getAllUserAnimeIds(): Set<Int> {
    val allIds = mutableSetOf<Int>()        
var offset = 0
val batchSize = 100
val headers = authHeader ?: return emptySet()
while (true) {
    val response = tryWithSuspend {                executeRequest {                    client.get(                        "$apiUrl/users/@me/animelist?fields=&sort=list_updated_at&limit=$batchSize&offset=$offset&nsfw=1",                        headers                    )                }.parsed<MalListResponse>()            } ?: break            response.data.forEach { allIds.add(it.node.id) }
if (response.data.size < batchSize || response.paging?.next == null) break            offset += batchSize        }
return allIds    }
