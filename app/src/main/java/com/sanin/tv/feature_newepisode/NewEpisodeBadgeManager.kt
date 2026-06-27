package com.sanin.tv.feature_newepisode

import android.content.Context
import com.sanin.tv.media.Media

/**
 * Tracks which anime had episodes available at the end of the previous session.
 *
 * Flow:
 *  1. [init] is called at app start — loads the last-session snapshot from prefs.
 *  2. [shouldShowBadge] is called per card in MediaAdaptor — returns true when
 *     the anime is tracked and has more available episodes than the snapshot recorded.
 *     It also registers this media in the current-session counts so they get saved later.
 *  3. [onAppBackground] is called from App lifecycle — persists the current-session
 *     counts so the next session can compare against them.
 *
 * "New Episode" means: a tracked anime now has more available episodes than it did
 * at the end of the user's previous visit.
 */
object NewEpisodeBadgeManager {

    private const val PREFS_NAME = "new_ep_badge_prefs"
    private const val KEY_SNAPSHOT = "ep_snapshot"

    /** Available episode counts as of the END of the last session. null = never visited. */
    private var lastSessionSnapshot: Map<Int, Int> = emptyMap()

    /** Available episode counts observed so far THIS session — saved on background. */
    private val currentSessionCounts = mutableMapOf<Int, Int>()

    private var initialized = false

    fun init(context: Context) {
        lastSessionSnapshot = loadSnapshot(context)
        initialized = true
    }

    /**
     * Returns true when the card should display the "New Episode" badge:
     *   - anime is currently being tracked (CURRENT or REPEATING)
     *   - there are available episodes to watch (nextAiringEpisode or totalEpisodes > 0)
     *   - the available count has grown since the last session snapshot
     *     (if no prior snapshot for this show, badge is suppressed to avoid first-run flood)
     *
     * Side effect: records this media in the current-session counts map.
     */
    fun shouldShowBadge(media: Media): Boolean {
        val id = media.id ?: return false

        val isTracking = media.userStatus == "CURRENT" || media.userStatus == "REPEATING"
        if (!isTracking || media.anime == null) return false

        val available = availableEpisodeCount(media)
        if (available <= 0) return false

        // Always track in the current session so the snapshot stays up-to-date.
        currentSessionCounts[id] = available

        val lastKnown = lastSessionSnapshot[id]
            ?: return false // null = first time seeing this show — suppress badge

        return available > lastKnown
    }

    /**
     * Call from MediaDetailsActivity when the user taps into a show.
     * Clears the badge for that show immediately (next card-bind will hide it)
     * and writes the dismissal to disk so it survives a force-kill.
     */
    fun markAsSeen(mediaId: Int, context: Context) {
        // currentSessionCounts[mediaId] holds the available-episode count that triggered
        // the badge. Lifting the snapshot baseline to that count kills the badge condition.
        val current = currentSessionCounts[mediaId] ?: return // not tracked — nothing to dismiss
        lastSessionSnapshot = lastSessionSnapshot.toMutableMap().also { it[mediaId] = current }
        // Persist immediately — merge snapshot + current session so nothing is lost.
        val merged = lastSessionSnapshot.toMutableMap().also { it.putAll(currentSessionCounts) }
        saveSnapshot(context, merged)
    }

    /**
     * Call from App when all activities stop (app goes to background).
     * Saves current-session episode counts so the next session can diff against them.
     */
    fun onAppBackground(context: Context) {
        if (currentSessionCounts.isNotEmpty()) {
            // Merge: carry forward anything from the old snapshot that wasn't seen this session.
            val merged = lastSessionSnapshot.toMutableMap()
            merged.putAll(currentSessionCounts)
            saveSnapshot(context, merged)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Returns the number of episodes currently available for this anime.
     * For airing shows we use nextAiringEpisode (most recent aired ep).
     * For finished shows we fall back to totalEpisodes.
     */
    private fun availableEpisodeCount(media: Media): Int {
        val isReleasing = media.status?.contains("RELEASING", ignoreCase = true) == true
        return if (isReleasing)
            media.anime?.nextAiringEpisode ?: media.anime?.totalEpisodes ?: 0
        else
            media.anime?.totalEpisodes ?: media.anime?.nextAiringEpisode ?: 0
    }

    /** Deserialise "id1:count1,id2:count2,..." from SharedPreferences. */
    private fun loadSnapshot(context: Context): Map<Int, Int> {
        val raw = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SNAPSHOT, null) ?: return emptyMap()
        return try {
            raw.split(",")
                .filter { it.contains(":") }
                .associate { entry ->
                    val (k, v) = entry.split(":")
                    k.trim().toInt() to v.trim().toInt()
                }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    /** Serialise map to "id1:count1,id2:count2,..." and write to SharedPreferences. */
    private fun saveSnapshot(context: Context, map: Map<Int, Int>) {
        val raw = map.entries.joinToString(",") { "${it.key}:${it.value}" }
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SNAPSHOT, raw)
            .apply()
    }
}
