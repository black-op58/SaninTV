package com.sanin.tv.parsers

Logger.log("Searching : ${mediaObj.mainName()}")            
val results = search(mediaObj.mainName())            //log all results            results.forEach {                Logger.log("Result: ${it.name}")            }

val sortedResults = if (results.isNotEmpty()) {                results.sortedByDescending {                    FuzzySearch.ratio(                        it.name.lowercase(),                        mediaObj.mainName().lowercase()                    )                }
} else {                emptyList()            }            response = sortedResults.firstOrNull()
if (response == null || FuzzySearch.ratio(                    response.name.lowercase(),                    mediaObj.mainName().lowercase()                ) < 100            ) {                setUserText("Searching : ${mediaObj.nameRomaji}")                Logger.log("Searching : ${mediaObj.nameRomaji}")                
val romajiResults = search(mediaObj.nameRomaji)                
val sortedRomajiResults = if (romajiResults.isNotEmpty()) {                    romajiResults.sortedByDescending {                        FuzzySearch.ratio(                            it.name.lowercase(),                            mediaObj.nameRomaji.lowercase()                        )                    }
} else {                    emptyList()                }

val closestRomaji = sortedRomajiResults.firstOrNull()                Logger.log("Closest match from RomajiResults: ${closestRomaji?.name ?: "None"}")                response = if (response == null) {                    Logger.log("No exact match found in results. Using closest match from RomajiResults.")                    closestRomaji
} else {
    val romajiRatio = FuzzySearch.ratio(                        closestRomaji?.name?.lowercase() ?: "",                        mediaObj.nameRomaji.lowercase()                    )                    
val mainNameRatio = FuzzySearch.ratio(                        response.name.lowercase(),                        mediaObj.mainName().lowercase()                    )                    Logger.log("Fuzzy ratio for closest match in results: $mainNameRatio for ${response.name.lowercase()}")                    Logger.log("Fuzzy ratio for closest match in RomajiResults: $romajiRatio for ${closestRomaji?.name?.lowercase() ?: "None"}")
if (romajiRatio > mainNameRatio) {                        Logger.log("RomajiResults has a closer match. Replacing response.")                        closestRomaji
} else {                        Logger.log("Results has a closer or equal match. Keeping existing response.")                        response                    }                }            }            saveShowResponse(mediaObj.id, response)        }
return response    }    /**     * ping the site to check if it's working or not.     * @return Triple<Int, Int?, String> : First Int is the status code, Second Int is the response time in milliseconds, Third String is the response message.     */    
fun ping(): Triple<Int, Int?, String> {
    val client = OkHttpClient()        
var statusCode = 0
var responseTime: Int? = null
var responseMessage = ""        println("Pinging $name at $hostUrl")
try {
    val request = Request.Builder()                .url(hostUrl)                .build()            responseTime = measureTimeMillis {                client.newCall(request).execute().use { response ->                    statusCode = response.code                    responseMessage = response.message.ifEmpty { "None" }                }            }.toInt()        } catch (e: Exception) {            Logger.log("Failed to ping $name")            statusCode = -1            responseMessage = if (e.message.isNullOrEmpty()) "None" else e.message!!            Logger.log(e)        }
return Triple(statusCode, responseTime, responseMessage)    }    /**     * Used to get an existing Search Response which was selected by the user.     * @param mediaId : The mediaId of the Media object.     * @return ShowResponse? : The ShowResponse 
object if found, else null.     */    
open suspend 
fun loadSavedShowResponse(mediaId: Int): ShowResponse? {        checkIfVariablesAreEmpty()
return PrefManager.getNullableCustomVal(            "${saveName}_$mediaId",            null,            ShowResponse::class.java        )    }    /**     * Used to save Shows Response using `saveName`.     * @param mediaId : The mediaId of the Media object.     * @param response : The ShowResponse 
object to save.     * @param selected : Boolean : If the ShowResponse was selected by the user or not.     */    
open fun saveShowResponse(mediaId: Int, response: ShowResponse?, selected: Boolean = false) {
if (response != null) {