package com.sanin.tv.feature_stats

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sanin.tv.connections.anilist.Anilist
import com.sanin.tv.connections.anilist.AnilistQueries
import com.sanin.tv.snackString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Feature 10: Statistics Dashboard
 * Displays watch-time analytics, genre breakdown, streak tracker,
 * and year-by-year episode history from AniList data.
 */
class StatsDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, statusBarHeight(), 48, 64)
        }
        scroll.addView(root)
        setContentView(scroll)

        root.addView(headerText("📊 Statistics Dashboard"))

        val loader = ProgressBar(this)
        root.addView(loader)

        lifecycleScope.launch {
    val cached = StatsCalculator.loadCached()
            if (cached != null) {
                loader.visibility = View.GONE
                renderStats(root, cached)
            }

            try {
    val userId = Anilist.userid
                if (userId == null) {
                    loader.visibility = View.GONE
                    root.addView(bodyText("Please log in to AniList to see your stats."))
                    return@launch
                }

                val allMedia = withContext(Dispatchers.IO) {
                    buildList {
                        addAll(AnilistQueries.getWatchingMedia(userId) ?: emptyList())
                        addAll(AnilistQueries.getCompletedMedia(userId) ?: emptyList())
                    }
                }

                val stats = withContext(Dispatchers.IO) { StatsCalculator.compute(allMedia) }
                loader.visibility = View.GONE

                if (cached == null) {
                    renderStats(root, stats)
                } else {
                    root.addView(bodyText("✓ Stats refreshed"))
                }
            } catch (e: Exception) {
                loader.visibility = View.GONE
                snackString("Could not refresh stats: ${e.message}")
            }
        }
    }

    private fun renderStats(root: LinearLayout, stats: WatchStats) {
        root.addView(sectionHeader("⏱ Watch Time"))
        root.addView(statRow("Total Episodes Watched", stats.totalEpisodes.toString()))
        root.addView(statRow("Total Hours", "%.1f h".format(stats.totalHoursWatched)))
        root.addView(statRow("Total Days", "%.2f days".format(stats.totalDaysWatched)))

        root.addView(sectionHeader("📚 List Breakdown"))
        root.addView(statRow("Total Titles", stats.uniqueTitles.toString()))
        root.addView(statRow("Completed", stats.completedTitles.toString()))
        root.addView(statRow("Currently Watching", stats.currentlyWatching.toString()))
        root.addView(statRow("Planning to Watch", stats.planningTitles.toString()))
        root.addView(statRow("Dropped", stats.droppedTitles.toString()))
        root.addView(statRow("Average Score", "%.1f / 100".format(stats.averageScore)))

        root.addView(sectionHeader("🔥 Streaks"))
        root.addView(statRow("Current Streak", "${stats.currentStreak} days"))
        root.addView(statRow("Longest Streak", "${stats.longestStreak} days"))

        root.addView(sectionHeader("🎭 Top Genres"))
        stats.topGenres.forEachIndexed { i, genre ->
            val count = stats.genreBreakdown[genre] ?: 0
            root.addView(statRow("#${i + 1} $genre", "$count titles"))
        }

        root.addView(sectionHeader("📅 Episodes by Year"))
        stats.yearBreakdown.entries
            .sortedByDescending { it.key }
            .take(6)
            .forEach { (year, eps) ->
                root.addView(statRow(year.toString(), "$eps episodes"))
            }

        val computedDate = java.text.SimpleDateFormat("MMM d, yyyy HH:mm", Locale.US)
            .format(java.util.Date(stats.computedAt))
        root.addView(bodyText("Last updated: $computedDate").apply {
            textSize = 11f
            setPadding(0, 24, 0, 0)
        })
    }

    private fun headerText(text: String) = TextView(this).apply {
        this.text = text; textSize = 22f; setPadding(0, 0, 0, 16)
    }

    private fun sectionHeader(text: String) = TextView(this).apply {
        this.text = text; textSize = 17f; setPadding(0, 32, 0, 8)
    }

    private fun statRow(label: String, value: String): View {
    val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, 4, 0, 4) }
        row.addView(TextView(this).apply {
            text = label; textSize = 14f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        row.addView(TextView(this).apply {
            text = value; textSize = 14f
        })
        return row
    }

    private fun bodyText(text: String) = TextView(this).apply { this.text = text; textSize = 13f }

    private fun statusBarHeight(): Int {
    val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else 0
    }
}
