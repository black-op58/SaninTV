package com.sanin.tv.notifications

import android.content.Context
import androidx.work.*
import com.sanin.tv.connections.anilist.Anilist
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Feature 5: New Episode Notifications
 *
 * Runs every 3 hours in the background (wifi-only by default).
 * For each show in the user's CURRENT watchlist it checks whether Anilist
 * reports more aired episodes than we last recorded, then fires a
 * notification for every show that has something new.
 *
 * Integration — call EpisodeNotificationWorker.schedule(context) once from
 * your Application.onCreate() or from a Settings toggle:
 *
 *   if (PrefManager.getVal(PrefName.NewEpisodeNotifications)) {
 *       EpisodeNotificationWorker.schedule(context)
 *   } else {
 *       EpisodeNotificationWorker.cancel(context)
 *   }
 */
class EpisodeNotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            if (!PrefManager.getVal<Boolean>(PrefName.NewEpisodeNotifications)) {
                return@withContext Result.success()
            }

            val watchingList = Anilist.getUserWatchingList() ?: return@withContext Result.retry()

            for (media in watchingList) {
                val mediaId    = media.id
                val title      = media.name ?: media.nameRomaji ?: continue
                val nowAired   = media.anime?.nextAiringEpisode
                    ?.let { it.episode - 1 }
                    ?: media.anime?.episodes
                    ?: continue

                val seenKey     = "notif_last_ep_$mediaId"
                val lastNotified = PrefManager.getCustomVal(seenKey, 0, Int::class.java)

                if (nowAired > lastNotified) {
                    NotificationHelper.showNewEpisodeNotification(
                        context = context,
                        mediaId = mediaId,
                        showTitle = title,
                        episodeNumber = nowAired,
                        coverUrl = media.cover
                    )
                    PrefManager.setCustomVal(seenKey, nowAired)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "sanintv_episode_check"

        fun schedule(context: Context, wifiOnly: Boolean = true) {
            NotificationHelper.createChannels(context)

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(
                    if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
                )
                .build()

            val request = PeriodicWorkRequestBuilder<EpisodeNotificationWorker>(
                3, TimeUnit.HOURS,
                30, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        fun runNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<EpisodeNotificationWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
