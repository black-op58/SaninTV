package com.sanin.tv.feature_sync

/**
 * Feature 8: AniList/MAL Sync Conflict Resolver
 * Represents a progress conflict between AniList and MAL for the same media item.
 */
data class SyncConflict(
    val mediaId: Int,
    val mediaIdMal: Int?,
    val mediaName: String,
    val coverUrl: String?,
    val totalEpisodes: Int?,
    val anilistProgress: Int?,
    val anilistStatus: String?,
    val malProgress: Int?,
    val malStatus: String?,
    val anilistScore: Int?,
    val malScore: Float?
) {
    val hasProgressConflict: Boolean
        get() = anilistProgress != null && malProgress != null && anilistProgress != malProgress

    val hasStatusConflict: Boolean
        get() = anilistStatus != null && malStatus != null &&
                normalizeStatus(anilistStatus) != normalizeStatus(malStatus)

    val hasScoreConflict: Boolean
        get() = anilistScore != null && malScore != null &&
                anilistScore != (malScore * 10).toInt()

    val hasAnyConflict: Boolean
        get() = hasProgressConflict || hasStatusConflict || hasScoreConflict

    private fun normalizeStatus(s: String): String = when (s.uppercase()) {
        "CURRENT", "WATCHING" -> "WATCHING"
        "COMPLETED" -> "COMPLETED"
        "PAUSED", "ON_HOLD" -> "PAUSED"
        "DROPPED" -> "DROPPED"
        "PLANNING", "PLAN_TO_WATCH" -> "PLANNING"
        else -> s.uppercase()
    }
}

enum class SyncResolution { USE_ANILIST, USE_MAL, USE_HIGHER, USE_LOWER, MANUAL }
