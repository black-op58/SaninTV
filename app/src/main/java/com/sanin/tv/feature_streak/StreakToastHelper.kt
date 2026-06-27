package com.sanin.tv.feature_streak

import android.content.Context
import com.sanin.tv.toast

/**
 * Call this after [StreakManager.recordWatchToday] to surface a milestone
 * toast if the new streak value hits a notable number.
 *
 * Usage (from wherever you update progress):
 *
 *   val newStreak = StreakManager.recordWatchToday(context)
 *   StreakToastHelper.showIfMilestone(context, newStreak)
 */
object StreakToastHelper {

    fun showIfMilestone(context: Context, newStreak: Int) {
        val msg = StreakManager.milestoneMessage(newStreak) ?: return
        toast(msg)
    }
}
