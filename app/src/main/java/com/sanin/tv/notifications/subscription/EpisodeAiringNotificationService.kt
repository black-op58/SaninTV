package com.sanin.tv.notifications.subscription

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.sanin.tv.connections.anilist.Anilist
import com.sanin.tv.connections.anilist.AnilistQueries
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Feature 3: Smart Notifications
 * Checks AniList's nextAiringEpisode data for all subscribed anime and fires
 * a local notification when a new episode is airing within the next 24 hours
 * or has just aired since the last check.
 *
 * Designed to be called from WorkManager / TaskScheduler on a periodic interval.
 */
object EpisodeAiringNotificationService {
    private const val TAG = "EpisodeAiringNotifSvc"
    private const val CHANNEL_ID = "episode_airing_alerts"
    private const val CHANNEL_NAME = "Episode Airing Alerts"
    private const val PREFS_LAST_CHECK = "ep_airing_last_check"
    private const val PREFS_SEEN_EPISODES = "ep_airing_seen_set"

    /** Main entry point: run the airing check. Call from a WorkManager worker. */
    suspend fun checkAndNotify(context: Context) = withContext(Dispatchers.IO) {
    if (!isEnabled()) {
            Logger.log("$TAG: Smart notifications disabled — skipping")
            return@withContext
        }

        val subscriptions = SubscriptionHelper.getSubscriptions()
        if (subscriptions.isEmpty()) {
            Logger.log("$TAG: No subscriptions, skipping airing check")
            return@withContext
        }

        ensureNotificationChannel(context)

        val now = System.currentTimeMillis()
        val seenSet = getSeenEpisodes().toMutableSet()
        var notifId = (now / 1000).toInt()

        Logger.log("$TAG: Checking ${subscriptions.size} subscribed titles for new episodes")

        subscriptions.values.filter { it.isAnime }.forEach { sub ->
            try {
    val media = AnilistQueries.getAiringData(sub.id) ?: return@forEach
                val nextAiring = media.nextAiringEpisode ?: return@forEach
                val airingAtMs = (nextAiring.airingAt?.toLong() ?: 0L) * 1000L
                val episodeKey = "${sub.id}_ep${nextAiring.episode}"

                val airedRecently = airingAtMs in (now - TimeUnit.HOURS.toMillis(6))..now
                val airingVeryShortly = airingAtMs in now..(now + TimeUnit.HOURS.toMillis(1))

                if ((airedRecently || airingVeryShortly) && episodeKey !in seenSet) {
                    seenSet.add(episodeKey)
                    val timeLabel = if (airedRecently) "just aired" else "airs in < 1 hour"
                    Logger.log("$TAG: Notifying — ${sub.name} ep ${nextAiring.episode} $timeLabel")
                    fireNotification(
                        context = context,
                        notifId = notifId++,
                        title = "📺 New Episode — ${sub.name}",
                        text = "Episode ${nextAiring.episode} $timeLabel!",
                        coverUrl = sub.image
                    )
                }
            } catch (e: Exception) {
                Logger.log("$TAG: Error checking ${sub.name}: ${e.message}")
            }
        }

        saveSeenEpisodes(seenSet)
        PrefManager.setVal(PREFS_LAST_CHECK, now)
        Logger.log("$TAG: Airing check complete")
    }

    private fun fireNotification(
        context: Context,
        notifId: Int,
        title: String,
        text: String,
        coverUrl: String?
    ) {
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))

        nm.notify(notifId, builder.build())
    }

    private fun ensureNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Alerts when subscribed anime episodes air" }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun isEnabled(): Boolean =
        PrefManager.getVal(PrefName.SubscriptionNotificationEnabled, true)

    @Suppress("UNCHECKED_CAST")
    private fun getSeenEpisodes(): Set<String> =
        (PrefManager.getNullableCustomVal(PREFS_SEEN_EPISODES, null, Set::class.java) as? Set<String>)
            ?: emptySet()

    private fun saveSeenEpisodes(set: Set<String>) {
        // Keep at most 500 entries to avoid unbounded growth
        val trimmed = if (set.size > 500) set.toList().takeLast(500).toSet() else set
        PrefManager.setCustomVal(PREFS_SEEN_EPISODES, trimmed)
    }
}
