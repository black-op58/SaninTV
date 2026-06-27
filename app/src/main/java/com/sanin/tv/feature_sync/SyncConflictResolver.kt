package com.sanin.tv.feature_sync

import com.sanin.tv.connections.anilist.AnilistMutations
import com.sanin.tv.connections.mal.MAL
import com.sanin.tv.connections.mal.MALQueries
import com.sanin.tv.util.Logger

/**
 * Feature 8: AniList/MAL Sync Conflict Resolver
 * Detects and resolves progress/status conflicts between AniList and MAL.
 */
object SyncConflictResolver {
    private const val TAG = "SyncConflictResolver"

    /**
     * Detect all conflicts between AniList and MAL watch lists.
     * Requires both services to be logged in.
     */
    suspend fun detectConflicts(
        anilistList: List<AnilistEntry>,
        malList: List<MALEntry>
    ): List<SyncConflict> {
    val conflicts = mutableListOf<SyncConflict>()
        val malMap = malList.associateBy { it.malId }

        for (aniEntry in anilistList) {
    val malId = aniEntry.malId ?: continue
            val malEntry = malMap[malId] ?: continue

            val conflict = SyncConflict(
                mediaId = aniEntry.mediaId,
                mediaIdMal = malId,
                mediaName = aniEntry.title,
                coverUrl = aniEntry.coverUrl,
                totalEpisodes = aniEntry.totalEpisodes,
                anilistProgress = aniEntry.progress,
                anilistStatus = aniEntry.status,
                malProgress = malEntry.progress,
                malStatus = malEntry.status,
                anilistScore = aniEntry.score,
                malScore = malEntry.score
            )

            if (conflict.hasAnyConflict) {
                Logger.log("$TAG: Conflict found for ${aniEntry.title} — " +
                        "AL=${aniEntry.progress}/${aniEntry.status}, MAL=${malEntry.progress}/${malEntry.status}")
                conflicts.add(conflict)
            }
        }

        Logger.log("$TAG: Found ${conflicts.size} conflicts out of ${anilistList.size} shared entries")
        return conflicts
    }

    /**
     * Apply a resolution to a single conflict.
     * Updates the losing service to match the winning service.
     */
    suspend fun resolveConflict(
        conflict: SyncConflict,
        resolution: SyncResolution,
        manualProgress: Int? = null,
        manualStatus: String? = null
    ): Boolean {
    return try {
    when (resolution) {
                SyncResolution.USE_ANILIST -> {
                    pushToMAL(conflict, conflict.anilistProgress, conflict.anilistStatus)
                }
                SyncResolution.USE_MAL -> {
                    pushToAnilist(conflict, conflict.malProgress, conflict.malStatus)
                }
                SyncResolution.USE_HIGHER -> {
    val progress = maxOf(conflict.anilistProgress ?: 0, conflict.malProgress ?: 0)
                    pushToAnilist(conflict, progress, null)
                    pushToMAL(conflict, progress, null)
                }
                SyncResolution.USE_LOWER -> {
    val progress = minOf(conflict.anilistProgress ?: 0, conflict.malProgress ?: 0)
                    pushToAnilist(conflict, progress, null)
                    pushToMAL(conflict, progress, null)
                }
                SyncResolution.MANUAL -> {
                    manualProgress?.let { p ->
                        pushToAnilist(conflict, p, manualStatus)
                        pushToMAL(conflict, p, manualStatus)
                    } ?: false
                }
            }
        } catch (e: Exception) {
            Logger.log("$TAG: Failed to resolve conflict for ${conflict.mediaName}: ${e.message}")
            false
        }
    }

    private suspend fun pushToAnilist(conflict: SyncConflict, progress: Int?, status: String?): Boolean {
    if (progress == null) return false
        Logger.log("$TAG: Pushing to AniList — ${conflict.mediaName} progress=$progress status=$status")
        return try {
            Anilist.mutation.editList(
                mediaId = conflict.mediaId,
                progress = progress,
                status = status ?: conflict.anilistStatus ?: "CURRENT",
                score = conflict.anilistScore?.toDouble() ?: 0.0,
                startedAt = null,
                completedAt = null,
                repeat = 0,
                notes = null,
                private = false,
                customLists = null,
                advancedScores = null,
                progressVolumes = null
            )
            true
        } catch (e: Exception) {
            Logger.log("$TAG: AniList push failed: ${e.message}")
            false
        }
    }

    private suspend fun pushToMAL(conflict: SyncConflict, progress: Int?, status: String?): Boolean {
    if (progress == null || conflict.mediaIdMal == null) return false
        Logger.log("$TAG: Pushing to MAL — ${conflict.mediaName} progress=$progress status=$status")
        return try {
            MALQueries.updateAnimeList(
                conflict.mediaIdMal,
                status = when (status?.uppercase()) {
                    "CURRENT" -> "watching"
                    "COMPLETED" -> "completed"
                    "PAUSED" -> "on_hold"
                    "DROPPED" -> "dropped"
                    "PLANNING" -> "plan_to_watch"
                    else -> "watching"
                },
                score = null,
                episode = progress
            )
            true
        } catch (e: Exception) {
            Logger.log("$TAG: MAL push failed: ${e.message}")
            false
        }
    }

    data class AnilistEntry(
        val mediaId: Int,
        val malId: Int?,
        val title: String,
        val coverUrl: String?,
        val totalEpisodes: Int?,
        val progress: Int?,
        val status: String?,
        val score: Int?
    )

    data class MALEntry(
        val malId: Int,
        val progress: Int?,
        val status: String?,
        val score: Float?
    )
}
