package com.sanin.tv.connections.mal
import com.sanin.tv.client
import com.sanin.tv.tryWithSuspend
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.URLEncoder
class JikanQueries {
    private val apiUrl = "https://api.jikan.moe/v4"    
companion object {
    private val rateMutex = Mutex()        
private var lastRequestTime = 0L        
private const val MIN_INTERVAL_MS = 350L    }

private suspend 
fun rateLimitedGet(url: String): com.lagradost.nicehttp.NiceResponse {
    var lastResponse: com.lagradost.nicehttp.NiceResponse? = null
var lastException: Exception? = null
var delayMs = 1000L
val maxAttempts = 4
for (attempt in 1..maxAttempts) {            rateMutex.withLock {
    val now = System.currentTimeMillis()                
val elapsed = now - lastRequestTime
if (elapsed < MIN_INTERVAL_MS) {                    delay(MIN_INTERVAL_MS - elapsed)                }                lastRequestTime = System.currentTimeMillis()            }
try {
    val response = client.get(url)                lastResponse = response
if (response.code != 429) {
return response                }            } catch (e: Exception) {                lastException = e            }
if (attempt < maxAttempts) {
    val jitter = (0..200).random().toLong()                delay(delayMs + jitter)                delayMs *= 2            }        }
return lastResponse ?: throw (lastException ?: Exception("Request failed after $maxAttempts attempts"))    }    suspend 
fun search(        query: String,        endpoint: String = "anime",        type: String? = null,        page: Int = 1,        limit: Int = 25,        sfw: Boolean = true,        orderBy: String? = null,        sort: String? = null,        status: String? = null,        rating: String? = null,        genres: String? = null,        startDate: String? = null,        endDate: String? = null,    ): JikanSearchResponse? {
    val params = mutableListOf(            "page" to page.toString(),            "limit" to limit.toString(),            "sfw" to sfw.toString(),        )
if (query.length >= 3) params.add(0, "q" to URLEncoder.encode(query, "UTF-8"))        type?.let { params.add("type" to it) }        orderBy?.let { params.add("order_by" to it) }        sort?.let { params.add("sort" to it) }        status?.let { params.add("status" to it) }        rating?.let { params.add("rating" to it) }        }    }    suspend 
fun getSeasonUpcoming(        page: Int = 1,        limit: Int = 15,    ): JikanSearchResponse? {
return tryWithSuspend {            rateLimitedGet("$apiUrl/seasons/upcoming?page=$page&limit=$limit")                .parsed<JikanSearchResponse>()        }    }    suspend 
fun getSeason(        year: Int,        season: String,        page: Int = 1,        limit: Int = 15,    ): JikanSearchResponse? {
return tryWithSuspend {            rateLimitedGet("$apiUrl/seasons/$year/$season?page=$page&limit=$limit")                .parsed<JikanSearchResponse>()        }    }    suspend 
fun getAnimeById(malId: Int): JikanMediaData? {
return tryWithSuspend {
    val response = rateLimitedGet("$apiUrl/anime/$malId/full")            
val wrapper = response.parsed<JikanSingleResponse>()            wrapper.data        }    }    suspend 
fun getSchedules(        filter: String? = null,          page: Int = 1,        limit: Int = 25,    ): JikanSearchResponse? {
    val params = mutableListOf(            "page" to page.toString(),        }    }    suspend 
fun searchUsers(query: String, page: Int = 1): JikanUserSearchResponse? {
    val encodedQuery = URLEncoder.encode(query, "UTF-8")
return tryWithSuspend {            rateLimitedGet("$apiUrl/users?q=$encodedQuery&page=$page")                .parsed<JikanUserSearchResponse>()        }    }    suspend 
fun getUserProfile(username: String): JikanUserRef? {
    val encodedUsername = URLEncoder.encode(username, "UTF-8")
return tryWithSuspend {            rateLimitedGet("$apiUrl/users/$encodedUsername")                .parsed<JikanUserProfileResponse>()                .data        }    }    suspend 
fun getCharacterFull(malId: Int): JikanCharacterFullData? {
return tryWithSuspend {            rateLimitedGet("$apiUrl/characters/$malId/full")                .parsed<JikanCharacterFullResponse>()                .data        }    }    suspend 
fun getPersonFull(malId: Int): JikanPersonFullData? {
return tryWithSuspend {            rateLimitedGet("$apiUrl/people/$malId/full")                .parsed<JikanPersonFullResponse>()                .data        }    }    suspend 
fun getProducerFull(malId: Int): JikanProducerFullData? {
return tryWithSuspend {            rateLimitedGet("$apiUrl/producers/$malId/full")                .parsed<JikanProducerFullResponse>()
})
