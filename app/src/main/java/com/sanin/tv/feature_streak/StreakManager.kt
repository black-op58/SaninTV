package com.sanin.tv.feature_streak

import android.content.Context
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.util.Logger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tracks a "consecutive watch days" streak locally on-device.
 *
 * A day counts if [recordWatchToday] is called at least once.
 * The streak resets if a full calendar day passes with no watch activity.
 *
 * Storage keys (via PrefManager.getCustomVal / setCustomVal):
 *   streak_last_date     – ISO date of the last watch day  (e.g. "2026-06-25")
 *   streak_current       – current consecutive-day count   (Int)
 *   streak_longest       – all-time best streak            (Int)
 *   streak_total_days    – total distinct days with watches (Int)
 */
object StreakManager {

    private const val KEY_LAST_DATE   = "streak_last_date"
    private const val KEY_CURRENT     = "streak_current"
    private const val KEY_LONGEST     = "streak_longest"
    private const val KEY_TOTAL_DAYS  = "streak_total_days"

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /** Call this every time the user marks an episode as watched. */
    fun recordWatchToday(context: Context): Int {
        PrefManager.init(context)
        val today = todayStr()
        val last  = PrefManager.getCustomVal(KEY_LAST_DATE, "", String::class.java)

        var current = PrefManager.getCustomVal(KEY_CURRENT, 0, Int::class.java)
        var longest = PrefManager.getCustomVal(KEY_LONGEST, 0, Int::class.java)
        var total   = PrefManager.getCustomVal(KEY_TOTAL_DAYS, 0, Int::class.java)

        when {
            last == today -> {
                // Already recorded today — no change to streak count
            }
            last == yesterday() -> {
                // Consecutive day!
                current++
                total++
            }
            else -> {
                // Streak broken (or first ever watch)
                current = 1
                total++
            }
        }

        if (current > longest) longest = current

        PrefManager.setCustomVal(KEY_LAST_DATE,  today)
        PrefManager.setCustomVal(KEY_CURRENT,    current)
        PrefManager.setCustomVal(KEY_LONGEST,    longest)
        PrefManager.setCustomVal(KEY_TOTAL_DAYS, total)

        Logger.log("StreakManager: streak=$current, longest=$longest, total=$total")
        return current
    }

    fun getCurrentStreak(context: Context): Int {
        PrefManager.init(context)
        ensureNotBroken(context)
        return PrefManager.getCustomVal(KEY_CURRENT, 0, Int::class.java)
    }

    fun getLongestStreak(context: Context): Int {
        PrefManager.init(context)
        return PrefManager.getCustomVal(KEY_LONGEST, 0, Int::class.java)
    }

    fun getTotalWatchDays(context: Context): Int {
        PrefManager.init(context)
        return PrefManager.getCustomVal(KEY_TOTAL_DAYS, 0, Int::class.java)
    }

    /**
     * Returns a milestone message if [newStreak] hits a notable number,
     * or null if no milestone was just reached.
     */
    fun milestoneMessage(newStreak: Int): String? = when (newStreak) {
        1   -> "🌱 Your streak begins! Watch again tomorrow to keep it going."
        3   -> "🔥 3-day streak! The habit is forming."
        7   -> "🏆 One week straight! You're on fire."
        14  -> "💪 Two-week streak — dedication unlocked."
        30  -> "👑 30 days! Monthly master achieved."
        60  -> "⚡ 60-day streak. Absolutely legendary."
        100 -> "💎 100 days. A true anime warrior."
        365 -> "🌟 365 days. One full year — you are the anime."
        else -> if (newStreak > 0 && newStreak % 50 == 0) "🎖️ ${newStreak}-day streak!" else null
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /** Resets streak to 0 if more than one day has passed since last watch. */
    private fun ensureNotBroken(context: Context) {
        val last    = PrefManager.getCustomVal(KEY_LAST_DATE, "", String::class.java)
        val current = PrefManager.getCustomVal(KEY_CURRENT, 0, Int::class.java)
        if (current > 0 && last.isNotBlank() && last != todayStr() && last != yesterday()) {
            PrefManager.setCustomVal(KEY_CURRENT, 0)
            Logger.log("StreakManager: streak broken (last=$last)")
        }
    }

    private fun todayStr(): String     = dateFmt.format(Date())
    private fun yesterday(): String    = dateFmt.format(Date(System.currentTimeMillis() - 86_400_000L))
}
