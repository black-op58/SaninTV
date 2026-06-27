package com.sanin.tv.connections.anilist

import com.sanin.tv.connections.anilist.Anilist.executeQuery
import com.sanin.tv.connections.anilist.api.FuzzyDate
import com.sanin.tv.connections.anilist.api.Query
import com.sanin.tv.connections.anilist.api.ToggleLike
import com.sanin.tv.currContext
import com.google.gson.Gson
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

class AnilistMutations {

    suspend fun updateSettings(
        timezone: String? = null,
        titleLanguage: String? = null,
        staffNameLanguage: String? = null,
        activityMergeTime: Int? = null,
        airingNotifications: Boolean? = null,
        displayAdultContent: Boolean? = null,
        restrictMessagesToFollowing: Boolean? = null,
        scoreFormat: String? = null,
        rowOrder: String? = null,
    ) {
        val query = """
            mutation (
                ${"$"}timezone: String,
                ${"$"}titleLanguage: UserTitleLanguage,
                ${"$"}staffNameLanguage: UserStaffNameLanguage,
                ${"$"}activityMergeTime: Int,
                ${"$"}airingNotifications: Boolean,
                ${"$"}displayAdultContent: Boolean,
                ${"$"}restrictMessagesToFollowing: Boolean,
                ${"$"}scoreFormat: ScoreFormat,
                ${"$"}rowOrder: String
            ) {
                UpdateUser(
                    timezone: ${"$"}timezone,
                    titleLanguage: ${"$"}titleLanguage,
                    staffNameLanguage: ${"$"}staffNameLanguage,
                    activityMergeTime: ${"$"}activityMergeTime,
                    airingNotifications: ${"$"}airingNotifications,
                    displayAdultContent: ${"$"}displayAdultContent,
                    restrictMessagesToFollowing: ${"$"}restrictMessagesToFollowing,
                    scoreFormat: ${"$"}scoreFormat,
                    rowOrder: ${"$"}rowOrder,
                ) {
                    id
                    options {
                        timezone
                        titleLanguage
                        staffNameLanguage
                        activityMergeTime
                        airingNotifications
                        displayAdultContent
                        restrictMessagesToFollowing
                    }
                    mediaListOptions {
                        scoreFormat
                        rowOrder
                    }
                }
            }
        """.trimIndent()
        val variables = """{
            ${timezone?.let { "\"timezone\":\"$it\"" } ?: ""}
            ${titleLanguage?.let { "\"titleLanguage\":\"$it\"" } ?: ""}
            ${staffNameLanguage?.let { "\"staffNameLanguage\":\"$it\"" } ?: ""}
            ${activityMergeTime?.let { "\"activityMergeTime\":$it" } ?: ""}
            ${airingNotifications?.let { "\"airingNotifications\":$it" } ?: ""}
            ${displayAdultContent?.let { "\"displayAdultContent\":$it" } ?: ""}
            ${restrictMessagesToFollowing?.let { "\"restrictMessagesToFollowing\":$it" } ?: ""}
            ${scoreFormat?.let { "\"scoreFormat\":\"$it\"" } ?: ""}
            ${rowOrder?.let { "\"rowOrder\":\"$it\"" } ?: ""}
        }""".trimIndent().replace("\n", "").replace("    ", "").replace(",}", "}")
        executeQuery<JsonObject>(query, variables)
    }

