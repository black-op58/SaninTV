package com.sanin.tv.settings.saving

import android.content.Context
import android.content.SharedPreferences

/**
 * Feature 7: Per-Show Source Memory with configurable expiry.
 *
 * Remembers the user's last-used source, server, quality, and language for
 * each show, with a saved timestamp so the memory can expire after a
 * user-configurable window (6h / 12h / 1–7 days / forever / never).
 *
 * Expiry controlled by [PrefName.SourceMemoryExpiryHours]:
 *   0   = never remember (feature disabled)
 *  -1   = remember forever
 *  N>0  = remember for N hours
 *
 * Call [init] from Application.onCreate (or MainActivity.onCreate) before use.
 */
object SourceMemoryManager {

    data class SourceMemory(
        val sourceIndex: Int,
        val serverIndex: Int,
        val quality: String,
        val lang: Int
    )

    /** Expiry option shown in Settings UI. */
    data class ExpiryOption(val label: String, val hours: Int)

    val EXPIRY_OPTIONS = listOf(
        ExpiryOption("Never",    0),
        ExpiryOption("6 hours",  6),
        ExpiryOption("12 hours", 12),
        ExpiryOption("1 day",    24),
        ExpiryOption("2 days",   48),
        ExpiryOption("3 days",   72),
        ExpiryOption("4 days",   96),
        ExpiryOption("7 days",   168),
        ExpiryOption("Forever",  -1)
    )

    private const val PREFS_NAME = "source_memory"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ─── Key helpers ─────────────────────────────────────────────────────────

    private fun keySource(id: Int)  = "src_$id"
    private fun keyServer(id: Int)  = "srv_$id"
    private fun keyQuality(id: Int) = "qty_$id"
    private fun keyLang(id: Int)    = "lng_$id"
    private fun keySavedAt(id: Int) = "ts_$id"

    // ─── Save ────────────────────────────────────────────────────────────────

    /**
     * Persist source selection for [mediaId].
     * Pass [expiryHours] from [PrefName.SourceMemoryExpiryHours]; 0 = feature off.
     */
    fun save(
        mediaId: Int,
        sourceIndex: Int,
        serverIndex: Int,
        quality: String,
        lang: Int,
        expiryHours: Int = 24
    ) {
        if (expiryHours == 0) return  // feature disabled
        prefs.edit()
            .putInt(keySource(mediaId),  sourceIndex)
            .putInt(keyServer(mediaId),  serverIndex)
            .putString(keyQuality(mediaId), quality)
            .putInt(keyLang(mediaId),    lang)
            .putLong(keySavedAt(mediaId), System.currentTimeMillis())
            .apply()
    }

    // ─── Load ────────────────────────────────────────────────────────────────

    /**
     * Retrieve saved source for [mediaId], respecting [expiryHours].
     * Returns null if never saved, the feature is disabled, or the entry expired.
     */
    fun load(mediaId: Int, expiryHours: Int = 24): SourceMemory? {
        if (expiryHours == 0) return null  // feature disabled

        if (!prefs.contains(keySource(mediaId))) return null

        if (expiryHours != -1) {
            val savedAt = prefs.getLong(keySavedAt(mediaId), 0L)
            val expiryMs = expiryHours * 3_600_000L
            if (System.currentTimeMillis() - savedAt > expiryMs) {
                clear(mediaId)
                return null
            }
        }

        val source  = prefs.getInt(keySource(mediaId),     -1)
        val server  = prefs.getInt(keyServer(mediaId),     -1)
        val quality = prefs.getString(keyQuality(mediaId), "") ?: ""
        val lang    = prefs.getInt(keyLang(mediaId),        0)

        if (source == -1) return null
        return SourceMemory(source, server, quality, lang)
    }

    /** Return how many hours until this entry expires, or null if not saved / already expired. */
    fun hoursUntilExpiry(mediaId: Int, expiryHours: Int): Long? {
        if (expiryHours <= 0) return null
        val savedAt = prefs.getLong(keySavedAt(mediaId), 0L)
        if (savedAt == 0L) return null
        val expiryMs = expiryHours * 3_600_000L
        val remainingMs = expiryMs - (System.currentTimeMillis() - savedAt)
        return if (remainingMs > 0) remainingMs / 3_600_000L else null
    }

    // ─── Clear ───────────────────────────────────────────────────────────────

    fun clear(mediaId: Int) {
        prefs.edit()
            .remove(keySource(mediaId))
            .remove(keyServer(mediaId))
            .remove(keyQuality(mediaId))
            .remove(keyLang(mediaId))
            .remove(keySavedAt(mediaId))
            .apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