    suspend fun editList(
        mediaId: Int,
        progress: Int? = null,
        progressVolumes: Int? = null,
        score: Double? = null,
        status: String? = null,
        startedAt: FuzzyDate? = null,
        completedAt: FuzzyDate? = null,
        repeat: Int? = null,
        notes: String? = null,
        private: Boolean? = null,
        customLists: List<String>? = null,
        advancedScores: List<Double>? = null,
    ) {
        val scoreRaw = score?.let { (it * 10).toInt() }
        val startArg = if (startedAt != null) "=" + startedAt.toVariableString() else ""
        val completedArg = if (completedAt != null) "=" + completedAt.toVariableString() else ""
        val query = """
            mutation (
                ${"$"}mediaID: Int,
                ${"$"}progress: Int,
                ${"$"}progressVolumes: Int,
                ${"$"}repeat: Int,
                ${"$"}notes: String,
                ${"$"}private: Boolean,
                ${"$"}scoreRaw: Int,
                ${"$"}status: MediaListStatus,
                ${"$"}start: FuzzyDateInput$startArg,
                ${"$"}completed: FuzzyDateInput$completedArg,
                ${"$"}customLists: [String]
            ) {
                SaveMediaListEntry(
                    mediaId: ${"$"}mediaID,
                    progress: ${"$"}progress,
                    progressVolumes: ${"$"}progressVolumes,
                    repeat: ${"$"}repeat,
                    notes: ${"$"}notes,
                    private: ${"$"}private,
                    scoreRaw: ${"$"}scoreRaw,
                    status: ${"$"}status,
                    startedAt: ${"$"}start,
                    completedAt: ${"$"}completed,
                    customLists: ${"$"}customLists
                ) {
                    score(format: POINT_10_DECIMAL)
                    startedAt { year month day }
                    completedAt { year month day }
                }
            }
        """.trimIndent()
        val variables = buildString {
            append("""{"mediaID":$mediaId""")
            if (private != null)       append(""","private":$private""")
            if (progress != null)      append(""","progress":$progress""")
            if (progressVolumes != null) append(""","progressVolumes":$progressVolumes""")
            if (scoreRaw != null)      append(""","scoreRaw":$scoreRaw""")
            if (repeat != null)        append(""","repeat":$repeat""")
            if (notes != null)         append(""","notes":"${notes.replace("\n", "\\n")}"""")
            if (status != null)        append(""","status":"$status"""")
            if (customLists != null)   append(""","customLists":[${customLists.joinToString { "\"$it\"" }}]""")
            append("}")
        }
        executeQuery<JsonObject>(query, variables, show = true)
    }

    suspend fun deleteList(listId: Int) {
        val query = """
            mutation(${"$"}id: Int) {
                DeleteMediaListEntry(id: ${"$"}id) {
                    deleted
                }
            }
        """.trimIndent()
        val variables = """{"id":$listId}"""
        executeQuery<JsonObject>(query, variables)
    }

    suspend fun rateReview(reviewId: Int, rating: String): Query.RateReviewResponse? {
        val query = """
            mutation {
                RateReview(reviewId: $reviewId, rating: $rating) {
                    id mediaId mediaType summary body(asHtml: true)
                    rating ratingAmount userRating score private
                    siteUrl createdAt updatedAt
                    user { id name bannerImage avatar { medium large } }
                }
            }
        """.trimIndent()
        return executeQuery<Query.RateReviewResponse>(query)
    }

    suspend fun toggleFollow(id: Int): Query.ToggleFollow? {
        return executeQuery<Query.ToggleFollow>("""
            mutation {
                ToggleFollow(userId: $id) { id isFollowing isFollower }
            }
        """.trimIndent())
    }

    suspend fun toggleLike(id: Int, type: String): ToggleLike? {
        return executeQuery<ToggleLike>("""
            mutation Like {
                ToggleLikeV2(id: $id, type: $type) { __typename }
            }
        """.trimIndent())
    }

    suspend fun toggleActivitySubscription(activityId: Int, subscribe: Boolean): Boolean {
        val result = executeQuery<JsonObject>("""
            mutation {
                ToggleActivitySubscription(activityId: $activityId, subscribe: $subscribe) {
                    __typename
                }
            }
        """.trimIndent())
        val errors = result?.get("errors") as? JsonArray
        return result != null && errors.isNullOrEmpty()
    }

    suspend fun postActivity(text: String, edit: Int? = null): String {
        val encodedText = text.stringSanitizer()
        val idPart = if (edit != null) "id: $edit," else ""
        val query = """
            mutation {
                SaveTextActivity($idPart text: $encodedText) { siteUrl }
            }
        """.trimIndent()
        val result = executeQuery<JsonObject>(query)
        val errors = result?.get("errors")
        return errors?.toString() ?: (currContext()?.getString(com.sanin.tv.R.string.success) ?: "Success")
    }

    suspend fun postMessage(
        userId: Int,
        text: String,
        edit: Int? = null,
        isPrivate: Boolean = false
    ): String {
        val encodedText = text.stringSanitizer()
        val idPart = if (edit != null) "id: $edit," else ""
        val query = """
            mutation {
                SaveMessageActivity(
                    ${idPart}recipientId: $userId,
                    message: $encodedText,
                    private: $isPrivate
                ) { id }
            }
        """.trimIndent()
        val result = executeQuery<JsonObject>(query)
        val errors = result?.get("errors")
        return errors?.toString() ?: (currContext()?.getString(com.sanin.tv.R.string.success) ?: "Success")
    }

    suspend fun postReply(activityId: Int, text: String, edit: Int? = null): String {
        val encodedText = text.stringSanitizer()
        val idPart = if (edit != null) "id: $edit," else ""
        val query = """
            mutation {
                SaveActivityReply(${idPart}activityId: $activityId, text: $encodedText) { id }
            }
        """.trimIndent()
        val result = executeQuery<JsonObject>(query)
        val errors = result?.get("errors")
        return errors?.toString() ?: (currContext()?.getString(com.sanin.tv.R.string.success) ?: "Success")
    }

    suspend fun postReview(summary: String, body: String, mediaId: Int, score: Int): String {
        val encodedSummary = summary.stringSanitizer()
        val encodedBody = body.stringSanitizer()
        val query = """
            mutation {
                SaveReview(
                    mediaId: $mediaId,
                    summary: $encodedSummary,
                    body: $encodedBody,
                    score: $score
                ) { siteUrl }
            }
        """.trimIndent()
        val result = executeQuery<JsonObject>(query)
        val errors = result?.get("errors")
        return errors?.toString() ?: (currContext()?.getString(com.sanin.tv.R.string.success) ?: "Success")
    }

    suspend fun deleteActivityReply(activityId: Int): Boolean {
        val query = """
            mutation { DeleteActivityReply(id: $activityId) { deleted } }
        """.trimIndent()
        val result = executeQuery<JsonObject>(query)
        return result?.get("errors") == null
    }

    suspend fun deleteActivity(activityId: Int): Boolean {
        val query = """
            mutation { DeleteActivity(id: $activityId) { deleted } }
        """.trimIndent()
        val result = executeQuery<JsonObject>(query)
        return result?.get("errors") == null
    }

    private fun String.stringSanitizer(): String {
        val sb = StringBuilder()
        var i = 0
        while (i < this.length) {
            val codePoint = this.codePointAt(i)
            if (codePoint > 0xFFFF) {
                sb.append("&#").append(codePoint).append(";")
                i += 2
            } else {
                sb.append(this[i])
                i++
            }
        }
        return Gson().toJson(sb.toString())
    }
}
